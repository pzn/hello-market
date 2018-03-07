package com.github.pzn.hellomarket.configuration;

import static java.util.Collections.singletonList;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth.consumer.ProtectedResourceDetails;
import org.springframework.security.oauth.consumer.client.OAuthRestTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppDirectClientConfiguration {

  @Bean
  public RestTemplate restTemplate(ObjectMapper objectMapper, ProtectedResourceDetails appDirectOAuthResourceDetails) {
    OAuthRestTemplate restTemplate = new OAuthRestTemplate(appDirectOAuthResourceDetails);
    restTemplate.setMessageConverters(singletonList(new MappingJackson2HttpMessageConverter(objectMapper)));
    return restTemplate;
  }
}
