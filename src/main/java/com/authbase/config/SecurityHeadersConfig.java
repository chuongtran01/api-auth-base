package com.authbase.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Configuration for security headers (Helmet equivalent for Spring Boot).
 * Adds various security headers to HTTP responses to enhance security.
 */
@Configuration
public class SecurityHeadersConfig {

  /**
   * Configure security headers for all requests.
   * 
   * @return HeaderWriter that adds security headers
   */
  @Bean
  public HeaderWriter securityHeadersWriter() {
    return new StaticHeadersWriter("X-Content-Type-Options", "nosniff");
  }

  /**
   * Configure X-Frame-Options header to prevent clickjacking.
   * 
   * @return HeaderWriter for X-Frame-Options
   */
  @Bean
  public HeaderWriter frameOptionsHeaderWriter() {
    return new StaticHeadersWriter("X-Frame-Options", "DENY");
  }

  /**
   * Configure X-XSS-Protection header.
   * 
   * @return HeaderWriter for X-XSS-Protection
   */
  @Bean
  public HeaderWriter xssProtectionHeaderWriter() {
    return new StaticHeadersWriter("X-XSS-Protection", "1; mode=block");
  }

  /**
   * Configure Referrer-Policy header.
   * 
   * @return HeaderWriter for Referrer-Policy
   */
  @Bean
  public HeaderWriter referrerPolicyHeaderWriter() {
    return new StaticHeadersWriter("Referrer-Policy", "strict-origin-when-cross-origin");
  }

  /**
   * Configure Content-Security-Policy header.
   * 
   * @return HeaderWriter for Content-Security-Policy
   */
  @Bean
  public HeaderWriter contentSecurityPolicyHeaderWriter() {
    String csp = "default-src 'self'; " +
        "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
        "style-src 'self' 'unsafe-inline'; " +
        "img-src 'self' data: https:; " +
        "font-src 'self' data:; " +
        "connect-src 'self'; " +
        "frame-ancestors 'none'; " +
        "base-uri 'self'; " +
        "form-action 'self'";

    return new StaticHeadersWriter("Content-Security-Policy", csp);
  }

  /**
   * Configure Permissions-Policy header (formerly Feature-Policy).
   * 
   * @return HeaderWriter for Permissions-Policy
   */
  @Bean
  public HeaderWriter permissionsPolicyHeaderWriter() {
    String permissionsPolicy = "geolocation=(), " +
        "microphone=(), " +
        "camera=(), " +
        "payment=(), " +
        "usb=(), " +
        "magnetometer=(), " +
        "gyroscope=(), " +
        "accelerometer=()";

    return new StaticHeadersWriter("Permissions-Policy", permissionsPolicy);
  }

  /**
   * Configure Strict-Transport-Security header for HTTPS.
   * Only applied to HTTPS requests.
   * 
   * @return HeaderWriter for HSTS
   */
  @Bean
  public HeaderWriter hstsHeaderWriter() {
    RequestMatcher httpsMatcher = new AntPathRequestMatcher("/**");
    return new DelegatingRequestMatcherHeaderWriter(httpsMatcher,
        new StaticHeadersWriter("Strict-Transport-Security", "max-age=31536000; includeSubDomains"));
  }

  /**
   * Configure Cache-Control header for API responses.
   * 
   * @return HeaderWriter for Cache-Control
   */
  @Bean
  public HeaderWriter cacheControlHeaderWriter() {
    return new StaticHeadersWriter("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
  }

  /**
   * Configure Pragma header for backward compatibility.
   * 
   * @return HeaderWriter for Pragma
   */
  @Bean
  public HeaderWriter pragmaHeaderWriter() {
    return new StaticHeadersWriter("Pragma", "no-cache");
  }

  /**
   * Configure Expires header.
   * 
   * @return HeaderWriter for Expires
   */
  @Bean
  public HeaderWriter expiresHeaderWriter() {
    return new StaticHeadersWriter("Expires", "0");
  }
}