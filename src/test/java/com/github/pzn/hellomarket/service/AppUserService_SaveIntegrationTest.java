package com.github.pzn.hellomarket.service;

import static com.github.pzn.hellomarket.model.entity.SubscriptionType.MONTHLY;
import static com.github.pzn.hellomarket.model.entity.SubscriptionType.TRIAL;
import static com.github.pzn.hellomarket.model.entity.SubscriptionType.YEARLY;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.github.pzn.hellomarket.model.entity.AppUser;
import com.github.pzn.hellomarket.model.entity.SubscriptionType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AppUserService_SaveIntegrationTest {

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
  }

  @Test
  public void can_save() throws Exception {

    // Execute
    Long appUserId = appUserService.save(MARKET_ACCOUNT_IDENTIFIER, SUBSCRIPTION_TYPE).getId();

    // Verify
    AppUser newAppUser = appUserService.findById(appUserId);
    assertThat(newAppUser.getId(), is(appUserId));
    assertThat(newAppUser.getCode(), is(notNullValue()));
    assertThat(newAppUser.getMarketAccountIdentifier(), is(MARKET_ACCOUNT_IDENTIFIER));
    assertThat(newAppUser.isActive(), is(ACTIVE));
    assertThat(newAppUser.getSubscriptionType(), is(SUBSCRIPTION_TYPE));
  }

  @Test(expected = DataAccessException.class)
  public void when_market_account_identifier_already_exists__should_throw_dae() throws Exception {

    // Given
    givenAnExistingAppUser();

    // Execute
    appUserService.save(MARKET_ACCOUNT_IDENTIFIER, YEARLY);
  }

  private void givenAnExistingAppUser() throws Exception {
    jdbcTemplate.update("INSERT INTO app_user VALUES(?, ?, ?, ?, ?)",
        new Object[]{ID, CODE, MARKET_ACCOUNT_IDENTIFIER, true, SUBSCRIPTION_TYPE.toString()});
  }
}
