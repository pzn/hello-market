package com.github.pzn.hellomarket.integration.appdirect.processor;

import com.github.pzn.hellomarket.integration.appdirect.AppDirectApiResponse;
import com.github.pzn.hellomarket.integration.appdirect.event.AppDirectNotification;
import com.github.pzn.hellomarket.integration.appdirect.event.EventType;

public interface AppDirectNotificationProcessor {

  AppDirectApiResponse process(AppDirectNotification notification) throws NotificationProcessorException;
  EventType getType();
}
