package com.github.pzn.hellomarket.integration.appdirect.processor;

import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.USER_ALREADY_EXISTS;
import static com.github.pzn.hellomarket.integration.appdirect.event.EventType.SUBSCRIPTION_ORDER;
import static com.github.pzn.hellomarket.model.entity.SubscriptionType.LIFE;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.pzn.hellomarket.integration.appdirect.AppDirectApiResponse;
import com.github.pzn.hellomarket.integration.appdirect.event.AppDirectNotification;
import com.github.pzn.hellomarket.integration.appdirect.event.Marketplace;
import com.github.pzn.hellomarket.integration.appdirect.event.Order;
import com.github.pzn.hellomarket.integration.appdirect.event.Payload;
import com.github.pzn.hellomarket.integration.appdirect.event.User;
import com.github.pzn.hellomarket.model.entity.AppUser;
import com.github.pzn.hellomarket.model.entity.SubscriptionType;
import com.github.pzn.hellomarket.service.AppUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionOrderProcessorTest {

  private static final String CODE = "code";
  private static final String CREATOR_UUID = "creator_uuid";
  private static final String MARKET_ACCOUNT_IDENTIFIER = "market_acccount_identifier";
  private static final SubscriptionType SUBSCRIPTION_TYPE = LIFE;

  @InjectMocks
  private SubscriptionOrderProcessor processor;
  @Mock
  private AppUserService appUserService;

  @Test
  public void can_subscribe_user() throws Exception {

    // Given
    when(appUserService.findByMarketAccountIdentifier(CREATOR_UUID))
        .thenReturn(null);
    when(appUserService.save(CREATOR_UUID, SUBSCRIPTION_TYPE))
        .thenReturn(anAppUser(CODE, MARKET_ACCOUNT_IDENTIFIER));

    // Execute
    AppDirectApiResponse response = processor.process(aSubscriptionOrder(CREATOR_UUID, SUBSCRIPTION_TYPE));

    // Verify
    assertThat(response.isSuccess(), is(true));
    assertThat(response.getAccountIdentifier(), is(CODE));
    verify(appUserService).findByMarketAccountIdentifier(eq(CREATOR_UUID));
    verify(appUserService).save(eq(CREATOR_UUID), eq(SUBSCRIPTION_TYPE));
  }

  @Test
  public void when_user_already_exists__should_return_user_already_exists_response() throws Exception {

    // Given
    when(appUserService.findByMarketAccountIdentifier(CREATOR_UUID))
        .thenReturn(anAppUser(CODE, MARKET_ACCOUNT_IDENTIFIER));

    // Execute
    AppDirectApiResponse response = processor.process(aSubscriptionOrder(CREATOR_UUID, SUBSCRIPTION_TYPE));

    // Verify
    assertThat(response.isSuccess(), is(false));
    assertThat(response.getAccountIdentifier(), is(CODE));
    assertThat(response.getErrorCode(), is(USER_ALREADY_EXISTS));
    verify(appUserService).findByMarketAccountIdentifier(eq(CREATOR_UUID));
    verify(appUserService, never()).save(anyString(), any(SubscriptionType.class));
  }

  private AppUser anAppUser(String code, String marketAccountIdentifier) {
    return AppUser.builder()
        .code(code)
        .marketAccountIdentifier(marketAccountIdentifier)
        .active(true)
        .build();
  }

  public AppDirectNotification aSubscriptionOrder(String creatorUuid, SubscriptionType subscriptionType) {
    return AppDirectNotification.builder()
        .type(SUBSCRIPTION_ORDER)
        .marketplace(Marketplace.builder()
            .partner("partner")
            .build())
        .creator(User.builder().uuid(creatorUuid).build())
        .payload(Payload.builder()
            .order(Order.builder().editionCode(subscriptionType.toString()).build())
            .build())
        .build();
  }
}
