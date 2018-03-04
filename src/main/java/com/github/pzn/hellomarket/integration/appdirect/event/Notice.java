package com.github.pzn.hellomarket.integration.appdirect.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Notice {

  private NoticeType type;
}
