package com.github.pzn.hellomarket.integration.appdirect.processor;

import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.ACCOUNT_NOT_FOUND;
import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.MAX_USERS_REACHED;
import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.USER_ALREADY_EXISTS;
import static com.github.pzn.hellomarket.integration.appdirect.event.EventType.USER_ASSIGNMENT;
import static com.github.pzn.hellomarket.model.entity.SubscriptionType.START_UP;
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
import com.github.pzn.hellomarket.integration.appdirect.event.User;
import com.github.pzn.hellomarket.model.entity.AppOrg;
import com.github.pzn.hellomarket.model.entity.AppUser;
import com.github.pzn.hellomarket.model.entity.SubscriptionType;
import com.github.pzn.hellomarket.repository.AppOrgRepository;
import com.github.pzn.hellomarket.repository.AppUserRepository;
import com.github.pzn.hellomarket.service.CodeService;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserAssignmentProcessorTest {

  private static final String APPORG_CODE = "apporg_code";

  private static final String APPUSER_CODE = "appuser_code";
  private static final String USER_UUID = "user_uuid";
  private static final String USER_FIRST_NAME = "user_first_name";
  private static final String USER_LAST_NAME = "user_last_name";
  private static final String USER_OPEN_ID = "user_open_id";

  private static final SubscriptionType SUBSCRIPTION_TYPE_OF_10_PEOPLE = START_UP;

  @InjectMocks
  private UserAssignmentProcessor processor;
  @Mock
  private AppOrgRepository appOrgRepository;
  @Mock
  private AppUserRepository appUserRepository;
  @Captor
  private ArgumentCaptor<AppUser> appUserCaptor;
  @Mock
  private CodeService codeService;

  @Before
  public void before() {
    when(codeService.generateCode())
        .thenReturn(APPUSER_CODE);
  }

  @Test
  public void can_assign_user() throws Exception {

    // Given
    AppOrg existingAppOrg = assumeCompanyDoesExist(SUBSCRIPTION_TYPE_OF_10_PEOPLE, 1);

    // Execute
    AppDirectApiResponse response = processor.process(aUserAssignment());

    // Verify
    assertThat(response.isSuccess(), is(true));
    assertThat(response.getAccountIdentifier(), is(APPORG_CODE));
    assertThat(response.getUserIdentifier(), is(APPUSER_CODE));
    assertThat(response.getErrorCode(), is(nullValue()));
    verify(appOrgRepository).findByCodeFetchUsers(eq(APPORG_CODE));
    verifySaveAppUserHasInteractions(existingAppOrg);
  }

  @Test
  public void when_unknown_apporg__should_return_bad_response() throws Exception {

    // Given
    assumeCompanyDoesNotExist();

    // Execute
    AppDirectApiResponse response = processor.process(aUserAssignment());

    // Verify
    assertThat(response.isSuccess(), is(false));
    assertThat(response.getAccountIdentifier(), is(nullValue()));
    assertThat(response.getUserIdentifier(), is(nullValue()));
    assertThat(response.getErrorCode(), is(ACCOUNT_NOT_FOUND));
    verify(appOrgRepository).findByCodeFetchUsers(eq(APPORG_CODE));
    verifySaveAppUserHasNoInteractions();
  }

  @Test
  public void when_cannot_assign_new_user__should_return_bad_response() throws Exception {

    // Given
    assumeCompanyDoesExist(SUBSCRIPTION_TYPE_OF_10_PEOPLE, 10);

    // Execute
    AppDirectApiResponse response = processor.process(aUserAssignment());

    // Verify
    assertThat(response.isSuccess(), is(false));
    assertThat(response.getAccountIdentifier(), is(APPORG_CODE));
    assertThat(response.getUserIdentifier(), is(nullValue()));
    assertThat(response.getErrorCode(), is(MAX_USERS_REACHED));
    verify(appOrgRepository).findByCodeFetchUsers(eq(APPORG_CODE));
    verifySaveAppUserHasNoInteractions();
  }

  @Test
  public void when_user_already_assigned__should_return_bad_response() throws Exception {

    // Given
    assumeCompanyDoesExist(SUBSCRIPTION_TYPE_OF_10_PEOPLE, 1);
    assumeUserAlreadySubscribed();

    // Execute
    AppDirectApiResponse response = processor.process(aUserAssignment());

    // Verify
    assertThat(response.isSuccess(), is(false));
    assertThat(response.getAccountIdentifier(), is(APPORG_CODE));
    assertThat(response.getUserIdentifier(), is(APPUSER_CODE));
    assertThat(response.getErrorCode(), is(USER_ALREADY_EXISTS));
    verify(appOrgRepository).findByCodeFetchUsers(eq(APPORG_CODE));
    verifySaveAppUserHasNoInteractions();
  }

  private AppOrg assumeCompanyDoesExist(SubscriptionType subscriptionType, int numberOfSubscribedUsers) {
    AppOrg appOrg = new AppOrg().builder()
        .code(APPORG_CODE)
        .subscriptionType(subscriptionType)
        .appUsers(someAppUsers(numberOfSubscribedUsers)).build();
    when(appOrgRepository.findByCodeFetchUsers(APPORG_CODE))
        .thenReturn(appOrg);
    return appOrg;
  }

  private void assumeCompanyDoesNotExist() {
    when(appOrgRepository.findByCodeFetchUsers(APPORG_CODE))
        .thenReturn(null);
  }

  private void assumeUserAlreadySubscribed() {
    when(appUserRepository.findByMarketIdentifierAndAppOrgCode(USER_UUID, APPORG_CODE))
        .thenReturn(AppUser.builder().marketIdentifier(USER_UUID).code(APPUSER_CODE).build());
  }

  private void verifySaveAppUserHasNoInteractions() {
    verify(appUserRepository, never()).save(any(AppUser.class));
  }

  private void verifySaveAppUserHasInteractions(AppOrg expectedAppOrg) {

    verify(appUserRepository).save(appUserCaptor.capture());
    AppUser capturedAppUser = appUserCaptor.getValue();

    assertThat(capturedAppUser.getCode(), is(APPUSER_CODE));
    assertThat(capturedAppUser.getMarketIdentifier(), is(USER_UUID));
    assertThat(capturedAppUser.getFirstName(), is(USER_FIRST_NAME));
    assertThat(capturedAppUser.getLastName(), is(USER_LAST_NAME));
    assertThat(capturedAppUser.getOpenId(), is(USER_OPEN_ID));
    assertThat(capturedAppUser.getAppOrg(), is(expectedAppOrg));
  }

  private Set<AppUser> someAppUsers(long quantity) {
    Set<AppUser> appUsers = new HashSet<>();
    for (int i = 0; i < quantity; i++) {
      appUsers.add(AppUser.builder().id(Long.valueOf(i)).build());
    }
    return appUsers;
  }

  public AppDirectNotification aUserAssignment() {

    return AppDirectNotification.builder()
        .type(USER_ASSIGNMENT)
        .marketplace(Marketplace.builder()
            .partner("partner").build())
        .payload(Payload.builder()
            .account(Account.builder()
                .accountIdentifier(APPORG_CODE).build())
            .user(User.builder()
                .uuid(USER_UUID)
                .firstName(USER_FIRST_NAME)
                .lastName(USER_LAST_NAME)
                .openId(USER_OPEN_ID).build()).build())
        .build();
  }
}
