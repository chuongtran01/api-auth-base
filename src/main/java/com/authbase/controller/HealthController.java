package com.authbase.controller;

import com.authbase.service.RedisTokenService;
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
 * Provides endpoints to check the health of various components including Redis.
 */
@RestController
@RequestMapping("/health")
@Slf4j
@RequiredArgsConstructor
public class HealthController {

  private final RedisTokenService redisTokenService;

  /**
   * Basic health check endpoint.
   * 
   * @return health status
   */
  @GetMapping
  public ResponseEntity<Map<String, Object>> health() {
    Map<String, Object> health = new HashMap<>();
    health.put("status", "UP");
    health.put("timestamp", LocalDateTime.now().toString());
    health.put("service", "api-auth-base");
    health.put("version", "0.0.1-SNAPSHOT");

    return ResponseEntity.ok(health);
  }

  /**
   * Detailed health check including Redis status.
   * 
   * @return detailed health status
   */
  @GetMapping("/detailed")
  public ResponseEntity<Map<String, Object>> detailedHealth() {
    Map<String, Object> health = new HashMap<>();
    health.put("status", "UP");
    health.put("timestamp", LocalDateTime.now().toString());
    health.put("service", "api-auth-base");
    health.put("version", "0.0.1-SNAPSHOT");

    // Check Redis status
    Map<String, Object> redis = new HashMap<>();
    boolean redisAvailable = redisTokenService.isRedisAvailable();
    redis.put("status", redisAvailable ? "UP" : "DOWN");
    redis.put("enabled", redisTokenService.isRedisAvailable());
    health.put("redis", redis);

    // Overall status
    String overallStatus = redisAvailable ? "UP" : "DEGRADED";
    health.put("overallStatus", overallStatus);

    return ResponseEntity.ok(health);
  }

  /**
   * Redis-specific health check.
   * 
   * @return Redis health status
   */
  @GetMapping("/redis")
  public ResponseEntity<Map<String, Object>> redisHealth() {
    Map<String, Object> redis = new HashMap<>();
    boolean redisAvailable = redisTokenService.isRedisAvailable();

    redis.put("status", redisAvailable ? "UP" : "DOWN");
    redis.put("timestamp", LocalDateTime.now().toString());
    redis.put("available", redisAvailable);

    if (redisAvailable) {
      redis.put("message", "Redis is available and responding");
    } else {
      redis.put("message", "Redis is not available or not responding");
    }

    return ResponseEntity.ok(redis);
  }
}