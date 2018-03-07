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
import java.util.Optional;
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

    Account account = notification.getPayload().getAccount();

    AppUser appUser = retrieveAppUser(notification.getPayload().getUser(), account);

    removeAppUser(appUser);

    return AppDirectApiResponse.success(account.getAccountIdentifier());
  }

  private AppUser retrieveAppUser(User user, Account account) {

    AppUser appUser = appUserRepository.findByMarketIdentifierAndAppOrgCode(user.getUuid(), account.getAccountIdentifier());
    if (appUser != null) {
      return appUser;
    }

    throw new NotificationProcessorException(USER_NOT_FOUND,
                                             account.getAccountIdentifier(),
                                             null,
                                             String.format("Cannot find user(uuid:%s) for company(accountIdentifier:%s)",
                                                 user.getUuid(), account.getAccountIdentifier()));
  }

  private void removeAppUser(AppUser appUser) {
    appUserRepository.delete(appUser);
  }

  @Override
  public EventType getType() {
    return USER_UNASSIGNMENT;
  }
}
