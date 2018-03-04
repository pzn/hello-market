package com.github.pzn.hellomarket.service;

import static java.util.Arrays.asList;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.github.pzn.hellomarket.integration.appdirect.event.AppDirectNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class AppDirectFetchEventService {

  private RestTemplate restTemplate;

  @Autowired
  public AppDirectFetchEventService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public AppDirectNotification fetch(String url) {

    log.debug("Fetching AppDirect notification: url={}", url);
    AppDirectNotification notification = restTemplate.exchange(url,
        GET,
        httpEntity(),
        AppDirectNotification.class).getBody();

    return notification;
  }

  private HttpEntity httpEntity() {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(asList(APPLICATION_JSON));
    return new HttpEntity<>("", headers);
  }
}
