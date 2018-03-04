package com.github.pzn.hellomarket.configuration;

import com.github.pzn.hellomarket.integration.appdirect.event.EventType;
import com.github.pzn.hellomarket.integration.appdirect.processor.AppDirectNotificationProcessor;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Collection;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AppDirectNotificationProcessorConfiguration {

  @Bean
  public Map<EventType, AppDirectNotificationProcessor> notificationProcessor(Collection<AppDirectNotificationProcessor> notificationProcessors) {

    Builder<EventType, AppDirectNotificationProcessor> mapBuilder = ImmutableMap.builder();
    for (AppDirectNotificationProcessor notificationProcessor : notificationProcessors) {
      log.info("Registering AppDirect Notification Processor for {}", notificationProcessor.getType());
      mapBuilder.put(notificationProcessor.getType(), notificationProcessor);
    }
    return mapBuilder.build();
  }
}
