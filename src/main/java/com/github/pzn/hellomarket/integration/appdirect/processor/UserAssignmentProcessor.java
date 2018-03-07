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

    AppOrg appOrg = retrieveAppOrg(notification.getPayload().getAccount());

    companyCanAssignNewUsers(appOrg);

    User assignedUser = notification.getPayload().getUser();

    userMustNotExist(appOrg, assignedUser);

    AppUser newAppUser = createAppUser(assignedUser, appOrg);

    createNewUser(newAppUser);

    return AppDirectApiResponse.success(appOrg.getCode(), newAppUser.getCode());
  }

  private AppOrg retrieveAppOrg(Account account) {

    String accountIdentifier = account.getAccountIdentifier();

    AppOrg appOrg = appOrgRepository.findByCodeFetchUsers(accountIdentifier);
    if (appOrg != null) {
      return appOrg;
    }

    throw new NotificationProcessorException(ACCOUNT_NOT_FOUND, accountIdentifier, null,
        String.format("Cannot find company(accountIdentifier:%s)", accountIdentifier));
  }

  private void companyCanAssignNewUsers(AppOrg appOrg) {

    if (appOrg.getAppUsers().size() + 1 < appOrg.getSubscriptionType().getMaxUsers()) {
      return;
    }

    throw new NotificationProcessorException(MAX_USERS_REACHED, appOrg.getCode(), null,
        String.format("Company(accountIdentifier:%s) cannot allow more users", appOrg.getCode()));
  }

  private void userMustNotExist(AppOrg appOrg, User user) {

    AppUser appUser = appUserRepository
        .findByMarketIdentifierAndAppOrgCode(user.getUuid(), appOrg.getCode());
    if (appUser == null) {
      return;
    }

    throw new NotificationProcessorException(USER_ALREADY_EXISTS, appOrg.getCode(),
        appUser.getCode(),
        String.format(
            "User(userIdentifier:%s) is already subscribed with the subscription of company(accountIdentifier:%s)",
            appOrg.getCode(), appUser.getCode()));
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
