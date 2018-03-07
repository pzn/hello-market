package com.github.pzn.hellomarket.integration.appdirect.processor;

import com.github.pzn.hellomarket.integration.appdirect.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class NotificationProcessorException extends RuntimeException {

  private ErrorCode errorCode;
  private String accountIdentifier;
  private String userIdentifier;
  private String message;

  public NotificationProcessorException(ErrorCode errorCode) {
    this(errorCode, null);
  }

  public NotificationProcessorException(ErrorCode errorCode, String message) {
    this(errorCode, null, null, message);
  }

  public NotificationProcessorException(String message) {
    super(message);
  }
}
