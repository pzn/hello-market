package com.github.pzn.hellomarket.service;

import static com.github.pzn.hellomarket.model.entity.SubscriptionType.MONTHLY;
import static com.github.pzn.hellomarket.model.entity.SubscriptionType.YEARLY;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsIterableWithSize.iterableWithSize;
import static org.junit.Assert.assertThat;

import com.github.pzn.hellomarket.model.entity.AppUser;
import com.github.pzn.hellomarket.model.entity.SubscriptionType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AppUserServiceIntegrationTest {

  private static final Long ID = 1L;
  private static final String CODE = "code";
  private static final String MARKET_ACCOUNT_IDENTIFIER = "market_account_identifier";
  private static final boolean ACTIVE = true;
  private static final SubscriptionType SUBSCRIPTION_TYPE = MONTHLY;

  @Autowired
  private JdbcTemplate jdbcTemplate;
  @Autowired
  private AppUserService appUserService;

  @Before
  public void before() throws Exception {
    jdbcTemplate.execute("DELETE FROM app_user");
    jdbcTemplate.update("INSERT INTO app_user VALUES(?, ?, ?, ?, ?)",
                        new Object[]{ID, CODE, MARKET_ACCOUNT_IDENTIFIER, true, SUBSCRIPTION_TYPE.toString()});
  }

  @Test
  public void can_find_all() throws Exception {

    // Execute
    Iterable<AppUser> appUsers = appUserService.findAll();

    // Verify
    assertThat(appUsers, is(iterableWithSize(1)));
    verifyAppUser(appUsers.iterator().next());
  }

  @Test
  public void can_find_by_id() throws Exception {
    verifyAppUser(appUserService.findById(ID));
  }

  @Test
  public void can_find_by_code() throws Exception {
    verifyAppUser(appUserService.findByCode(CODE));
  }

  @Test
  public void can_find_by_market_account_identifier() throws Exception {
    verifyAppUser(appUserService.findByMarketAccountIdentifier(MARKET_ACCOUNT_IDENTIFIER));
  }

  @Test
  public void can_change_subscription() throws Exception {

    // Execute
    appUserService.changeSubscriptionType(ID, YEARLY);

    // Verify
    AppUser appUser = appUserService.findById(ID);
    assertThat(appUser.getSubscriptionType(), is(YEARLY));
  }

  @Test
  public void can_change_status() throws Exception {

    // Execute
    appUserService.changeStatus(ID, false);

    // Verify
    AppUser appUser = appUserService.findById(ID);
    assertThat(appUser.isActive(), is(false));
  }

  private void verifyAppUser(AppUser appUser) {
    assertThat(appUser.getId(), is(ID));
    assertThat(appUser.getCode(), is(CODE));
    assertThat(appUser.getMarketAccountIdentifier(), is(MARKET_ACCOUNT_IDENTIFIER));
    assertThat(appUser.isActive(), is(ACTIVE));
    assertThat(appUser.getSubscriptionType(), is(SUBSCRIPTION_TYPE));
  }
}
