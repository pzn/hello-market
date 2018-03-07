package com.github.pzn.hellomarket.model.entity;

import lombok.Getter;

@Getter
public enum SubscriptionType {

  SINGLE_USER(1),
  START_UP(10),
  SMALL(25),
  MEDIUM(50),
  LARGE(500);

  private final int maxUsers;

  SubscriptionType(int maxUsers) {
    this.maxUsers = maxUsers;
  }
}
