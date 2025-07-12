package com.authbase.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for monitoring application status.
 * Provides basic health information and status endpoints.
 */
@RestController
@RequestMapping("/api/health")
@Slf4j
@RequiredArgsConstructor
public class HealthController {

  /**
   * Basic health check endpoint.
   * 
   * @return health status information
   */
  @GetMapping
  public ResponseEntity<Map<String, Object>> health() {
    log.debug("Health check requested");

    Map<String, Object> health = new HashMap<>();
    health.put("status", "UP");
    health.put("timestamp", LocalDateTime.now().toString());
    health.put("service", "api-auth-base");
    health.put("version", "0.0.1-SNAPSHOT");

    return ResponseEntity.ok(health);
  }

  /**
   * Detailed health check endpoint.
   * 
   * @return detailed health status information
   */
  @GetMapping("/details")
  public ResponseEntity<Map<String, Object>> healthDetails() {
    log.debug("Detailed health check requested");

    Map<String, Object> health = new HashMap<>();
    health.put("status", "UP");
    health.put("timestamp", LocalDateTime.now().toString());
    health.put("service", "api-auth-base");
    health.put("version", "0.0.1-SNAPSHOT");
    health.put("description", "JWT Authentication Base for Spring Boot");
    health.put("features", new String[] {
        "JWT Authentication",
        "Role-based Access Control",
        "Multiple Roles per User",
        "Database Migrations",
        "CORS Configuration"
    });

    return ResponseEntity.ok(health);
  }
}