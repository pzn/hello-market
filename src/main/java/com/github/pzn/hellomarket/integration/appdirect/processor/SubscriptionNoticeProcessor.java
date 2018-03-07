package com.github.pzn.hellomarket.integration.appdirect.processor;

import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.ACCOUNT_NOT_FOUND;
import static com.github.pzn.hellomarket.integration.appdirect.event.EventType.SUBSCRIPTION_NOTICE;

import com.github.pzn.hellomarket.integration.appdirect.AppDirectApiResponse;
import com.github.pzn.hellomarket.integration.appdirect.event.Account;
import com.github.pzn.hellomarket.integration.appdirect.event.AppDirectNotification;
import com.github.pzn.hellomarket.integration.appdirect.event.EventType;
import com.github.pzn.hellomarket.integration.appdirect.event.NoticeType;
import com.github.pzn.hellomarket.model.entity.AppOrg;
import com.github.pzn.hellomarket.repository.AppOrgRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SubscriptionNoticeProcessor implements AppDirectNotificationProcessor {

  private AppOrgRepository appOrgRepository;

  @Autowired
  public SubscriptionNoticeProcessor(AppOrgRepository appOrgRepository) {
    this.appOrgRepository = appOrgRepository;
  }

  @Override
  public AppDirectApiResponse process(AppDirectNotification notification) throws NotificationProcessorException {

    AppOrg appOrg = retrieveAppOrg(notification.getPayload().getAccount());

    processNotice(notification.getPayload().getType(), appOrg);

    return AppDirectApiResponse.success(appOrg.getCode());
  }

  private AppOrg retrieveAppOrg(Account account) {

    String accountIdentifier = account.getAccountIdentifier();

    AppOrg appOrg = appOrgRepository.findByCode(accountIdentifier);
    if (appOrg != null) {
      return appOrg;
    }

    throw new NotificationProcessorException(ACCOUNT_NOT_FOUND, accountIdentifier, null,
        String.format("Cannot find company(accountIdentifier:%s)", accountIdentifier));
  }

  private void processNotice(NoticeType noticeType, AppOrg appOrg) {

    switch (noticeType) {
      case CLOSED:
        removeAppOrg(appOrg);
        break;
      case DEACTIVATED:
        changeStatus(appOrg, false);
        break;
      case REACTIVATED:
        changeStatus(appOrg, true);
        break;
      case UPCOMING_INVOICE:
      default:
        break;
    }
  }

  private void removeAppOrg(AppOrg appOrg) {
    appOrgRepository.delete(appOrg);
  }

  private void changeStatus(AppOrg appOrg, boolean newActiveStatus) {

    if (appOrg.getActive() != newActiveStatus) {
      appOrgRepository.changeActiveStatus(appOrg.getId(), newActiveStatus);
      log.info("Company(accontIdentifier:{}) as been {}", appOrg.getCode(),
          newActiveStatus ? "enabled" : "disabled");
    } else {
      log.info("Company(accontIdentifier:{}) already {}", appOrg.getCode(),
          newActiveStatus ? "enabled" : "disabled");
    }
  }

  @Override
  public EventType getType() {
    return SUBSCRIPTION_NOTICE;
  }
}
