package com.github.pzn.hellomarket.configuration;

import static java.util.Collections.singletonMap;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth.common.signature.SharedConsumerSecretImpl;
import org.springframework.security.oauth.consumer.BaseProtectedResourceDetails;
import org.springframework.security.oauth.consumer.ProtectedResourceDetails;
import org.springframework.security.oauth.provider.BaseConsumerDetails;
import org.springframework.security.oauth.provider.ConsumerDetails;
import org.springframework.security.oauth.provider.ConsumerDetailsService;
import org.springframework.security.oauth.provider.InMemoryConsumerDetailsService;

@Configuration
public class AppDirectOAuthConsumerConfiguration {

    @Value("${appdirect.consumer_key}")
    private String consumerKey;
    @Value("${appdirect.consumer_secret}")
    private String consumerSecret;

    @Bean
    public ProtectedResourceDetails appDirectOAuthResourceDetails() {
        BaseProtectedResourceDetails resourceDetails = new BaseProtectedResourceDetails();
        resourceDetails.setConsumerKey(consumerKey);
        resourceDetails.setSharedSecret(new SharedConsumerSecretImpl(consumerSecret));
        return resourceDetails;
    }

    @Bean
    public ConsumerDetailsService oAuthConsumerDetailsService() {
        InMemoryConsumerDetailsService service = new InMemoryConsumerDetailsService();
        service.setConsumerDetailsStore(consumerDetailsStore());
        return service;
    }

    private Map<String, ConsumerDetails> consumerDetailsStore() {
        return singletonMap(consumerKey, appDirectOAuthConsumerDetails());
    }

    private ConsumerDetails appDirectOAuthConsumerDetails() {

        BaseConsumerDetails consumerDetails = new BaseConsumerDetails();
        consumerDetails.setConsumerKey(consumerKey);
        consumerDetails.setSignatureSecret(new SharedConsumerSecretImpl(consumerSecret));
        consumerDetails.getAuthorities().add(new SimpleGrantedAuthority("APPDIRECT"));
        consumerDetails.setRequiredToObtainAuthenticatedToken(false);
        return consumerDetails;
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
