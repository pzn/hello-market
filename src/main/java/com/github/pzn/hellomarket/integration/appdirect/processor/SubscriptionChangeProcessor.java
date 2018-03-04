package com.github.pzn.hellomarket.integration.appdirect.processor;

import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.USER_NOT_FOUND;
import static com.github.pzn.hellomarket.integration.appdirect.event.EventType.SUBSCRIPTION_CHANGE;

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
public class SubscriptionChangeProcessor implements AppDirectNotificationProcessor {

  private AppUserService appUserService;

  @Autowired
  public SubscriptionChangeProcessor(AppUserService appUserService) {
    this.appUserService = appUserService;
  }

  @Override
  public AppDirectApiResponse process(AppDirectNotification notification) throws NotificationProcessorException {

    String code = notification.getPayload().getAccount().getAccountIdentifier();
    AppUser appUser = appUserService.findByCode(code);

    if (appUser == null) {
      log.warn("Cannot change AppUser type because account not found! From:{}, code:{}",
          notification.getMarketplace().getPartner(), code);
      return AppDirectApiResponse.builder()
          .success(false)
          .errorCode(USER_NOT_FOUND)
          .build();
    }

    SubscriptionType newSubscriptionType = extractSubscriptionType(notification);
    if (newSubscriptionType.equals(appUser.getSubscriptionType())) {
      log.info("AppUser(code:{}) already assigned to subscription type {}", appUser.getCode(), newSubscriptionType);
    } else {
      appUserService.changeSubscriptionType(appUser.getId(), newSubscriptionType);
      log.info("AppUser(code:{}) switched subscription type to {}", appUser.getCode(), newSubscriptionType);
    }

    return AppDirectApiResponse.builder()
        .success(true)
        .build();
  }

  @Override
  public EventType getType() {
    return SUBSCRIPTION_CHANGE;
  }

  private SubscriptionType extractSubscriptionType(AppDirectNotification notification) {
    return SubscriptionType.valueOf(notification.getPayload().getOrder().getEditionCode());
  }
}
