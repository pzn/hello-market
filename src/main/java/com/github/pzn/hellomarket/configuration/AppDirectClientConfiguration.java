package com.github.pzn.hellomarket.configuration;

import static java.util.Collections.singletonList;

import com.fasterxml.jackson.databind.ObjectMapper;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth.common.signature.SharedConsumerSecretImpl;
import org.springframework.security.oauth.consumer.BaseProtectedResourceDetails;
import org.springframework.security.oauth.consumer.ProtectedResourceDetails;
import org.springframework.security.oauth.consumer.client.OAuthRestTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppDirectClientConfiguration {

  @Value("${appdirect.consumer_key}")
  private String consumerKey;
  @Value("${appdirect.consumer_secret}")
  private String consumerSecret;

  @Bean
  public RestTemplate restTemplate(ObjectMapper objectMapper) {
    OAuthRestTemplate restTemplate = new OAuthRestTemplate(getResources());
    restTemplate.setMessageConverters(singletonList(new MappingJackson2HttpMessageConverter(objectMapper)));
    return restTemplate;
  }

  @Bean
  public OAuthConsumer oAuthConsumer() {
    return new DefaultOAuthConsumer(consumerKey, consumerSecret);
  }

  private ProtectedResourceDetails getResources() {
    BaseProtectedResourceDetails resourceDetails = new BaseProtectedResourceDetails();
    resourceDetails.setConsumerKey(consumerKey);
    resourceDetails.setSharedSecret(new SharedConsumerSecretImpl(consumerSecret));
    return resourceDetails;
  }

  public void afterPropertiesSet() {
    if (consumerKey == null) {
      throw new IllegalArgumentException("Property 'appdirect.consumer_key' not set!");
    }
    if (consumerSecret == null) {
      throw new IllegalArgumentException("Property 'appdirect.consumer_secret' not set!");
    }
  }
}
