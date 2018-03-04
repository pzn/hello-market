package com.github.pzn.hellomarket.integration.appdirect.processor;

import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.ACCOUNT_NOT_FOUND;
import static com.github.pzn.hellomarket.integration.appdirect.event.EventType.SUBSCRIPTION_NOTICE;

import com.github.pzn.hellomarket.integration.appdirect.AppDirectApiResponse;
import com.github.pzn.hellomarket.integration.appdirect.event.Account;
import com.github.pzn.hellomarket.integration.appdirect.event.AppDirectNotification;
import com.github.pzn.hellomarket.integration.appdirect.event.EventType;
import com.github.pzn.hellomarket.integration.appdirect.event.NoticeType;
import com.github.pzn.hellomarket.model.entity.AppOrg;
import com.github.pzn.hellomarket.repository.AppOrgRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SubscriptionNoticeProcessor implements AppDirectNotificationProcessor {

  private AppOrgRepository appOrgRepository;

  @Autowired
  public SubscriptionNoticeProcessor(AppOrgRepository appOrgRepository) {
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

    processNotice(notification.getPayload().getType(), appOrg);

    return AppDirectApiResponse.builder()
        .success(true)
        .accountIdentifier(appOrg.getCode())
        .build();
  }

  private AppOrg getAppOrg(Account account) {
    return appOrgRepository.findByCode(account.getAccountIdentifier());
  }

  private void processNotice(NoticeType noticeType, AppOrg appOrg) {

    switch (noticeType) {
      case CLOSED:
        removeAppOrg(appOrg);
        break;
      case DEACTIVATED:
        changeStatus(appOrg, false);
        break;
      case REACTIVATED:
        changeStatus(appOrg, true);
        break;
      case UPCOMING_INVOICE:
      default:
        break;
    }
  }

  private void removeAppOrg(AppOrg appOrg) {
    appOrgRepository.delete(appOrg);
  }

  private void changeStatus(AppOrg appOrg, boolean newActiveStatus) {

    if (appOrg.getActive() != newActiveStatus) {
      appOrgRepository.changeActiveStatus(appOrg.getId(), newActiveStatus);
      log.info("AppUser(code:{}) as been {}", appOrg.getCode(), newActiveStatus ? "enabled" : "disabled");
    } else {
      log.info("AppUser(code:{}) already {}", appOrg.getCode(), newActiveStatus ? "enabled" : "disabled");
    }
  }

  @Override
  public EventType getType() {
    return SUBSCRIPTION_NOTICE;
  }
}
