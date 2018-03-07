package com.github.pzn.hellomarket.integration.appdirect.processor;

import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.USER_NOT_FOUND;
import static com.github.pzn.hellomarket.integration.appdirect.event.EventType.USER_UNASSIGNMENT;

import com.github.pzn.hellomarket.integration.appdirect.AppDirectApiResponse;
import com.github.pzn.hellomarket.integration.appdirect.event.Account;
import com.github.pzn.hellomarket.integration.appdirect.event.AppDirectNotification;
import com.github.pzn.hellomarket.integration.appdirect.event.EventType;
import com.github.pzn.hellomarket.integration.appdirect.event.User;
import com.github.pzn.hellomarket.model.entity.AppUser;
import com.github.pzn.hellomarket.repository.AppUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserUnassignmentProcessor implements AppDirectNotificationProcessor {

  private AppUserRepository appUserRepository;

  @Autowired
  public UserUnassignmentProcessor(AppUserRepository appUserRepository) {
    this.appUserRepository = appUserRepository;
  }

  @Override
  public AppDirectApiResponse process(AppDirectNotification notification) throws NotificationProcessorException {

    AppUser appUser = getAppUser(notification.getPayload().getUser(), notification.getPayload().getAccount());
    if (appUser == null) {
      log.warn("Organization(code:{}) from partner '{}' attempted to unassign User(marketIdentifier:{}), but not found in database",
          notification.getPayload().getAccount().getAccountIdentifier(), notification.getMarketplace().getPartner(), notification.getPayload().getUser().getUuid());
      return AppDirectApiResponse.builder()
          .success(false)
          .errorCode(USER_NOT_FOUND)
          .build();
    }

    removeAppUser(appUser);

    return AppDirectApiResponse.builder()
        .success(true)
        .accountIdentifier(notification.getPayload().getAccount().getAccountIdentifier())
        .build();
  }

  private AppUser getAppUser(User user, Account account) {
    return appUserRepository.findByMarketIdentifierAndAppOrgCode(user.getUuid(), account.getAccountIdentifier());
  }

  private void removeAppUser(AppUser appUser) {
    appUserRepository.delete(appUser);
  }

  @Override
  public EventType getType() {
    return USER_UNASSIGNMENT;
  }
}
