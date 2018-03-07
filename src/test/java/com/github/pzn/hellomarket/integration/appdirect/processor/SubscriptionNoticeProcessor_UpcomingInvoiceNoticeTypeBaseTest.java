package com.github.pzn.hellomarket.integration.appdirect.processor;

import static com.github.pzn.hellomarket.integration.appdirect.event.NoticeType.UPCOMING_INVOICE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.github.pzn.hellomarket.integration.appdirect.AppDirectApiResponse;
import com.github.pzn.hellomarket.integration.appdirect.event.NoticeType;
import com.github.pzn.hellomarket.model.entity.AppOrg;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionNoticeProcessor_UpcomingInvoiceNoticeTypeBaseTest extends SubscriptionNoticeProcessor_BaseTest {

  @Override
  protected NoticeType getNoticeType() {
    return UPCOMING_INVOICE;
  }

  @Test
  public void noop_on_subscription_notice_of_type_upcoming_invoice() throws Exception {

    // Given
    assumeCompanyDoesExist(true);

    // Execute
    AppDirectApiResponse response = processor.process(aSubscriptionNotice(APPORG_CODE, getNoticeType()));

    // Verify
    verifyApiResponse(response);
    verify(appOrgRepository).findByCode(eq(APPORG_CODE));
    verify(appOrgRepository, never()).delete(any(AppOrg.class));
    verify(appOrgRepository, never()).changeActiveStatus(anyLong(), anyBoolean());
  }
}
