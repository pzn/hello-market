package com.github.pzn.hellomarket.integration.appdirect.processor;

import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.CONFIGURATION_ERROR;
import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.USER_ALREADY_EXISTS;
import static com.github.pzn.hellomarket.integration.appdirect.event.EventType.SUBSCRIPTION_ORDER;

import com.github.pzn.hellomarket.integration.appdirect.AppDirectApiResponse;
import com.github.pzn.hellomarket.integration.appdirect.event.AppDirectNotification;
import com.github.pzn.hellomarket.integration.appdirect.event.Company;
import com.github.pzn.hellomarket.integration.appdirect.event.EventType;
import com.github.pzn.hellomarket.integration.appdirect.event.Order;
import com.github.pzn.hellomarket.integration.appdirect.event.Payload;
import com.github.pzn.hellomarket.integration.appdirect.event.User;
import com.github.pzn.hellomarket.model.entity.AppOrg;
import com.github.pzn.hellomarket.model.entity.AppUser;
import com.github.pzn.hellomarket.model.entity.SubscriptionType;
import com.github.pzn.hellomarket.repository.AppOrgRepository;
import com.github.pzn.hellomarket.repository.AppUserRepository;
import com.github.pzn.hellomarket.service.CodeService;
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

    companyMustNotExist(company);

    SubscriptionType subscriptionType = extractNewSubscriptionType(notification.getPayload());

    AppOrg appOrg = createAppOrg(company, subscriptionType);

    AppUser appUser = createAppUser(notification.getCreator(), appOrg);

    createNewSubscription(appOrg, appUser);

    return AppDirectApiResponse.success(appOrg.getCode(), appUser.getCode());
  }

  private void companyMustNotExist(Company company) {

    AppOrg appOrg = appOrgRepository.findByMarketIdentifier(company.getUuid());
    if (appOrg == null) {
      return;
    }

    throw new NotificationProcessorException(USER_ALREADY_EXISTS, appOrg.getCode(), null,
        String.format("Company(accountIdentifier:%s) already subscribed", appOrg.getCode()));
  }

  private SubscriptionType extractNewSubscriptionType(Payload payload) {

    String editionCode = payload.getOrder().getEditionCode();

    try {
      return SubscriptionType.valueOf(editionCode);
    } catch (Exception e) {
      throw new NotificationProcessorException(CONFIGURATION_ERROR, null, null,
          String.format("Unrecognized Edition(code:{})", editionCode));
    }
  }

  private SubscriptionType retrieveNewSubscriptionType(Order order) {
    return SubscriptionType.valueOf(order.getEditionCode());
  }

  private AppOrg createAppOrg(Company company, SubscriptionType subscriptionType) {

    return AppOrg.builder()
        .code(codeService.generateCode())
        .marketIdentifier(company.getUuid())
        .active(true)
        .subscriptionType(subscriptionType)

        .name(company.getName())
        .country(company.getCountry())

        .build();
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

  @Override
  public EventType getType() {
    return SUBSCRIPTION_ORDER;
  }
}
