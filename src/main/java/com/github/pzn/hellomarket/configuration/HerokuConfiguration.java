package com.github.pzn.hellomarket.configuration;

import static java.lang.System.getenv;

import java.net.URI;
import java.net.URISyntaxException;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("heroku")
public class HerokuConfiguration {

  @Bean
  public DataSource dataSource() throws URISyntaxException {
    URI dbUri = new URI(getenv("DATABASE_URL"));

    String username = dbUri.getUserInfo().split(":")[0];
    String password = dbUri.getUserInfo().split(":")[1];
    String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();

    return DataSourceBuilder.create()
        .driverClassName("org.postgresql.Driver")
        .username(username)
        .password(password)
        .url(dbUrl)
        .build();
  }
}
