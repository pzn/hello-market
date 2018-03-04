package com.github.pzn.hellomarket.integration.appdirect.event;

import lombok.Builder;
import lombok.Data;

/**
 * https://help.appdirect.com/appdistrib/Default.htm#Dev-DistributionGuide/en-attributes.html
 */
@Data
@Builder
public class AppDirectNotification {

  private EventType type;
  private Marketplace marketplace;
  private String applicationUuid;
  private Flag flag;
  private String returnUrl;
  private User creator;
  private Payload payload;
}
