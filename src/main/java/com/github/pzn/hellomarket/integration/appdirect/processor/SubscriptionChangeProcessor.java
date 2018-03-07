package com.github.pzn.hellomarket.integration.appdirect.processor;

import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.ACCOUNT_NOT_FOUND;
import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.CONFIGURATION_ERROR;
import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.MAX_USERS_REACHED;
import static com.github.pzn.hellomarket.integration.appdirect.event.EventType.SUBSCRIPTION_CHANGE;

import com.github.pzn.hellomarket.integration.appdirect.AppDirectApiResponse;
import com.github.pzn.hellomarket.integration.appdirect.event.Account;
import com.github.pzn.hellomarket.integration.appdirect.event.AppDirectNotification;
import com.github.pzn.hellomarket.integration.appdirect.event.EventType;
import com.github.pzn.hellomarket.integration.appdirect.event.Payload;
import com.github.pzn.hellomarket.model.entity.AppOrg;
import com.github.pzn.hellomarket.model.entity.SubscriptionType;
import com.github.pzn.hellomarket.repository.AppOrgRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SubscriptionChangeProcessor implements AppDirectNotificationProcessor {

  private AppOrgRepository appOrgRepository;

  @Autowired
  public SubscriptionChangeProcessor(AppOrgRepository appOrgRepository) {
    this.appOrgRepository = appOrgRepository;
  }

  @Override
  public AppDirectApiResponse process(AppDirectNotification notification) throws NotificationProcessorException {

    AppOrg appOrg = retrieveAppOrg(notification.getPayload().getAccount());

    SubscriptionType newSubscriptionType = extractNewSubscriptionType(notification.getPayload());

    companyCanAssignNewUsers(appOrg, newSubscriptionType);

    updateAppOrgWithNewSubscriptionType(appOrg, newSubscriptionType);

    return AppDirectApiResponse.success(appOrg.getCode());
  }

  private AppOrg retrieveAppOrg(Account account) {

    String accountIdentifier = account.getAccountIdentifier();

    AppOrg appOrg = appOrgRepository.findByCodeFetchUsers(accountIdentifier);
    if (appOrg != null) {
      return appOrg;
    }

    throw new NotificationProcessorException(ACCOUNT_NOT_FOUND, accountIdentifier, null,
        String.format("Cannot find company(accountIdentifier:%s)", accountIdentifier));
  }

  private SubscriptionType extractNewSubscriptionType(Payload payload) {

    String editionCode = payload.getOrder().getEditionCode();

    try {
      return SubscriptionType.valueOf(editionCode);
    } catch (Exception e) {
      throw new NotificationProcessorException(CONFIGURATION_ERROR,
          payload.getAccount().getAccountIdentifier(), null,
          String.format("Unrecognized Edition(code:{})", editionCode));
    }
  }

  private void companyCanAssignNewUsers(AppOrg appOrg, SubscriptionType newSubscriptionType) {

    if (newSubscriptionType.getMaxUsers() >= appOrg.getAppUsers().size()) {
      return;
    }

    throw new NotificationProcessorException(MAX_USERS_REACHED, appOrg.getCode(), null,
        String.format("Company(accountIdentifier:%s) cannot allow more users", appOrg.getCode()));
  }

  private void updateAppOrgWithNewSubscriptionType(AppOrg appOrg, SubscriptionType newSubscriptionType) {
    appOrg.setSubscriptionType(newSubscriptionType);
    appOrgRepository.save(appOrg);
  }

  @Override
  public EventType getType() {
    return SUBSCRIPTION_CHANGE;
  }
}
