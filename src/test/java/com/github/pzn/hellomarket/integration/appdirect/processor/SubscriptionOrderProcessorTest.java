package com.github.pzn.hellomarket.integration.appdirect.processor;

import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.CONFIGURATION_ERROR;
import static com.github.pzn.hellomarket.integration.appdirect.ErrorCode.USER_ALREADY_EXISTS;
import static com.github.pzn.hellomarket.integration.appdirect.event.EventType.SUBSCRIPTION_ORDER;
import static com.github.pzn.hellomarket.model.entity.SubscriptionType.MEDIUM;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.pzn.hellomarket.integration.appdirect.AppDirectApiResponse;
import com.github.pzn.hellomarket.integration.appdirect.event.AppDirectNotification;
import com.github.pzn.hellomarket.integration.appdirect.event.Company;
import com.github.pzn.hellomarket.integration.appdirect.event.Marketplace;
import com.github.pzn.hellomarket.integration.appdirect.event.Order;
import com.github.pzn.hellomarket.integration.appdirect.event.Order.Item;
import com.github.pzn.hellomarket.integration.appdirect.event.Payload;
import com.github.pzn.hellomarket.integration.appdirect.event.User;
import com.github.pzn.hellomarket.model.entity.AppOrg;
import com.github.pzn.hellomarket.model.entity.AppUser;
import com.github.pzn.hellomarket.model.entity.SubscriptionType;
import com.github.pzn.hellomarket.repository.AppOrgRepository;
import com.github.pzn.hellomarket.repository.AppUserRepository;
import com.github.pzn.hellomarket.service.CodeService;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SubscriptionOrderProcessorTest {

  private static final String APPORG_CODE = "apporg_code";
  private static final String COMPANY_UUID = "company_uuid";
  private static final String SUBSCRIPTION_TYPE = MEDIUM.toString();
  private static final String COMPANY_NAME = "company_name";
  private static final String COMPANY_COUNTRY = "company_country";

  private static final String APPUSER_CODE = "appuser_code";
  private static final String CREATOR_UUID = "creator_uuid";
  private static final String CREATOR_FIRST_NAME = "creator_first_name";
  private static final String CREATOR_LAST_NAME = "creator_last_name";
  private static final String CREATOR_OPEN_ID = "creator_open_id";

  @InjectMocks
  private SubscriptionOrderProcessor processor;
  @Mock
  private AppOrgRepository appOrgRepository;
  @Captor
  private ArgumentCaptor<AppOrg> appOrgCaptor;
  @Mock
  private AppUserRepository appUserRepository;
  @Captor
  private ArgumentCaptor<AppUser> appUserCaptor;
  @Mock
  private CodeService codeService;

  @Before
  public void before() {
    when(codeService.generateCode())
        .thenReturn(APPORG_CODE, APPUSER_CODE);
  }

  @Test
  public void can_subscribe() throws Exception {

    // Given
    assumeCompanyDoesNotExist();

    // Execute
    AppDirectApiResponse response = processor.process(aSubscriptionOrder(COMPANY_UUID, SUBSCRIPTION_TYPE));

    // Verify
    assertThat(response.isSuccess(), is(true));
    assertThat(response.getAccountIdentifier(), is(APPORG_CODE));
    assertThat(response.getUserIdentifier(), is(APPUSER_CODE));
    assertThat(response.getErrorCode(), is(nullValue()));
    verify(appOrgRepository).findByMarketIdentifier(eq(COMPANY_UUID));
    verifySaveAppOrgHasInteractions(SUBSCRIPTION_TYPE);
    verifySaveAppUserHasInteractions();
  }

  @Test
  public void when_invalid_edition_code__should_return_bad_response() throws Exception {

    // Given
    assumeCompanyDoesNotExist();

    // Execute
    AppDirectApiResponse response = processor.process(aSubscriptionOrder(COMPANY_UUID, "SUPERPONY"));

    // Verify
    assertThat(response.isSuccess(), is(false));
    assertThat(response.getAccountIdentifier(), is(nullValue()));
    assertThat(response.getUserIdentifier(), is(nullValue()));
    assertThat(response.getErrorCode(), is(CONFIGURATION_ERROR));
    verify(appOrgRepository).findByMarketIdentifier(eq(COMPANY_UUID));
    verifySaveAppOrgHasNoInteractions();
    verifySaveAppUserHasNoInteractions();
  }

  @Test
  public void when_company_already_subscribed__should_return_bad_response() throws Exception {

    // Given
    assumeCompanyDoesExist();

    // Execute
    AppDirectApiResponse response = processor.process(aSubscriptionOrder(COMPANY_UUID, SUBSCRIPTION_TYPE));

    // Verify
    assertThat(response.isSuccess(), is(false));
    assertThat(response.getAccountIdentifier(), is(COMPANY_UUID));
    assertThat(response.getUserIdentifier(), is(nullValue()));
    assertThat(response.getErrorCode(), is(USER_ALREADY_EXISTS));
    verify(appOrgRepository).findByMarketIdentifier(eq(COMPANY_UUID));
    verifySaveAppOrgHasNoInteractions();
    verifySaveAppUserHasNoInteractions();
  }

  private void verifySaveAppOrgHasNoInteractions() {
    verify(appOrgRepository, never()).save(any(AppOrg.class));
  }

  private void verifySaveAppOrgHasInteractions(String expectedSubscriptionType) {

    verify(appOrgRepository).save(appOrgCaptor.capture());
    AppOrg capturedAppOrg = appOrgCaptor.getValue();

    assertThat(capturedAppOrg.getCode(), is(APPORG_CODE));
    assertThat(capturedAppOrg.getMarketIdentifier(), is(COMPANY_UUID));
    assertThat(capturedAppOrg.getActive(), is(true));
    assertThat(capturedAppOrg.getSubscriptionType(), is(SubscriptionType.valueOf(expectedSubscriptionType)));
    assertThat(capturedAppOrg.getName(), is(COMPANY_NAME));
    assertThat(capturedAppOrg.getCountry(), is(COMPANY_COUNTRY));
  }

  private void verifySaveAppUserHasNoInteractions() {
    verify(appUserRepository, never()).save(any(AppUser.class));
  }

  private void verifySaveAppUserHasInteractions() {

    verify(appUserRepository).save(appUserCaptor.capture());
    AppUser capturedAppUser = appUserCaptor.getValue();

    assertThat(capturedAppUser.getCode(), is(APPUSER_CODE));
    assertThat(capturedAppUser.getMarketIdentifier(), is(CREATOR_UUID));
    assertThat(capturedAppUser.getFirstName(), is(CREATOR_FIRST_NAME));
    assertThat(capturedAppUser.getLastName(), is(CREATOR_LAST_NAME));
    assertThat(capturedAppUser.getOpenId(), is(CREATOR_OPEN_ID));

    AppOrg capturedAppOrg = appOrgCaptor.getValue();
    assertThat(capturedAppUser.getAppOrg(), is(capturedAppOrg));
  }

  private void assumeCompanyDoesNotExist() {
    when(appOrgRepository.findByMarketIdentifier(COMPANY_UUID))
        .thenReturn(null);
  }

  private void assumeCompanyDoesExist() {
    when(appOrgRepository.findByMarketIdentifier(COMPANY_UUID))
        .thenReturn(new AppOrg());
  }

  public AppDirectNotification aSubscriptionOrder(String companyUuid, String editionCode) {

    return AppDirectNotification.builder()
        .type(SUBSCRIPTION_ORDER)
        .marketplace(Marketplace.builder()
            .partner("partner").build())
        .creator(
            User.builder()
                .uuid(CREATOR_UUID)
                .firstName(CREATOR_FIRST_NAME)
                .lastName(CREATOR_LAST_NAME)
                .openId(CREATOR_OPEN_ID).build())
        .payload(Payload.builder()
            .company(Company.builder()
                .uuid(companyUuid)
                .name(COMPANY_NAME)
                .country(COMPANY_COUNTRY).build())
            .order(Order.builder()
                .editionCode(editionCode)
                .items(Arrays.asList(Item.builder().unit("USER").quantity("1").build())).build()).build())
        .build();
  }
}
