package com.github.pzn.hellomarket.integration.appdirect.processor;

import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.ACCOUNT_NOT_FOUND;
import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.CONFIGURATION_ERROR;
import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.MAX_USERS_REACHED;
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
import com.github.pzn.hellomarket.integration.appdirect.event.Order;
import com.github.pzn.hellomarket.integration.appdirect.event.Order.Item;
import com.github.pzn.hellomarket.integration.appdirect.event.Payload;
import com.github.pzn.hellomarket.model.entity.AppOrg;
import com.github.pzn.hellomarket.model.entity.AppUser;
import com.github.pzn.hellomarket.repository.AppOrgRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
  private static final Long CURRENT_MAX_USERS = 100L;
  private static final Long NEW_MAX_USERS = 101L;

  @InjectMocks
  private SubscriptionChangeProcessor processor;
  @Mock
  private AppOrgRepository appOrgRepository;
  @Captor
  private ArgumentCaptor<AppOrg> appOrgCaptor;

  @Test
  public void can_change_subscription() throws Exception {

    // Given
    assumeCompanyDoesExist(CURRENT_MAX_USERS, CURRENT_MAX_USERS);

    // Execute
    AppDirectApiResponse response = processor.process(aSubscriptionChange(APPORG_CODE, NEW_MAX_USERS));

    // Verify
    assertThat(response.isSuccess(), is(true));
    assertThat(response.getAccountIdentifier(), is(APPORG_CODE));
    assertThat(response.getErrorCode(), is(nullValue()));
    verify(appOrgRepository).findByCodeFetchUsers(eq(APPORG_CODE));
    verify(appOrgRepository).save(appOrgCaptor.capture());
    AppOrg capturedAppOrg = appOrgCaptor.getValue();
    assertThat(capturedAppOrg.getMaxUsers(), is(NEW_MAX_USERS));
  }

  @Test
  public void cannot_change_subscription_when_organization_not_found() throws Exception {

    // Execute
    AppDirectApiResponse response = processor.process(aSubscriptionChange("unknown_accountIdentifier", NEW_MAX_USERS));

    // Verify
    assertThat(response.isSuccess(), is(false));
    assertThat(response.getAccountIdentifier(), is(nullValue()));
    assertThat(response.getErrorCode(), is(ACCOUNT_NOT_FOUND));
    verify(appOrgRepository).findByCodeFetchUsers(eq("unknown_accountIdentifier"));
    verify(appOrgRepository, never()).save(any(AppOrg.class));
  }

  @Test
  public void cannot_change_subscription_when_user_quantity_not_available_in_payload() throws Exception {

    // Given
    assumeCompanyDoesExist(CURRENT_MAX_USERS, CURRENT_MAX_USERS);

    // Execute
    AppDirectApiResponse response = processor.process(aSubscriptionChange(APPORG_CODE, null));

    // Verify
    assertThat(response.isSuccess(), is(false));
    assertThat(response.getAccountIdentifier(), is(APPORG_CODE));
    assertThat(response.getErrorCode(), is(CONFIGURATION_ERROR));
    verify(appOrgRepository).findByCodeFetchUsers(eq(APPORG_CODE));
    verify(appOrgRepository, never()).save(any(AppOrg.class));
  }

  @Test
  public void cannot_change_subscription_when_apporg_user_base_is_larger_than_new_edition() throws Exception {

    // Given
    assumeCompanyDoesExist(CURRENT_MAX_USERS, CURRENT_MAX_USERS);

    // Execute
    AppDirectApiResponse response = processor.process(aSubscriptionChange(APPORG_CODE, -1 + CURRENT_MAX_USERS));

    // Verify
    assertThat(response.isSuccess(), is(false));
    assertThat(response.getAccountIdentifier(), is(APPORG_CODE));
    assertThat(response.getErrorCode(), is(MAX_USERS_REACHED));
    verify(appOrgRepository).findByCodeFetchUsers(eq(APPORG_CODE));
    verify(appOrgRepository, never()).save(any(AppOrg.class));
  }

  private void assumeCompanyDoesExist(long actualNumberOfUsers, long currentMaxUsers) {

    AppOrg appOrg = AppOrg.builder()
        .code(APPORG_CODE)
        .maxUsers(currentMaxUsers)
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

  public AppDirectNotification aSubscriptionChange(String appOrgCode, Long newNumberOfUsers) {

    List<Item> items = new ArrayList<>(1);
    if (newNumberOfUsers != null) {
      items.add(Item.builder().unit("USER").quantity(newNumberOfUsers.toString()).build());
    }

    return AppDirectNotification.builder()
        .type(SUBSCRIPTION_CHANGE)
        .marketplace(Marketplace.builder()
            .partner("partner").build())
        .payload(Payload.builder()
            .account(Account.builder()
                .accountIdentifier(appOrgCode).build())
            .order(Order.builder()
                .items(items).build()).build())
        .build();
  }
}
