package com.github.pzn.hellomarket.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth.provider.filter.OAuthProviderProcessingFilter;
import org.springframework.security.oauth.provider.filter.ProtectedResourceProcessingFilter;
import org.springframework.security.oauth.provider.token.InMemoryProviderTokenServices;
import org.springframework.security.oauth.provider.token.OAuthProviderTokenServices;
import org.springframework.security.web.header.HeaderWriterFilter;

@Configuration
@EnableWebSecurity
public class AppDirectControllerSecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .authorizeRequests()
        .antMatchers("/appdirect/**").authenticated()
        .and()
        .addFilterAfter(oAuthProviderProcessingFilter(), HeaderWriterFilter.class);
  }

  @Bean
  public OAuthProviderProcessingFilter oAuthProviderProcessingFilter() {
    ProtectedResourceProcessingFilter filter = new ProtectedResourceProcessingFilter();
    filter.setIgnoreMissingCredentials(false);
    return filter;
  }

  @Bean
  public OAuthProviderTokenServices oAuthProviderTokenServices() {
    return new InMemoryProviderTokenServices();
  }
}
