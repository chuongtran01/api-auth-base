package com.authbase.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration properties for whitelist endpoints.
 * These endpoints are publicly accessible without authentication.
 */
@Component
@ConfigurationProperties(prefix = "security.whitelist")
@Data
public class WhitelistProperties {

  /**
   * List of endpoint patterns that are publicly accessible.
   * These endpoints do not require authentication.
   * Configured in application.yml under security.whitelist.endpoints
   */
  private List<String> endpoints;

  /**
   * Check if a given path matches any of the whitelist patterns.
   * 
   * @param path the request path to check
   * @return true if the path is whitelisted, false otherwise
   */
  public boolean isWhitelisted(String path) {
    if (path == null || path.isEmpty() || endpoints == null || endpoints.isEmpty()) {
      return false;
    }

    return endpoints.stream()
        .anyMatch(pattern -> matchesPattern(path, pattern));
  }

  /**
   * Check if a path matches a pattern.
   * Supports both exact matches and Ant-style patterns.
   * 
   * @param path    the request path
   * @param pattern the pattern to match against
   * @return true if the path matches the pattern
   */
  private boolean matchesPattern(String path, String pattern) {
    // Handle exact matches
    if (pattern.equals(path)) {
      return true;
    }

    // Handle Ant-style patterns
    if (pattern.contains("*")) {
      return matchesAntPattern(path, pattern);
    }

    // Handle prefix matches
    if (pattern.endsWith("/**")) {
      String prefix = pattern.substring(0, pattern.length() - 2);
      return path.startsWith(prefix);
    }

    return false;
  }

  /**
   * Simple Ant-style pattern matching.
   * Supports single asterisk (*) and double asterisk (**) patterns.
   * 
   * @param path    the request path
   * @param pattern the Ant-style pattern
   * @return true if the path matches the pattern
   */
  private boolean matchesAntPattern(String path, String pattern) {
    // Convert pattern to regex
    String regex = pattern
        .replace("**", ".*") // ** matches any sequence
        .replace("*", "[^/]*"); // * matches any sequence except /

    return path.matches(regex);
  }

  /**
   * Get all whitelist patterns as a formatted string for logging.
   * 
   * @return formatted string of all patterns
   */
  public String getWhitelistSummary() {
    if (endpoints == null || endpoints.isEmpty()) {
      return "No whitelist endpoints configured";
    }
    return String.join(", ", endpoints);
  }

  /**
   * Get the number of whitelist patterns.
   * 
   * @return number of patterns
   */
  public int getWhitelistSize() {
    return endpoints != null ? endpoints.size() : 0;
  }

  /**
   * Check if whitelist configuration is loaded.
   * 
   * @return true if endpoints are configured, false otherwise
   */
  public boolean isConfigured() {
    return endpoints != null && !endpoints.isEmpty();
  }
}