package com.authbase.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration properties for CORS settings.
 * These can be customized per environment in application.yml files.
 */
@Component
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

  /**
   * Allowed origin patterns (e.g., "http://localhost:3000",
   * "https://*.example.com")
   */
  private List<String> allowedOriginPatterns = List.of("*");

  /**
   * Allowed HTTP methods
   */
  private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");

  /**
   * Allowed headers
   */
  private List<String> allowedHeaders = List.of(
      "Origin",
      "Content-Type",
      "Accept",
      "Authorization",
      "X-Requested-With",
      "Access-Control-Request-Method",
      "Access-Control-Request-Headers");

  /**
   * Exposed headers to the client
   */
  private List<String> exposedHeaders = List.of(
      "Authorization",
      "X-Total-Count",
      "X-Page-Number",
      "X-Page-Size");

  /**
   * Whether to allow credentials (cookies, authorization headers)
   */
  private boolean allowCredentials = true;

  /**
   * Max age for preflight requests in seconds
   */
  private long maxAge = 3600L;

  // Getters and Setters
  public List<String> getAllowedOriginPatterns() {
    return allowedOriginPatterns;
  }

  public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
    this.allowedOriginPatterns = allowedOriginPatterns;
  }

  public List<String> getAllowedMethods() {
    return allowedMethods;
  }

  public void setAllowedMethods(List<String> allowedMethods) {
    this.allowedMethods = allowedMethods;
  }

  public List<String> getAllowedHeaders() {
    return allowedHeaders;
  }

  public void setAllowedHeaders(List<String> allowedHeaders) {
    this.allowedHeaders = allowedHeaders;
  }

  public List<String> getExposedHeaders() {
    return exposedHeaders;
  }

  public void setExposedHeaders(List<String> exposedHeaders) {
    this.exposedHeaders = exposedHeaders;
  }

  public boolean isAllowCredentials() {
    return allowCredentials;
  }

  public void setAllowCredentials(boolean allowCredentials) {
    this.allowCredentials = allowCredentials;
  }

  public long getMaxAge() {
    return maxAge;
  }

  public void setMaxAge(long maxAge) {
    this.maxAge = maxAge;
  }
}