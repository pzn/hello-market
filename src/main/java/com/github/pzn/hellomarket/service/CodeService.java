package com.github.pzn.hellomarket.service;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CodeService {

  public String generateCode() {
    return UUID.randomUUID().toString();
  }
}
