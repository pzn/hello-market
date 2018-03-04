package com.github.pzn.hellomarket.integration.appdirect.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Account {

  private String accountIdentifier;
  private String status;
}
