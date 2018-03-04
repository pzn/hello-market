package com.github.pzn.hellomarket.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.github.pzn.hellomarket.integration.appdirect.AppDirectApiResponse;
import com.github.pzn.hellomarket.integration.appdirect.event.AppDirectNotification;
import com.github.pzn.hellomarket.integration.appdirect.processor.NotificationProcessorException;
import com.github.pzn.hellomarket.service.AppDirectFetchEventService;
import com.github.pzn.hellomarket.service.NotificationProcessorService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(MockitoJUnitRunner.class)
public class AppDirectControllerTest {

  @InjectMocks
  private AppDirectController controller;
  @Mock
  private AppDirectFetchEventService mockedAppDirectFetchEventService;
  @Mock
  private NotificationProcessorService mockedNotificationProcessorService;

  private MockMvc mockMvc;

  @Before
  public void before() throws Exception {
    mockMvc = standaloneSetup(controller)
        .setControllerAdvice(new HelloMarketControllerAdvice())
        .build();
  }

  @Test
  public void can_send_subscription_order() throws Exception {

    // Given
    String eventUrl = "https%3A%2F%2Fmarketplace.appdirect.com%2Fapi%2Fintegration%2Fv1%2Fevents%2F00000000-0000-0000-0000-000000000000";
    when(mockedAppDirectFetchEventService.fetch(eq(eventUrl)))
        .thenReturn(aNotification());
    when(mockedNotificationProcessorService.process(any(AppDirectNotification.class)))
        .thenReturn(aSuccessfulApiResponse());

    // Execute & Verify
    mockMvc.perform(
        get("/appdirect/")
            .param("url", eventUrl))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success", is(true)));
  }

  @Test
  public void when_notification_processor_exception_thrown__should_return_proper_response()
      throws Exception {

    // Given
    String eventUrl = "https%3A%2F%2Fmarketplace.appdirect.com%2Fapi%2Fintegration%2Fv1%2Fevents%2F00000000-0000-0000-0000-000000000000";
    when(mockedAppDirectFetchEventService.fetch(eq(eventUrl)))
        .thenReturn(aNotification());
    doThrow(NotificationProcessorException.class)
        .when(mockedNotificationProcessorService).process(any(AppDirectNotification.class));

    // Execute & Verify
    mockMvc.perform(
        get("/appdirect/")
            .param("url", eventUrl))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.success", is(false)));
  }

  private AppDirectNotification aNotification() {
    return AppDirectNotification.builder().build();
  }

  private AppDirectApiResponse aSuccessfulApiResponse() {
    return AppDirectApiResponse.builder()
        .success(true)
        .build();
  }
}
