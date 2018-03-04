package com.github.pzn.hellomarket.integration.appdirect.processor;

public class NotificationProcessorException extends RuntimeException {

  public NotificationProcessorException() {
    super();
  }

  public NotificationProcessorException(String message) {
    super(message);
  }

  public NotificationProcessorException(String message, Throwable cause) {
    super(message, cause);
  }

  public NotificationProcessorException(Throwable cause) {
    super(cause);
  }

  protected NotificationProcessorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
