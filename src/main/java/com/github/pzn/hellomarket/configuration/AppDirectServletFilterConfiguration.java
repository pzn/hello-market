package com.github.pzn.hellomarket.configuration;

import com.github.pzn.hellomarket.controller.filter.OAuthRequestFilter;
import java.util.Collections;
import oauth.signpost.OAuthConsumer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppDirectServletFilterConfiguration {

  @Bean
  public OAuthRequestFilter oAuthRequestFilter(OAuthConsumer oAuthConsumer) {
    return new OAuthRequestFilter(oAuthConsumer);
  }

  @Bean
  public FilterRegistrationBean filterRegistrationBean(OAuthRequestFilter oAuthRequestFilter) {

    FilterRegistrationBean registrationBean = new FilterRegistrationBean();
    registrationBean.setFilter(oAuthRequestFilter);
    registrationBean.setUrlPatterns(Collections.singletonList("/appdirect/*"));
    return registrationBean;
  }
}
