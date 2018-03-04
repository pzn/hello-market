package com.github.pzn.hellomarket.service;

import static com.github.pzn.hellomarket.integration.appdirect.event.Flag.STATELESS;

import com.github.pzn.hellomarket.integration.appdirect.AppDirectApiResponse;
import com.github.pzn.hellomarket.integration.appdirect.event.AppDirectNotification;
import com.github.pzn.hellomarket.integration.appdirect.event.EventType;
import com.github.pzn.hellomarket.integration.appdirect.event.Flag;
import com.github.pzn.hellomarket.integration.appdirect.processor.AppDirectNotificationProcessor;
import com.github.pzn.hellomarket.integration.appdirect.processor.NotificationProcessorException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationProcessorService {

  private Map<EventType, AppDirectNotificationProcessor> notificationProcessors;

  @Autowired
  public NotificationProcessorService(Map<EventType, AppDirectNotificationProcessor> notificationProcessors) {
    this.notificationProcessors = notificationProcessors;
  }

  public AppDirectApiResponse process(AppDirectNotification notification) throws NotificationProcessorException {

    log.debug("AppDirect Subscription Event Notification Received: {}", notification);

    if (!notificationProcessors.containsKey(notification.getType())) {
      throw new NotificationProcessorException(
          String.format("The following AppDirect Subscription Event Notification is not supported: '%s'", notification.getType()));
    }

    if (notification.getFlag() == STATELESS) {
      return AppDirectApiResponse.builder()
          .success(true)
          .build();
    }

    return notificationProcessors.get(notification.getType()).process(notification);
  }
}
