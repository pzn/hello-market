package com.github.pzn.hellomarket.integration.appdirect.processor;

import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.ACCOUNT_NOT_FOUND;
import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.CONFIGURATION_ERROR;
import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.MAX_USERS_REACHED;
import static com.github.pzn.hellomarket.integration.appdirect.event.EventType.SUBSCRIPTION_CHANGE;
import static com.github.pzn.hellomarket.model.entity.SubscriptionType.LARGE;
import static com.github.pzn.hellomarket.model.entity.SubscriptionType.SINGLE_USER;
import static com.github.pzn.hellomarket.model.entity.SubscriptionType.SMALL;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.pzn.hellomarket.integration.appdirect.AppDirectApiResponse;
import com.github.pzn.hellomarket.integration.appdirect.event.Account;
import com.github.pzn.hellomarket.integration.appdirect.event.AppDirectNotification;
import com.github.pzn.hellomarket.integration.appdirect.event.Marketplace;
import com.github.pzn.hellomarket.integration.appdirect.event.Order;
import com.github.pzn.hellomarket.integration.appdirect.event.Payload;
import com.github.pzn.hellomarket.model.entity.AppOrg;
import com.github.pzn.hellomarket.model.entity.AppUser;
import com.github.pzn.hellomarket.model.entity.SubscriptionType;
import com.github.pzn.hellomarket.repository.AppOrgRepository;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionChangeProcessorTest {

  private static final String APPORG_CODE = "apporg_code";
  private static final SubscriptionType CURRENT_SUBSCRIPTION_TYPE = SMALL;
  private static final SubscriptionType NEW_SUBSCRIPTION_TYPE = LARGE;

  @InjectMocks
  private SubscriptionChangeProcessor processor;
  @Mock
  private AppOrgRepository appOrgRepository;
  @Captor
  private ArgumentCaptor<AppOrg> appOrgCaptor;

  @Test
  public void can_change_subscription() throws Exception {

    // Given
    assumeCompanyDoesExist(CURRENT_SUBSCRIPTION_TYPE.getMaxUsers(), CURRENT_SUBSCRIPTION_TYPE);

    // Execute
    AppDirectApiResponse response = processor.process(aSubscriptionChange(APPORG_CODE, NEW_SUBSCRIPTION_TYPE.toString()));

    // Verify
    assertThat(response.isSuccess(), is(true));
    assertThat(response.getAccountIdentifier(), is(APPORG_CODE));
    assertThat(response.getUserIdentifier(), is(nullValue()));
    assertThat(response.getErrorCode(), is(nullValue()));
    assertThat(response.getMessage(), is(nullValue()));

    verify(appOrgRepository).findByCodeFetchUsers(eq(APPORG_CODE));
    verify(appOrgRepository).save(appOrgCaptor.capture());
    AppOrg capturedAppOrg = appOrgCaptor.getValue();
    assertThat(capturedAppOrg.getSubscriptionType(), is(NEW_SUBSCRIPTION_TYPE));
  }

  @Test
  public void cannot_change_subscription_when_company_not_found() throws Exception {

    // Execute
    try {
      processor.process(aSubscriptionChange("unknown_accountIdentifier", NEW_SUBSCRIPTION_TYPE.toString()));
    } catch (NotificationProcessorException e) {

      // Verify
      assertThat(e.getErrorCode(), is(ACCOUNT_NOT_FOUND));
      assertThat(e.getAccountIdentifier(), is("unknown_accountIdentifier"));
      assertThat(e.getUserIdentifier(), is(nullValue()));
      assertThat(e.getMessage(), is(notNullValue()));

      verify(appOrgRepository).findByCodeFetchUsers(eq("unknown_accountIdentifier"));
      verify(appOrgRepository, never()).save(any(AppOrg.class));
      return;
    }
    fail("should throw a NotificationProcessorException!");
  }

  @Test
  public void cannot_change_subscription_when_edition_code_not_available_in_payload() throws Exception {
    cannot_change_subscription_when_edition_code_is(null);
  }

  @Test
  public void cannot_change_subscription_when_unrecognized_edition_code() throws Exception {
    cannot_change_subscription_when_edition_code_is("UNICORN");
  }

  public void cannot_change_subscription_when_edition_code_is(String editionCode) throws Exception {

    // Given
    assumeCompanyDoesExist(CURRENT_SUBSCRIPTION_TYPE.getMaxUsers(), CURRENT_SUBSCRIPTION_TYPE);

    // Execute
    try {
      processor.process(aSubscriptionChange(APPORG_CODE, editionCode));
    } catch (NotificationProcessorException e) {

      // Verify
      assertThat(e.getErrorCode(), is(CONFIGURATION_ERROR));
      assertThat(e.getAccountIdentifier(), is(APPORG_CODE));
      assertThat(e.getUserIdentifier(), is(nullValue()));
      assertThat(e.getMessage(), is(notNullValue()));

      verify(appOrgRepository).findByCodeFetchUsers(eq(APPORG_CODE));
      verify(appOrgRepository, never()).save(any(AppOrg.class));
      return;
    }
    fail("should throw a NotificationProcessorException!");
  }

  @Test
  public void cannot_change_subscription_when_company_cannot_allow_more_users() throws Exception {

    // Given
    assumeCompanyDoesExist(CURRENT_SUBSCRIPTION_TYPE.getMaxUsers(), CURRENT_SUBSCRIPTION_TYPE);

    // Execute
    try {
      processor.process(aSubscriptionChange(APPORG_CODE, SINGLE_USER.toString()));
    } catch (NotificationProcessorException e) {

      // Verify
      assertThat(e.getErrorCode(), is(MAX_USERS_REACHED));
      assertThat(e.getAccountIdentifier(), is(APPORG_CODE));
      assertThat(e.getUserIdentifier(), is(nullValue()));
      assertThat(e.getMessage(), is(notNullValue()));

      verify(appOrgRepository).findByCodeFetchUsers(eq(APPORG_CODE));
      verify(appOrgRepository, never()).save(any(AppOrg.class));
      return;
    }
    fail("should throw a NotificationProcessorException!");
  }

  private void assumeCompanyDoesExist(long actualNumberOfUsers, SubscriptionType currentSubscriptionType) {

    AppOrg appOrg = AppOrg.builder()
        .code(APPORG_CODE)
        .subscriptionType(currentSubscriptionType)
        .appUsers(someAppUsers(actualNumberOfUsers)).build();
    when(appOrgRepository.findByCodeFetchUsers(APPORG_CODE))
        .thenReturn(appOrg);
  }

  private Set<AppUser> someAppUsers(long quantity) {
    Set<AppUser> appUsers = new HashSet<>();
    for (int i = 0; i < quantity; i++) {
      appUsers.add(AppUser.builder().id(Long.valueOf(i)).build());
    }
    return appUsers;
  }

  public AppDirectNotification aSubscriptionChange(String appOrgCode, String newEditionCode) {

    return AppDirectNotification.builder()
        .type(SUBSCRIPTION_CHANGE)
        .marketplace(Marketplace.builder()
            .partner("partner").build())
        .payload(Payload.builder()
            .account(Account.builder()
                .accountIdentifier(appOrgCode).build())
            .order(Order.builder()
                .editionCode(newEditionCode).build()).build())
        .build();
  }
}
