package com.github.pzn.hellomarket.integration.appdirect;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AppDirectApiResponse {

  private boolean success;
  private ErrorCode errorCode;
  private String message;
  private String accountIdentifier;
  private String userIdentifier;

  public static AppDirectApiResponse success() {
    return success(null);
  }

  public static AppDirectApiResponse success(String accountIdentifier) {
    return success(accountIdentifier, null);
  }

  public static AppDirectApiResponse success(String accountIdentifier, String userIdentifier) {
    return AppDirectApiResponse.builder()
        .success(true)
        .accountIdentifier(accountIdentifier)
        .userIdentifier(userIdentifier)
        .build();
  }
}
