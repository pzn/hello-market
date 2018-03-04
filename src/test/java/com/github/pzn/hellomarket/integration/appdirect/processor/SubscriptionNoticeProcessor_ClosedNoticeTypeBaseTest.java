package com.github.pzn.hellomarket.integration.appdirect.processor;

import static com.github.pzn.hellomarket.integration.appdirect.event.NoticeType.CLOSED;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.pzn.hellomarket.integration.appdirect.AppDirectApiResponse;
import com.github.pzn.hellomarket.integration.appdirect.event.NoticeType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionNoticeProcessor_ClosedNoticeTypeBaseTest extends
    SubscriptionNoticeProcessor_BaseTest {

  @Override
  protected NoticeType getNoticeType() {
    return CLOSED;
  }

  @Test
  public void can_process_subscription_notice_closed_when_user_is_active() throws Exception {

    // Given
    when(appUserService.findByCode(CODE))
        .thenReturn(anAppUser(true));

    // Execute
    AppDirectApiResponse response = processor.process(aSubscriptionNotice(CODE, getNoticeType()));

    // Verify
    assertThat(response.isSuccess(), is(true));
    verify(appUserService).findByCode(eq(CODE));
    verify(appUserService).changeStatus(eq(ID), eq(false));
  }

  @Test
  public void can_process_subscription_notice_closed_when_user_is_already_disabled() throws Exception {

    // Given
    when(appUserService.findByCode(CODE))
        .thenReturn(anAppUser(false));

    // Execute
    AppDirectApiResponse response = processor.process(aSubscriptionNotice(CODE, getNoticeType()));

    // Verify
    assertThat(response.isSuccess(), is(true));
    verify(appUserService).findByCode(eq(CODE));
    verify(appUserService, never()).changeStatus(anyLong(), anyBoolean());
  }
}
