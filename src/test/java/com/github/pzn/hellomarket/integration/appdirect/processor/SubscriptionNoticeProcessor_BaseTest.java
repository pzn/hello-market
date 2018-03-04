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
import com.github.pzn.hellomarket.integration.appdirect.event.NoticeType;
import com.github.pzn.hellomarket.integration.appdirect.event.Payload;
import com.github.pzn.hellomarket.integration.appdirect.event.User;
import com.github.pzn.hellomarket.model.entity.AppUser;
import com.github.pzn.hellomarket.service.AppUserService;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public abstract class SubscriptionNoticeProcessor_BaseTest {

  protected static final Long ID = 1L;
  protected static final String CODE = "code";

  @InjectMocks
  protected SubscriptionNoticeProcessor processor;
  @Mock
  protected AppUserService appUserService;

  @Test
  public void when_user_not_found__should_return_user_not_found_response() throws Exception {

    // Given
    when(appUserService.findByCode(CODE))
        .thenReturn(null);

    // Execute
    AppDirectApiResponse response = processor.process(aSubscriptionNotice(CODE, getNoticeType()));

    // Verify
    assertThat(response.isSuccess(), is(false));
    assertThat(response.getErrorCode(), is(USER_NOT_FOUND));
    assertThat(response.isSuccess(), is(false));
    verify(appUserService).findByCode(eq(CODE));
    verify(appUserService, never()).changeStatus(anyLong(), anyBoolean());
  }

  protected AppUser anAppUser(boolean isActive) {
    return AppUser.builder()
        .id(ID)
        .code(CODE)
        .active(isActive)
        .build();
  }

  protected AppDirectNotification aSubscriptionNotice(String code, NoticeType noticeType) {
    return AppDirectNotification.builder()
        .type(SUBSCRIPTION_CANCEL)
        .marketplace(Marketplace.builder()
            .partner("partner")
            .build())
        .creator(User.builder().uuid("creator_uuid").build())
        .payload(Payload.builder()
            .account(Account.builder().accountIdentifier(code).build())
            .type(noticeType)
            .build())
        .build();
  }

  protected abstract NoticeType getNoticeType();
}
