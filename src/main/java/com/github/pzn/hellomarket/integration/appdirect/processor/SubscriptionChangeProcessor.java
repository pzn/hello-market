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
import com.github.pzn.hellomarket.integration.appdirect.event.Order.Item;
import com.github.pzn.hellomarket.model.entity.AppOrg;
import com.github.pzn.hellomarket.repository.AppOrgRepository;
import java.util.Optional;
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

    Long newUserQuantity = retrieveUserQuantity(notification.getPayload().getOrder());
    if (newUserQuantity == null) {
      log.warn("Unrecognized Edition(code:{}) from partner '{}' (edition has no number of users)",
          notification.getPayload().getOrder().getEditionCode(), notification.getMarketplace().getPartner());

      return AppDirectApiResponse.builder()
          .success(false)
          .accountIdentifier(appOrg.getCode())
          .errorCode(CONFIGURATION_ERROR)
          .build();
    }

    if (newUserQuantity < appOrg.getAppUsers().size()) {
      log.info("Organization(code:{}, marketIdentifier:{}) from partner '{}' attempted to change subscription, but its userbase won't fit in the new one",
          appOrg.getCode(), appOrg.getMarketIdentifier(), notification.getMarketplace().getPartner());

      return AppDirectApiResponse.builder()
          .success(false)
          .accountIdentifier(appOrg.getCode())
          .errorCode(MAX_USERS_REACHED)
          .build();
    }

    updateAppOrgWithNewMaxUsers(appOrg, newUserQuantity);

    return AppDirectApiResponse.builder()
        .success(true)
        .accountIdentifier(appOrg.getCode())
        .build();
  }

  private AppOrg getAppOrg(Account account) {
    return appOrgRepository.findByCodeFetchUsers(account.getAccountIdentifier());
  }

  @Override
  public EventType getType() {
    return SUBSCRIPTION_CHANGE;
  }

  private Long retrieveUserQuantity(Order order) {
    Optional<Item> item = order.getItems().stream()
        .filter(i -> "USER".equals(i.getUnit()))
        .findFirst();
    if (item.isPresent()) {
      return Long.parseLong(item.get().getQuantity());
    }
    return null;
  }

  private void updateAppOrgWithNewMaxUsers(AppOrg appOrg, Long newUserQuantity) {
    appOrg.setMaxUsers(newUserQuantity);
    appOrgRepository.save(appOrg);
  }
}
