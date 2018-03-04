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

    AppOrg appOrg = getAppOrg(notification.getPayload().getAccount());
    if (appOrg == null) {
      log.warn("Organization(marketIdentifier:{}) from partner '{}' attempted to cancel subscription, but not found in database",
          notification.getPayload().getAccount().getAccountIdentifier(), notification.getMarketplace().getPartner());
      return AppDirectApiResponse.builder()
          .success(false)
          .errorCode(ACCOUNT_NOT_FOUND)
          .build();
    }

    removeAppOrg(appOrg);

    return AppDirectApiResponse.builder()
        .success(true)
        .accountIdentifier(appOrg.getCode())
        .build();
  }

  private AppOrg getAppOrg(Account account) {
    return appOrgRepository.findByCode(account.getAccountIdentifier());
  }

  private void removeAppOrg(AppOrg appOrg) {
    appOrgRepository.delete(appOrg);
  }

  @Override
  public EventType getType() {
    return SUBSCRIPTION_CANCEL;
  }
}
