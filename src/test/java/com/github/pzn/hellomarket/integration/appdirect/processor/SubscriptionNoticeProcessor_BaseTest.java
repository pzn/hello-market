package com.github.pzn.hellomarket.integration.appdirect.processor;

import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.ACCOUNT_NOT_FOUND;
import static com.github.pzn.hellomarket.integration.appdirect.event.EventType.SUBSCRIPTION_CHANGE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.pzn.hellomarket.integration.appdirect.AppDirectApiResponse;
import com.github.pzn.hellomarket.integration.appdirect.event.Account;
import com.github.pzn.hellomarket.integration.appdirect.event.AppDirectNotification;
import com.github.pzn.hellomarket.integration.appdirect.event.Marketplace;
import com.github.pzn.hellomarket.integration.appdirect.event.NoticeType;
import com.github.pzn.hellomarket.integration.appdirect.event.Payload;
import com.github.pzn.hellomarket.model.entity.AppOrg;
import com.github.pzn.hellomarket.repository.AppOrgRepository;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public abstract class SubscriptionNoticeProcessor_BaseTest {

  protected static final Long ID = 1L;
  protected static final String APPORG_CODE = "apporg_code";

  @InjectMocks
  protected SubscriptionNoticeProcessor processor;
  @Mock
  protected AppOrgRepository appOrgRepository;

  @Test
  public void cannot_change_subscription_when_organization_not_found() throws Exception {

    // Execute
    AppDirectApiResponse response = processor.process(aSubscriptionNotice("unknown_accountIdentifier", getNoticeType()));

    // Verify
    assertThat(response.isSuccess(), is(false));
    assertThat(response.getAccountIdentifier(), is(nullValue()));
    assertThat(response.getErrorCode(), is(ACCOUNT_NOT_FOUND));
    verify(appOrgRepository).findByCode(eq("unknown_accountIdentifier"));
    verify(appOrgRepository, never()).save(any(AppOrg.class));
  }

  protected AppOrg assumeCompanyDoesExist(boolean isActive) {
    AppOrg appOrg = AppOrg.builder()
        .id(ID)
        .code(APPORG_CODE)
        .active(isActive).build();
    when(appOrgRepository.findByCode(APPORG_CODE))
        .thenReturn(appOrg);
    return appOrg;
  }

  protected AppDirectNotification aSubscriptionNotice(String accountIdentifier, NoticeType noticeType) {
    return AppDirectNotification.builder()
        .type(SUBSCRIPTION_CHANGE)
        .marketplace(Marketplace.builder()
            .partner("partner").build())
        .payload(Payload.builder()
            .type(noticeType)
            .account(Account.builder()
                .accountIdentifier(accountIdentifier).build()).build())
        .build();
  }

  protected abstract NoticeType getNoticeType();
}
