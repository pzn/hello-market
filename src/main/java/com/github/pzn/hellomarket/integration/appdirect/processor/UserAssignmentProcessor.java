package com.github.pzn.hellomarket.integration.appdirect.processor;

import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.ACCOUNT_NOT_FOUND;
import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.MAX_USERS_REACHED;
import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.USER_ALREADY_EXISTS;
import static com.github.pzn.hellomarket.integration.appdirect.event.EventType.USER_ASSIGNMENT;

import com.github.pzn.hellomarket.integration.appdirect.AppDirectApiResponse;
import com.github.pzn.hellomarket.integration.appdirect.event.Account;
import com.github.pzn.hellomarket.integration.appdirect.event.AppDirectNotification;
import com.github.pzn.hellomarket.integration.appdirect.event.EventType;
import com.github.pzn.hellomarket.integration.appdirect.event.User;
import com.github.pzn.hellomarket.model.entity.AppOrg;
import com.github.pzn.hellomarket.model.entity.AppUser;
import com.github.pzn.hellomarket.repository.AppOrgRepository;
import com.github.pzn.hellomarket.repository.AppUserRepository;
import com.github.pzn.hellomarket.service.CodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserAssignmentProcessor implements AppDirectNotificationProcessor {

  private AppOrgRepository appOrgRepository;
  private AppUserRepository appUserRepository;
  private CodeService codeService;

  @Autowired
  public UserAssignmentProcessor(AppOrgRepository appOrgRepository,
                                 AppUserRepository appUserRepository,
                                 CodeService codeService) {
    this.appOrgRepository = appOrgRepository;
    this.appUserRepository = appUserRepository;
    this.codeService = codeService;
  }

  @Override
  public AppDirectApiResponse process(AppDirectNotification notification) throws NotificationProcessorException {

    AppOrg appOrg = getAppOrg(notification.getPayload().getAccount());
    if (appOrg == null) {
      log.warn("Organization(marketIdentifier:{}) from partner '{}' attempted to assign user, but organization not found in database",
          notification.getPayload().getAccount().getAccountIdentifier(), notification.getMarketplace().getPartner());
      return AppDirectApiResponse.builder()
          .success(false)
          .errorCode(ACCOUNT_NOT_FOUND)
          .build();
    }

    if(companyCannotAssignNewUsers(appOrg)) {
      log.warn("AppOrg(marketIdentifier:{}) from partner '{}' cannot assign new users (max limit)",
          appOrg.getMarketIdentifier(), notification.getMarketplace().getPartner());

      return AppDirectApiResponse.builder()
          .success(false)
          .accountIdentifier(appOrg.getCode())
          .errorCode(MAX_USERS_REACHED)
          .build();
    }

    User assignedUser = notification.getPayload().getUser();

    AppUser appUser = getAppUser(appOrg, assignedUser);
    if (appUser != null) {
      log.warn("AppUser(marketIdentifier:{}) from partner '{}' is already subscribed with AppOrg(marketIdentifier:{})",
          appUser.getMarketIdentifier(), notification.getMarketplace().getPartner(), appOrg.getMarketIdentifier());

      return AppDirectApiResponse.builder()
          .success(false)
          .accountIdentifier(appOrg.getCode())
          .userIdentifier(appUser.getCode())
          .errorCode(USER_ALREADY_EXISTS)
          .build();
    }

    AppUser newAppUser = createAppUser(assignedUser, appOrg);

    createNewUser(newAppUser);

    return AppDirectApiResponse.builder()
        .success(true)
        .accountIdentifier(appOrg.getCode())
        .userIdentifier(newAppUser.getCode())
        .build();
  }

  private AppOrg getAppOrg(Account account) {
    return appOrgRepository.findByCodeFetchUsers(account.getAccountIdentifier());
  }

  private AppUser getAppUser(AppOrg appOrg, User user) {
    return appUserRepository.findByMarketIdentifierAndAppOrgCode(user.getUuid(), appOrg.getCode());
  }

  private boolean companyCannotAssignNewUsers(AppOrg appOrg) {
    return appOrg.getSubscriptionType().getMaxUsers() < appOrg.getAppUsers().size() + 1;
  }

  private AppUser createAppUser(User assignedUser, AppOrg appOrg) {

    return AppUser.builder()
        .code(codeService.generateCode())
        .marketIdentifier(assignedUser.getUuid())

        .firstName(assignedUser.getFirstName())
        .lastName(assignedUser.getLastName())
        .openId(assignedUser.getOpenId())

        .appOrg(appOrg)

        .build();
  }

  private void createNewUser(AppUser appUser) {
    appUserRepository.save(appUser);
  }

  @Override
  public EventType getType() {
    return USER_ASSIGNMENT;
  }
}
