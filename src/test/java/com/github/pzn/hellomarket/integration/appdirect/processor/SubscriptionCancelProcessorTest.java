package com.github.pzn.hellomarket.integration.appdirect.processor;

import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.USER_NOT_FOUND;
import static com.github.pzn.hellomarket.integration.appdirect.event.EventType.SUBSCRIPTION_CANCEL;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
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
import com.github.pzn.hellomarket.service.AppUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionCancelProcessorTest {

  private static final Long ID = 1L;
  private static final String CODE = "code";

  @InjectMocks
  private SubscriptionCancelProcessor processor;
  @Mock
  private AppUserService appUserService;

  @Test
  public void can_process_subscription_cancel() throws Exception {

    // Given
    when(appUserService.findByCode(CODE))
        .thenReturn(anAppUser(true));

    // Execute
    AppDirectApiResponse response = processor.process(aSubscriptionCancel(CODE));

    // Verify
    assertThat(response.isSuccess(), is(true));
    verify(appUserService).findByCode(eq(CODE));
    verify(appUserService).changeStatus(eq(ID), eq(false));
  }

  @Test
  public void when_user_not_found__should_return_user_not_found_response() throws Exception {

    // Given
    when(appUserService.findByCode(CODE))
        .thenReturn(null);

    // Execute
    AppDirectApiResponse response = processor.process(aSubscriptionCancel(CODE));

    // Verify
    assertThat(response.isSuccess(), is(false));
    assertThat(response.getErrorCode(), is(USER_NOT_FOUND));
    assertThat(response.isSuccess(), is(false));
    verify(appUserService).findByCode(eq(CODE));
    verify(appUserService, never()).changeStatus(anyLong(), anyBoolean());
  }

//  @Test
  public void when_user_already_cancelled__should_return_ok() throws Exception {

    // Given
    when(appUserService.findByCode(CODE))
        .thenReturn(anAppUser(false));

    // Execute
    AppDirectApiResponse response = processor.process(aSubscriptionCancel(CODE));

    // Verify
    assertThat(response.isSuccess(), is(true));
    verify(appUserService).findByCode(eq(CODE));
    verify(appUserService, never()).changeStatus(anyLong(), anyBoolean());
  }

  private AppUser anAppUser(boolean isActive) {
    return AppUser.builder()
        .id(ID)
        .code(CODE)
        .active(isActive)
        .build();
  }

  public AppDirectNotification aSubscriptionCancel(String code) {
    return AppDirectNotification.builder()
        .type(SUBSCRIPTION_CANCEL)
        .marketplace(Marketplace.builder()
            .partner("partner")
            .build())
        .creator(User.builder().uuid("creator_uuid").build())
        .payload(Payload.builder()
            .account(Account.builder().accountIdentifier(code).build())
            .build())
        .build();
  }
}
