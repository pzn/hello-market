package com.github.pzn.hellomarket.repository;

import static com.github.pzn.hellomarket.model.entity.SubscriptionType.SMALL;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import com.github.pzn.hellomarket.model.entity.AppOrg;
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
public class AppOrgRepositoryIntegrationTest {

  private static final Long ID = 1L;
  private static final String CODE = "apporg_code";
  private static final String MARKET_IDENTIFIER = "apporg_market_identifier";
  private static final Boolean INITIAL_STATUS = true;
  private static final SubscriptionType SUBSCRIPTION_TYPE = SMALL;
  private static final String NAME = "name";
  private static final String COUNTRY = "CA";

  @Autowired
  private JdbcTemplate jdbcTemplate;
  @Autowired
  private AppOrgRepository appOrgRepository;

  @Before
  public void before() throws Exception {
    jdbcTemplate.execute("DELETE FROM appuser");
    jdbcTemplate.execute("DELETE FROM apporg");
    jdbcTemplate.update("INSERT INTO apporg(id, code, market_identifier, active, name, country, subscription_type) VALUES(?, ?, ?, ?, ?, ?, ?)",
        new Object[]{ID, CODE, MARKET_IDENTIFIER, INITIAL_STATUS, NAME, COUNTRY, SUBSCRIPTION_TYPE.toString()});
    jdbcTemplate.update("INSERT INTO appuser VALUES(?, ?, ?, ?, ?, ?, ?)",
        new Object[]{3000L, "appuser_code", "appuser_market_identifier", "first_name", "last_name", "open_id_url", ID});
  }

  @Test
  public void can_change_active_status() throws Exception {

    // Execute
    appOrgRepository.changeActiveStatus(ID, !INITIAL_STATUS);

    // Verify
    Boolean newStatus = jdbcTemplate.queryForObject("SELECT active FROM apporg WHERE id = " + ID, Boolean.class);
    assertThat(newStatus, is(not(INITIAL_STATUS)));
  }

  @Test
  public void can_delete_apporg() throws Exception {

    // Execute
    appOrgRepository.delete(ID);

    // Verify
    int appOrgCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM apporg", Integer.class);
    assertThat(appOrgCount, is(0));
    int appUserCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM appuser", Integer.class);
    assertThat(appUserCount, is(0));
  }

  @Test(expected = DataAccessException.class)
  public void cannot_create_apporg_with_same_code() throws Exception {

    // Given
    AppOrg appOrg = AppOrg.builder()
        .code(CODE)
        .marketIdentifier("something else")
        .active(INITIAL_STATUS)
        .name(NAME)
        .country(COUNTRY).build();

    // Execute
    appOrgRepository.save(appOrg);
  }

  @Test(expected = DataAccessException.class)
  public void cannot_create_apporg_with_same_market_identifier() throws Exception {

    // Given
    AppOrg appOrg = AppOrg.builder()
        .code("something else")
        .marketIdentifier(MARKET_IDENTIFIER)
        .active(INITIAL_STATUS)
        .name(NAME)
        .country(COUNTRY).build();

    // Execute
    appOrgRepository.save(appOrg);
  }
}
