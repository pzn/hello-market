package com.github.pzn.hellomarket.controller.filter;

import static oauth.signpost.OAuth.HTTP_AUTHORIZATION_HEADER;
import static oauth.signpost.OAuth.OAUTH_SIGNATURE;
import static oauth.signpost.OAuth.oauthHeaderToParamsMap;
import static oauth.signpost.OAuth.percentDecode;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.http.HttpParameters;
import oauth.signpost.http.HttpRequest;

@Slf4j
public class OAuthRequestFilter implements Filter {

  private OAuthConsumer oAuthConsumer;

  public OAuthRequestFilter(OAuthConsumer oAuthConsumer) {
    this.oAuthConsumer = oAuthConsumer;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) servletRequest;
    HttpRequest wrappedRequest = new HttpServletOAuth(req);

    String requestOAuthSignature = extractOAuthSignature(wrappedRequest);

    log.debug("Validating OAuth Signature Request...");
    try {
      oAuthConsumer.sign(wrappedRequest);
    } catch (OAuthMessageSignerException|OAuthExpectationFailedException|OAuthCommunicationException e) {
      throw new IllegalArgumentException(e);
    }

    String signedOAuthSignature = extractOAuthSignature(wrappedRequest);

    if (!requestOAuthSignature.equals(signedOAuthSignature)) {
      throw new IllegalArgumentException("Invalid OAuth Signature");
    }
    log.debug("Validating OAuth Signature Request... Done.");
    filterChain.doFilter(servletRequest, servletResponse);
  }

  @Override
  public void destroy() {
  }

  private String extractOAuthSignature(HttpRequest req) {

    String authorization = req.getHeader(HTTP_AUTHORIZATION_HEADER);

    HttpParameters oAuthHeaderParams = oauthHeaderToParamsMap(authorization);

    String oAuthSignature = percentDecode(oAuthHeaderParams.getFirst(OAUTH_SIGNATURE));
    if (oAuthSignature == null) {
      oAuthSignature = req.getHeader(OAUTH_SIGNATURE);
    }
    return oAuthSignature;
  }

  private static class HttpServletOAuth implements HttpRequest {

    private HttpServletRequest req;
    private Map<String, String> headers = new HashMap<>(1);

    public HttpServletOAuth(HttpServletRequest req) {
      this.req = req;
      headers.put(HTTP_AUTHORIZATION_HEADER, req.getHeader(HTTP_AUTHORIZATION_HEADER));
    }

    @Override
    public String getMethod() {
      return req.getMethod();
    }

    @Override
    public String getRequestUrl() {
      return req.getRequestURL().toString() + "?" + req.getQueryString();
    }

    @Override
    public void setRequestUrl(String s) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setHeader(String key, String value) {
      headers.put(key, value);
    }

    @Override
    public String getHeader(String key) {
      return getAllHeaders().get(key);
    }

    @Override
    public Map<String, String> getAllHeaders() {
      return headers;
    }

    @Override
    public InputStream getMessagePayload() throws IOException {
      return req.getInputStream();
    }

    @Override
    public String getContentType() {
      return req.getContentType();
    }

    @Override
    public Object unwrap() {
      throw new UnsupportedOperationException();
    }
  }
}
