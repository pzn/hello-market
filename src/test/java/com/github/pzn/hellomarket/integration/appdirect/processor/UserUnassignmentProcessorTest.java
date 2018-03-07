package com.github.pzn.hellomarket.integration.appdirect.processor;

import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.USER_NOT_FOUND;
import static com.github.pzn.hellomarket.integration.appdirect.event.EventType.USER_ASSIGNMENT;
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
import com.github.pzn.hellomarket.model.entity.AppUser;
import com.github.pzn.hellomarket.repository.AppUserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserUnassignmentProcessorTest {

  private static final String APPORG_CODE = "apporg_code";
  private static final String USER_UUID = "user_uuid";

  @InjectMocks
  private UserUnassignmentProcessor processor;
  @Mock
  private AppUserRepository appUserRepository;

  @Test
  public void can_unassign_user() throws Exception {

    // Given
    AppUser existingAppUser = assumeUserExists();

    // Execute
    AppDirectApiResponse response = processor.process(aUserAssignment());

    // Verify
    assertThat(response.isSuccess(), is(true));
    assertThat(response.getAccountIdentifier(), is(APPORG_CODE));
    assertThat(response.getErrorCode(), is(nullValue()));
    verify(appUserRepository).findByMarketIdentifierAndAppOrgCode(eq(USER_UUID), eq(APPORG_CODE));
    verify(appUserRepository).delete(eq(existingAppUser));
  }

  @Test
  public void when_user_not_found__should_return_bad_response() throws Exception {

    // Given
    assumeUserDoesNotExist();

    // Execute
    AppDirectApiResponse response = processor.process(aUserAssignment());

    // Verify
    assertThat(response.isSuccess(), is(false));
    assertThat(response.getAccountIdentifier(), is(nullValue()));
    assertThat(response.getErrorCode(), is(USER_NOT_FOUND));
    verify(appUserRepository).findByMarketIdentifierAndAppOrgCode(eq(USER_UUID), eq(APPORG_CODE));
    verify(appUserRepository, never()).delete(any(AppUser.class));
  }

  private AppUser assumeUserExists() {
    AppUser existingAppUser = new AppUser();
    when(appUserRepository.findByMarketIdentifierAndAppOrgCode(USER_UUID, APPORG_CODE))
        .thenReturn(existingAppUser);
    return existingAppUser;
  }

  private void assumeUserDoesNotExist() {
    when(appUserRepository.findByMarketIdentifierAndAppOrgCode(USER_UUID, APPORG_CODE))
        .thenReturn(null);
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
                .uuid(USER_UUID).build()).build())
        .build();
  }
}
