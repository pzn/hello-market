package com.github.pzn.hellomarket.integration.appdirect.processor;

import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.ACCOUNT_NOT_FOUND;
import static com.github.pzn.hellomarket.integration.appdirect.event.EventType.SUBSCRIPTION_CANCEL;
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
import com.github.pzn.hellomarket.integration.appdirect.event.Payload;
import com.github.pzn.hellomarket.model.entity.AppOrg;
import com.github.pzn.hellomarket.repository.AppOrgRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionCancelProcessorTest {

  private static final String APPORG_CODE = "apporg_code";

  @InjectMocks
  private SubscriptionCancelProcessor processor;
  @Mock
  private AppOrgRepository appOrgRepository;

  @Test
  public void can_cancel_subscription() throws Exception {

    // Given
    AppOrg existingAppOrg = assumeCompanyDoesExist();

    // Execute
    AppDirectApiResponse response = processor.process(aSubscriptionCancel(APPORG_CODE));

    // Verify
    assertThat(response.isSuccess(), is(true));
    assertThat(response.getAccountIdentifier(), is(APPORG_CODE));
    assertThat(response.getErrorCode(), is(nullValue()));
    verify(appOrgRepository).findByCode(eq(APPORG_CODE));
    verify(appOrgRepository).delete(eq(existingAppOrg));
  }

  @Test
  public void cannot_cancel_subscription_when_organization_not_found() throws Exception {

    // Execute
    AppDirectApiResponse response = processor.process(aSubscriptionCancel("unknown_accountIdentifier"));

    // Verify
    assertThat(response.isSuccess(), is(false));
    assertThat(response.getAccountIdentifier(), is(nullValue()));
    assertThat(response.getErrorCode(), is(ACCOUNT_NOT_FOUND));
    verify(appOrgRepository).findByCode(eq("unknown_accountIdentifier"));
    verify(appOrgRepository, never()).delete(any(AppOrg.class));
  }

  private AppOrg assumeCompanyDoesExist() {

    AppOrg appOrg = AppOrg.builder().code(APPORG_CODE).build();
    when(appOrgRepository.findByCode(APPORG_CODE))
        .thenReturn(appOrg);
    return appOrg;
  }

  public AppDirectNotification aSubscriptionCancel(String appOrgCode) {

    return AppDirectNotification.builder()
        .type(SUBSCRIPTION_CANCEL)
        .marketplace(Marketplace.builder()
            .partner("partner").build())
        .payload(Payload.builder()
            .account(Account.builder()
                .accountIdentifier(appOrgCode).build()).build())
        .build();
  }
}
