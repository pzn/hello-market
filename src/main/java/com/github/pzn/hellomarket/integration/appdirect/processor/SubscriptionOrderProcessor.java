package com.github.pzn.hellomarket.integration.appdirect.processor;

import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.USER_ALREADY_EXISTS;
import static com.github.pzn.hellomarket.integration.appdirect.event.EventType.SUBSCRIPTION_ORDER;

import com.github.pzn.hellomarket.integration.appdirect.AppDirectApiResponse;
import com.github.pzn.hellomarket.integration.appdirect.event.AppDirectNotification;
import com.github.pzn.hellomarket.integration.appdirect.event.Company;
import com.github.pzn.hellomarket.integration.appdirect.event.EventType;
import com.github.pzn.hellomarket.integration.appdirect.event.Order;
import com.github.pzn.hellomarket.integration.appdirect.event.Order.Item;
import com.github.pzn.hellomarket.integration.appdirect.event.User;
import com.github.pzn.hellomarket.model.entity.AppOrg;
import com.github.pzn.hellomarket.model.entity.AppUser;
import com.github.pzn.hellomarket.repository.AppOrgRepository;
import com.github.pzn.hellomarket.repository.AppUserRepository;
import com.github.pzn.hellomarket.service.CodeService;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class SubscriptionOrderProcessor implements AppDirectNotificationProcessor {

  private AppOrgRepository appOrgRepository;
  private AppUserRepository appUserRepository;
  private CodeService codeService;

  @Autowired
  public SubscriptionOrderProcessor(AppOrgRepository appOrgRepository,
                                    AppUserRepository appUserRepository,
                                    CodeService codeService) {
    this.appOrgRepository = appOrgRepository;
    this.appUserRepository = appUserRepository;
    this.codeService = codeService;
  }

  @Override
  @Transactional
  public AppDirectApiResponse process(AppDirectNotification notification) throws NotificationProcessorException {

    Company company = notification.getPayload().getCompany();
    if (!companyAlreadySubscribed(company)) {
      log.warn("AppOrg(marketIdentifier:{}) from partner '{}' is already subscribed",
          company.getUuid(), notification.getMarketplace().getPartner());
      return AppDirectApiResponse.builder()
          .success(false)
          .accountIdentifier(company.getUuid())
          .errorCode(USER_ALREADY_EXISTS)
          .build();
    }

    AppOrg appOrg = createAppOrg(company, notification.getPayload().getOrder());

    AppUser appUser = createAppUser(notification.getCreator(), appOrg);

    createNewSubscription(appOrg, appUser);

    return AppDirectApiResponse.builder()
        .success(true)
        .accountIdentifier(appOrg.getCode())
        .userIdentifier(appUser.getCode())
        .build();
  }

  @Override
  public EventType getType() {
    return SUBSCRIPTION_ORDER;
  }

  private boolean companyAlreadySubscribed(Company company) {
    return null == appOrgRepository.findByMarketIdentifier(company.getUuid());
  }

  private AppOrg createAppOrg(Company company, Order order) {

    return AppOrg.builder()
        .code(codeService.generateCode())
        .marketIdentifier(company.getUuid())
        .active(true)
        .maxUsers(retrieveUserQuantity(order))

        .name(company.getName())
        .country(company.getCountry())

        .build();
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

  private AppUser createAppUser(User orderCreator, AppOrg appOrg) {

    return AppUser.builder()
        .code(codeService.generateCode())
        .marketIdentifier(orderCreator.getUuid())

        .firstName(orderCreator.getFirstName())
        .lastName(orderCreator.getLastName())
        .openId(orderCreator.getOpenId())

        .appOrg(appOrg)

        .build();
  }

  private void createNewSubscription(AppOrg appOrg, AppUser appUser) {
    appOrgRepository.save(appOrg);
    appUserRepository.save(appUser);
  }
}
