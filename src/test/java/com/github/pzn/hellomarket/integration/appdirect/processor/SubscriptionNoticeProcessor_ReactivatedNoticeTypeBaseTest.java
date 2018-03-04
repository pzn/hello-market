package com.github.pzn.hellomarket.integration.appdirect.processor;

import static com.github.pzn.hellomarket.integration.appdirect.event.NoticeType.REACTIVATED;
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
public class SubscriptionNoticeProcessor_ReactivatedNoticeTypeBaseTest extends SubscriptionNoticeProcessor_BaseTest {

  @Override
  protected NoticeType getNoticeType() {
    return REACTIVATED;
  }

  @Test
  public void can_activate_an_deactivated_apporg_on_subscription_notice_of_type_reactivated() throws Exception {

    // Given
    assumeCompanyDoesExist(false);

    // Execute
    AppDirectApiResponse response = processor.process(aSubscriptionNotice(APPORG_CODE, getNoticeType()));

    // Verify
    assertThat(response.isSuccess(), is(true));
    assertThat(response.getAccountIdentifier(), is(APPORG_CODE));
    assertThat(response.getErrorCode(), is(nullValue()));
    verify(appOrgRepository).findByCode(eq(APPORG_CODE));
    verify(appOrgRepository, never()).delete(any(AppOrg.class));
    verify(appOrgRepository).changeActiveStatus(ID, true);
  }

  @Test
  public void noop_on_activated_apporg_on_subscription_notice_of_type_reactivated() throws Exception {

    // Given
    assumeCompanyDoesExist(true);

    // Execute
    AppDirectApiResponse response = processor.process(aSubscriptionNotice(APPORG_CODE, getNoticeType()));

    // Verify
    assertThat(response.isSuccess(), is(true));
    assertThat(response.getAccountIdentifier(), is(APPORG_CODE));
    assertThat(response.getErrorCode(), is(nullValue()));
    verify(appOrgRepository).findByCode(eq(APPORG_CODE));
    verify(appOrgRepository, never()).delete(any(AppOrg.class));
    verify(appOrgRepository, never()).changeActiveStatus(anyLong(), anyBoolean());
  }
}
