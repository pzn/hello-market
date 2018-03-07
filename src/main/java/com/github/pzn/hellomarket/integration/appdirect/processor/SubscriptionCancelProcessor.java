package com.github.pzn.hellomarket.integration.appdirect.processor;

import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.ACCOUNT_NOT_FOUND;
import static com.github.pzn.hellomarket.integration.appdirect.event.EventType.SUBSCRIPTION_CANCEL;

import com.github.pzn.hellomarket.integration.appdirect.AppDirectApiResponse;
import com.github.pzn.hellomarket.integration.appdirect.event.Account;
import com.github.pzn.hellomarket.integration.appdirect.event.AppDirectNotification;
import com.github.pzn.hellomarket.integration.appdirect.event.EventType;
import com.github.pzn.hellomarket.model.entity.AppOrg;
import com.github.pzn.hellomarket.repository.AppOrgRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SubscriptionCancelProcessor implements AppDirectNotificationProcessor {

  private AppOrgRepository appOrgRepository;

  @Autowired
  public SubscriptionCancelProcessor(AppOrgRepository appOrgRepository) {
    this.appOrgRepository = appOrgRepository;
  }

  @Override
  public AppDirectApiResponse process(AppDirectNotification notification) throws NotificationProcessorException {

    AppOrg appOrg = retrieveAppOrg(notification.getPayload().getAccount());

    removeAppOrg(appOrg);

    return AppDirectApiResponse.success(appOrg.getCode());
  }

  private AppOrg retrieveAppOrg(Account account) {

    String accountIdentifier = account.getAccountIdentifier();

    AppOrg appOrg = appOrgRepository.findByCode(accountIdentifier);
    if (appOrg != null) {
      return appOrg;
    }

    throw new NotificationProcessorException(ACCOUNT_NOT_FOUND, accountIdentifier, null,
        String.format("Cannot find company(accountIdentifier:%s)", accountIdentifier));
  }

  private void removeAppOrg(AppOrg appOrg) {
    appOrgRepository.delete(appOrg);
  }

  @Override
  public EventType getType() {
    return SUBSCRIPTION_CANCEL;
  }
}
