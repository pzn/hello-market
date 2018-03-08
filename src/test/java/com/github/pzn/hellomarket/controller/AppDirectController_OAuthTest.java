package com.github.pzn.hellomarket.controller;

import static com.google.common.collect.ImmutableMap.of;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.net.URL;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth.consumer.ProtectedResourceDetails;
import org.springframework.security.oauth.consumer.client.CoreOAuthConsumerSupport;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AppDirectController_OAuthTest {

    private static final String PATH = "/appdirect/tceridppa";
    private static final String URL_PARAM = "https://www.appdirect.com/api/integration/v1/events/00000000-0000-0000-0000-000000000000";

    @Autowired
    private FilterChainProxy securityFilterChain;
    @Autowired
    private ProtectedResourceDetails appDirectOAuthResourceDetails;

    private MockMvc mockMvc;

    @Before
    public void before() throws Exception {
        mockMvc = standaloneSetup(new AppDirectTestController())
            .addFilter(securityFilterChain, "/*")
            .build();
    }

    @Test
    public void return_200_when_oauth_is_valid() throws Exception {

        // Given
        MockHttpServletRequest request = get(PATH)
            .param("url", URL_PARAM)
            .buildRequest(null);
        request.addHeader(AUTHORIZATION, anOAuthAuthorizationHeader(request, of("url", URL_PARAM)));

        // Execute & Verify
        mockMvc.perform(servletContext -> request)
            .andExpect(status().isOk());
    }

    @Test
    public void return_401_when_oauth_authorization_is_invalid() throws Exception {

        // Execute & Verify
        mockMvc.perform(get(PATH)
            .header(AUTHORIZATION, anInvalidOAuthAuthorizationHeader())
            .param("url", URL_PARAM))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void return_403_when_oauth_authorization_is_absent() throws Exception {

        // Execute & Verify
        mockMvc.perform(get(PATH).param("url", URL_PARAM))
            .andExpect(status().isForbidden());
    }

    private String anOAuthAuthorizationHeader(HttpServletRequest request, Map<String, String> params) throws Exception {
        return new CoreOAuthConsumerSupport().getAuthorizationHeader(appDirectOAuthResourceDetails,
            null,
            new URL(request.getRequestURL().toString()),
            request.getMethod(),
            params);
    }

    private String anInvalidOAuthAuthorizationHeader() {
        return "Oauth "
            + "oauth_callback=\"callback\","
            + "oauth_consumer_key=\"" + appDirectOAuthResourceDetails.getConsumerKey() + "\","
            + "oauth_nonce=\"0\","
            + "oauth_signature_method=\"HMAC-SHA1\","
            + "oauth_timestamp=\"0\","
            + "oauth_version=\"1.0\","
            + "oauth_signature=\"badly-forged-signature\",";
    }

    @RestController
    private class AppDirectTestController {

        @GetMapping(PATH)
        public void tceridppa(@RequestParam("url") String url) {
        }
    }
}
