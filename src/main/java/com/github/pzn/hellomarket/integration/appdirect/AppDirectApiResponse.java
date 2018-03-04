package com.github.pzn.hellomarket.integration.appdirect;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppDirectApiResponse {

  private boolean success;
  private ErrorCode errorCode;
  private String message;
  private String accountIdentifier;
  private String userIdentifier;
}
