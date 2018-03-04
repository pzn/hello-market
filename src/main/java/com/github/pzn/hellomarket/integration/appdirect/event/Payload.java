package com.github.pzn.hellomarket.integration.appdirect.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Payload {

  private User user;
  private Account account;
  private Company company;
  private Order order;
  private NoticeType type;
}
