package com.github.pzn.hellomarket.integration.appdirect.event;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {

  private String email;
  private String firstName;
  private String language;
  private String lastName;
  private String locale;
  private String openId;
  private String uuid;
  private Address address;
  private Map<String, String> attributes;
}
