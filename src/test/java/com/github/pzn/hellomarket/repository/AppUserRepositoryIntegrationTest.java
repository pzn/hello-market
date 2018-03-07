package com.github.pzn.hellomarket.repository;

import static com.github.pzn.hellomarket.model.entity.SubscriptionType.START_UP;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.github.pzn.hellomarket.model.entity.AppOrg;
import com.github.pzn.hellomarket.model.entity.AppUser;
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
public class AppUserRepositoryIntegrationTest {

  private static final Long ID = 1L;
  private static final String CODE = "appuser_code";
  private static final String MARKET_IDENTIFIER = "appouser_market_identifier";
  private static final String FIRST_NAME = "first_name";
  private static final String LAST_NAME = "last_name";
  private static final String OPEN_ID = "open_id_url";
  private static final Long APPORG_ID = 1L;

  @Autowired
  private JdbcTemplate jdbcTemplate;
  @Autowired
  private AppOrgRepository appOrgRepository;
  @Autowired
  private AppUserRepository appUserRepository;

  private AppOrg appOrg;

  @Before
  public void before() throws Exception {
    jdbcTemplate.execute("DELETE FROM appuser");
    jdbcTemplate.execute("DELETE FROM apporg");
    jdbcTemplate.update("INSERT INTO apporg(id, code, market_identifier, active, name, country, subscription_type) VALUES(?, ?, ?, ?, ?, ?, ?)",
        new Object[]{APPORG_ID, "apporg_code", "apporg_market_identifier", true, "name", "CA", START_UP.toString()});
    appOrg = appOrgRepository.findOne(APPORG_ID);
    jdbcTemplate.update("INSERT INTO appuser VALUES(?, ?, ?, ?, ?, ?, ?)",
        new Object[]{ID, CODE, MARKET_IDENTIFIER, FIRST_NAME, LAST_NAME, OPEN_ID, ID});
  }

  @Test
  public void can_delete_appuser() throws Exception {

    // Execute
    appUserRepository.delete(ID);

    // Verify
    int appUserCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM appuser", Integer.class);
    assertThat(appUserCount, is(0));
    int appOrgCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM apporg WHERE id = " + APPORG_ID, Integer.class);
    assertThat(appOrgCount, is(1));
  }

  @Test(expected = DataAccessException.class)
  public void cannot_create_appuser_with_same_code() throws Exception {

    // Given
    AppUser appUser = AppUser.builder()
        .code(CODE)
        .marketIdentifier("something else")
        .openId("something else")
        .appOrg(appOrg)
        .build();

    // Execute
    appUserRepository.save(appUser);
  }

  @Test(expected = DataAccessException.class)
  public void cannot_create_appuser_with_no_apporg() throws Exception {

    // Given
    AppUser appUser = AppUser.builder()
        .code("something else")
        .marketIdentifier("something else")
        .openId("something else")
        .build();

    // Execute
    appUserRepository.save(appUser);
  }

  @Test(expected = DataAccessException.class)
  public void cannot_create_appuser_with_same_market_identifier_and_apporg_id_combination() throws Exception {

    // Given
    AppUser appUser = AppUser.builder()
        .code("something else")
        .marketIdentifier(MARKET_IDENTIFIER)
        .openId("something else")
        .appOrg(appOrg)
        .build();

    // Execute
    appUserRepository.save(appUser);
  }
}
