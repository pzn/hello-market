package com.github.pzn.hellomarket.integration.appdirect.processor;

import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.USER_NOT_FOUND;
import static com.github.pzn.hellomarket.integration.appdirect.event.EventType.SUBSCRIPTION_NOTICE;

import com.github.pzn.hellomarket.integration.appdirect.AppDirectApiResponse;
import com.github.pzn.hellomarket.integration.appdirect.event.AppDirectNotification;
import com.github.pzn.hellomarket.integration.appdirect.event.EventType;
import com.github.pzn.hellomarket.integration.appdirect.event.NoticeType;
import com.github.pzn.hellomarket.model.entity.AppUser;
import com.github.pzn.hellomarket.service.AppUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SubscriptionNoticeProcessor implements AppDirectNotificationProcessor {

  private AppUserService appUserService;

  @Autowired
  public SubscriptionNoticeProcessor(AppUserService appUserService) {
    this.appUserService = appUserService;
  }

  @Override
  public AppDirectApiResponse process(AppDirectNotification notification) throws NotificationProcessorException {

    String code = notification.getPayload().getAccount().getAccountIdentifier();
    AppUser appUser = appUserService.findByCode(code);
    if (appUser == null) {
      log.warn("Cannot process notification for AppUser because account not found! From:{}, code:{}",
          notification.getMarketplace().getPartner(), code);
      return AppDirectApiResponse.builder()
          .success(false)
          .errorCode(USER_NOT_FOUND)
          .build();
    }

    processNotice(notification.getPayload().getType(), appUser);

    return AppDirectApiResponse.builder()
        .success(true)
        .build();
  }

  private void processNotice(NoticeType noticeType, AppUser appUser) {

    switch (noticeType) {
      case CLOSED:
      case DEACTIVATED:
        changeStatus(appUser, false);
        break;
      case REACTIVATED:
        changeStatus(appUser, true);
        break;
      case UPCOMING_INVOICE:
      default:
        break;
    }
  }

  private void changeStatus(AppUser appUser, boolean newActiveStatus) {

    if (appUser.isActive() != newActiveStatus) {
      appUserService.changeStatus(appUser.getId(), newActiveStatus);
      log.info("AppUser(code:{}) {}", appUser.getCode(), newActiveStatus ? "enabled" : "disabled");
    } else {
      log.info("AppUser(code:{}) already {}", appUser.getCode(), newActiveStatus ? "enabled" : "disabled");
    }
  }

  @Override
  public EventType getType() {
    return SUBSCRIPTION_NOTICE;
  }
}
