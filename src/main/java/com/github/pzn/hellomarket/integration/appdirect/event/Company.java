package com.github.pzn.hellomarket.integration.appdirect.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Company {

  private String uuid;
  private String externalId;
  private String name;
  private String email;
  private String phoneNumber;
  private String website;
  private String country;
}
