package com.github.pzn.hellomarket.integration.appdirect.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Address {

  private String city;
  private String country;
  private String state;
  private String street1;
  private String street2;
  private String zip;
}
