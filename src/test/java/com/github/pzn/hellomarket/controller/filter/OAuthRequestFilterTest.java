package com.github.pzn.hellomarket.controller.filter;

import static oauth.signpost.OAuth.HTTP_AUTHORIZATION_HEADER;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

@RunWith(MockitoJUnitRunner.class)
public class OAuthRequestFilterTest {

  @InjectMocks
  private OAuthRequestFilter filter;
  @Spy
  private OAuthConsumer oAuthConsumer = new DefaultOAuthConsumer("hello-market-193427", "80eXbPZDdlSJKclo");
  @Mock
  private HttpServletResponse res;
  @Mock
  private FilterChain filterChain;

  @Test
  @Ignore("TODO create a proper working HttpServletRequest")
  public void can_filter_valid_request_signature() throws Exception {

    // Given
    HttpServletRequest req = givenValidRequest();

    // Execute
    filter.doFilter(req, res, filterChain);

    // Verify
    verify(filterChain).doFilter(eq(req), eq(res));
  }

  private HttpServletRequest givenValidRequest() {

    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setServerName("hellomarket.herokuapp.com");
    req.setServletPath("/appdirect");
    req.setQueryString("url=https%3A%2F%2Fmarketplace.appdirect.com%2Fapi%2Fintegration%2Fv1%2Fevents%2Fd9cf6547-30bb-4f5a-b357-02db520df5cb");
    req.setServerPort(443);
    req.setMethod("GET");
    req.addHeader(HTTP_AUTHORIZATION_HEADER, "OAuth oauth_consumer_key=\"hello-market-193427\", oauth_nonce=\"885968161484436082\", oauth_signature=\"m9DNHt2TjxoLN%2FS7kLnFImmgciw%3D\", oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"1520197381\", oauth_version=\"1.0\"");
    return req;
  }
}
