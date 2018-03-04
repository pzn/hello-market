package com.github.pzn.hellomarket.integration.appdirect.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Order {

  private String editionCode;
  private String addonOfferingCode;
  private String pricingDuration;
  private List<Item> items;

  @Data
  @Builder
  public static class Item {

    private String quantity;
    private String unit;
  }
}
