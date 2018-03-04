package com.github.pzn.hellomarket.integration.appdirect.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Marketplace {

  private String baseUrl;
  private String partner;
}
