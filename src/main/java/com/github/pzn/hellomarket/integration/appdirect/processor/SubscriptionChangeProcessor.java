package com.github.pzn.hellomarket.integration.appdirect.processor;

import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.ACCOUNT_NOT_FOUND;
import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.CONFIGURATION_ERROR;
import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.MAX_USERS_REACHED;
import static com.github.pzn.hellomarket.integration.appdirect.event.EventType.SUBSCRIPTION_CHANGE;

import com.github.pzn.hellomarket.integration.appdirect.AppDirectApiResponse;
import com.github.pzn.hellomarket.integration.appdirect.event.Account;
import com.github.pzn.hellomarket.integration.appdirect.event.AppDirectNotification;
import com.github.pzn.hellomarket.integration.appdirect.event.EventType;
import com.github.pzn.hellomarket.integration.appdirect.event.Order;
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

    AppOrg appOrg = getAppOrg(notification.getPayload().getAccount());
    if (appOrg == null) {
      log.warn("Organization(marketIdentifier:{}) from partner '{}' attempted to cancel subscription, but not found in database",
          notification.getPayload().getAccount().getAccountIdentifier(), notification.getMarketplace().getPartner());

      return AppDirectApiResponse.builder()
          .success(false)
          .errorCode(ACCOUNT_NOT_FOUND)
          .build();
    }

    SubscriptionType newSubscriptionType;
    try {
      newSubscriptionType = retrieveNewSubscriptionType(notification.getPayload().getOrder());
    } catch (Exception e) {
      log.warn("Unrecognized Edition(code:{}) from partner '{}'",
          notification.getPayload().getOrder().getEditionCode(), notification.getMarketplace().getPartner());

      return AppDirectApiResponse.builder()
          .success(false)
          .accountIdentifier(appOrg.getCode())
          .errorCode(CONFIGURATION_ERROR)
          .build();
    }

    if (newSubscriptionType.getMaxUsers() < appOrg.getAppUsers().size()) {
      log.info("Organization(code:{}, marketIdentifier:{}) from partner '{}' attempted to change subscription, but its userbase won't fit in the new one",
          appOrg.getCode(), appOrg.getMarketIdentifier(), notification.getMarketplace().getPartner());

      return AppDirectApiResponse.builder()
          .success(false)
          .accountIdentifier(appOrg.getCode())
          .errorCode(MAX_USERS_REACHED)
          .build();
    }

    updateAppOrgWithNewSubscriptionType(appOrg, newSubscriptionType);

    return AppDirectApiResponse.builder()
        .success(true)
        .accountIdentifier(appOrg.getCode())
        .build();
  }

  private SubscriptionType retrieveNewSubscriptionType(Order order) {
    return SubscriptionType.valueOf(order.getEditionCode());
  }

  private AppOrg getAppOrg(Account account) {
    return appOrgRepository.findByCodeFetchUsers(account.getAccountIdentifier());
  }

  @Override
  public EventType getType() {
    return SUBSCRIPTION_CHANGE;
  }

  private void updateAppOrgWithNewSubscriptionType(AppOrg appOrg, SubscriptionType newSubscriptionType) {
    appOrg.setSubscriptionType(newSubscriptionType);
    appOrgRepository.save(appOrg);
  }
}
