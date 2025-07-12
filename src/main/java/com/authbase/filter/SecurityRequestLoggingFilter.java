package com.authbase.filter;

import com.authbase.service.SecurityEventService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Filter for logging security-relevant request information.
 * Logs request details for security audit and monitoring purposes.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SecurityRequestLoggingFilter extends OncePerRequestFilter {

  private final SecurityEventService securityEventService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    // Generate unique request ID for tracking
    String requestId = UUID.randomUUID().toString();

    // Get request details
    String method = request.getMethod();
    String uri = request.getRequestURI();
    String queryString = request.getQueryString();
    String ipAddress = getClientIpAddress(request);
    String userAgent = request.getHeader("User-Agent");
    String referer = request.getHeader("Referer");

    // Log request start
    long startTime = System.currentTimeMillis();

    try {
      // Continue with the filter chain
      filterChain.doFilter(request, response);

      // Log successful request
      long duration = System.currentTimeMillis() - startTime;
      int statusCode = response.getStatus();

      logSecurityRequest(requestId, method, uri, queryString, ipAddress, userAgent,
          referer, statusCode, duration, true, null);

    } catch (Exception e) {
      // Log failed request
      long duration = System.currentTimeMillis() - startTime;
      int statusCode = response.getStatus();

      logSecurityRequest(requestId, method, uri, queryString, ipAddress, userAgent,
          referer, statusCode, duration, false, e.getMessage());

      throw e;
    }
  }

  /**
   * Log security-relevant request information.
   */
  private void logSecurityRequest(String requestId, String method, String uri, String queryString,
      String ipAddress, String userAgent, String referer,
      int statusCode, long duration, boolean success, String errorMessage) {

    // Determine if this is a security-sensitive endpoint
    boolean isSecurityEndpoint = isSecuritySensitiveEndpoint(uri, method);

    if (isSecurityEndpoint) {
      log.info("Security Request [{}] - {} {} - IP: {} - Status: {} - Duration: {}ms - Success: {}",
          requestId, method, uri, ipAddress, statusCode, duration, success);

      // Log suspicious activity for failed security endpoints
      if (!success && isSecurityEndpoint) {
        String details = String.format("Failed security request: %s %s - Status: %d - Error: %s",
            method, uri, statusCode, errorMessage != null ? errorMessage : "Unknown error");

        securityEventService.logSuspiciousActivity(ipAddress, userAgent, details);
      }
    } else {
      log.debug("Request [{}] - {} {} - IP: {} - Status: {} - Duration: {}ms",
          requestId, method, uri, ipAddress, statusCode, duration);
    }
  }

  /**
   * Check if the endpoint is security-sensitive.
   */
  private boolean isSecuritySensitiveEndpoint(String uri, String method) {
    // Authentication endpoints
    if (uri.startsWith("/api/auth/")) {
      return true;
    }

    // Admin endpoints
    if (uri.startsWith("/api/admin/")) {
      return true;
    }

    // User profile endpoints
    if (uri.startsWith("/api/users/") && (method.equals("PUT") || method.equals("DELETE"))) {
      return true;
    }

    // Password-related endpoints
    if (uri.contains("/password") || uri.contains("/reset")) {
      return true;
    }

    return false;
  }

  /**
   * Extract client IP address from the request.
   */
  private String getClientIpAddress(HttpServletRequest request) {
    String ipAddress = request.getHeader("X-Forwarded-For");
    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("Proxy-Client-IP");
    }
    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("HTTP_CLIENT_IP");
    }
    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
    }
    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getRemoteAddr();
    }
    return ipAddress;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    // Skip logging for static resources and health checks
    String uri = request.getRequestURI();
    return uri.startsWith("/static/") ||
        uri.startsWith("/css/") ||
        uri.startsWith("/js/") ||
        uri.startsWith("/images/") ||
        uri.startsWith("/favicon.ico") ||
        uri.equals("/actuator/health");
  }
}