package com.github.pzn.hellomarket.integration.appdirect.processor;

import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.USER_ALREADY_EXISTS;
import static com.github.pzn.hellomarket.integration.appdirect.event.EventType.SUBSCRIPTION_ORDER;

import com.github.pzn.hellomarket.integration.appdirect.AppDirectApiResponse;
import com.github.pzn.hellomarket.integration.appdirect.event.AppDirectNotification;
import com.github.pzn.hellomarket.integration.appdirect.event.EventType;
import com.github.pzn.hellomarket.model.entity.AppUser;
import com.github.pzn.hellomarket.model.entity.SubscriptionType;
import com.github.pzn.hellomarket.service.AppUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SubscriptionOrderProcessor implements AppDirectNotificationProcessor {

  private AppUserService appUserService;

  @Autowired
  public SubscriptionOrderProcessor(AppUserService appUserService) {
    this.appUserService = appUserService;
  }

  @Override
  public AppDirectApiResponse process(AppDirectNotification notification) throws NotificationProcessorException {

    AppUser appUser = appUserService.findByMarketAccountIdentifier(notification.getCreator().getUuid());
    if (appUser == null) { // TODO TEEEEEEEEEEEST
      appUser = createAppUser(notification);
      log.info("New AppUser(code:{}) created", appUser.getCode());
    } else if (appUser.isActive() == false) {
      reactivateAppUser(appUser, notification);
      log.info("AppUser(code:{}) reactivated", appUser.getCode());
    } else {
      log.warn("AppUser(code:{}) is already activated. From:{}, code:{}",
          notification.getMarketplace().getPartner(), appUser.getCode());
      return AppDirectApiResponse.builder()
          .success(false)
          .accountIdentifier(appUser.getCode())
          .errorCode(USER_ALREADY_EXISTS)
          .build();
    }

    return AppDirectApiResponse.builder()
        .success(true)
        .accountIdentifier(appUser.getCode())
        .build();
  }

  @Override
  public EventType getType() {
    return SUBSCRIPTION_ORDER;
  }

  private AppUser createAppUser(AppDirectNotification subscriptionOrder) {
    return appUserService.save(subscriptionOrder.getCreator().getUuid(),
                               SubscriptionType.valueOf(subscriptionOrder.getPayload().getOrder().getEditionCode()));
  }

  private void reactivateAppUser(AppUser appUser, AppDirectNotification subscriptionOrder) {
    appUser.setActive(true);
    appUser.setSubscriptionType(SubscriptionType.valueOf(subscriptionOrder.getPayload().getOrder().getEditionCode()));
  }
}
