package com.github.pzn.hellomarket.integration.appdirect.processor;

import static com.github.pzn.hellomarket.integration.appdirect.event.NoticeType.UPCOMING_INVOICE;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.github.pzn.hellomarket.integration.appdirect.AppDirectApiResponse;
import com.github.pzn.hellomarket.integration.appdirect.event.NoticeType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionNoticeProcessor_UpcomingInvoiceNoticeTypeBaseTest extends
    SubscriptionNoticeProcessor_BaseTest {

  @Override
  protected NoticeType getNoticeType() {
    return UPCOMING_INVOICE;
  }

  @Test
  public void no_op__when_subscription_notice_deactivated() throws Exception {

    // Given
    when(appUserService.findByCode(CODE))
        .thenReturn(anAppUser(true));

    // Execute
    AppDirectApiResponse response = processor.process(aSubscriptionNotice(CODE, getNoticeType()));

    // Verify
    assertThat(response.isSuccess(), is(true));
    verify(appUserService).findByCode(eq(CODE));
    verifyNoMoreInteractions(appUserService);
  }
}
