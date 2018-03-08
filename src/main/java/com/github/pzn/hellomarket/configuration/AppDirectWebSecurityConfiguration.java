package com.github.pzn.hellomarket.configuration;

import static com.github.pzn.hellomarket.controller.AppDirectController.APPDIRECT_ROOT_PATH;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth.provider.filter.OAuthProviderProcessingFilter;
import org.springframework.security.oauth.provider.filter.ProtectedResourceProcessingFilter;
import org.springframework.security.oauth.provider.token.InMemoryProviderTokenServices;
import org.springframework.security.oauth.provider.token.OAuthProviderTokenServices;
import org.springframework.security.web.header.HeaderWriterFilter;

@Configuration
@Order(1)
public class AppDirectWebSecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .antMatcher(APPDIRECT_ROOT_PATH + "/**")
        .authorizeRequests()
        .anyRequest().authenticated()
        .and()
        .addFilterAfter(oAuthProviderProcessingFilter(), HeaderWriterFilter.class);
  }

  @Bean
  public OAuthProviderProcessingFilter oAuthProviderProcessingFilter() {
    ProtectedResourceProcessingFilter filter = new ProtectedResourceProcessingFilter();
    filter.setIgnoreMissingCredentials(true);
    return filter;
  }

  @Bean
  public OAuthProviderTokenServices oAuthProviderTokenServices() {
    return new InMemoryProviderTokenServices();
  }
}
