package com.github.pzn.hellomarket.controller;

import static com.github.pzn.hellomarket.controller.AppDirectController.APPDIRECT_ROOT_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.github.pzn.hellomarket.integration.appdirect.AppDirectApiResponse;
import com.github.pzn.hellomarket.integration.appdirect.event.AppDirectNotification;
import com.github.pzn.hellomarket.service.AppDirectFetchEventService;
import com.github.pzn.hellomarket.service.NotificationProcessorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = APPDIRECT_ROOT_PATH, produces = APPLICATION_JSON_VALUE)
@Slf4j
public class AppDirectController {

  public static final String APPDIRECT_ROOT_PATH = "/appdirect";

  private AppDirectFetchEventService fetchEventService;
  private NotificationProcessorService processNotificationService;

  @Autowired
  public AppDirectController(AppDirectFetchEventService appDirectFetchEventService,
                             NotificationProcessorService processNotificationService) {
    this.fetchEventService = appDirectFetchEventService;
    this.processNotificationService = processNotificationService;
  }

  @GetMapping
  public @ResponseBody AppDirectApiResponse appDirectEvent(@RequestParam("url") String url) {

    log.info("Received AppDirect Subscription Event, url={}", url);
    AppDirectNotification notification = fetchEventService.fetch(url);

    return processNotificationService.process(notification);
  }
}
