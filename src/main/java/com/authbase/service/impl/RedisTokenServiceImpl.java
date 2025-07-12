package com.authbase.service.impl;

import com.authbase.config.RedisConfig;
import com.authbase.service.RedisTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Implementation of RedisTokenService for token blacklisting.
 * Provides Redis-based operations for secure logout.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RedisTokenServiceImpl implements RedisTokenService {

  private final RedisTemplate<String, Object> redisTemplate;
  private final RedisConfig redisConfig;

  @Value("${auth.redis.token-blacklist-prefix:blacklist:}")
  private String tokenBlacklistPrefix;

  @Override
  public void blacklistToken(String token, long expirationTime) {
    if (!redisConfig.isRedisEnabled()) {
      log.debug("Redis is disabled, skipping token blacklisting");
      return;
    }

    try {
      String key = tokenBlacklistPrefix + token;
      long ttl = calculateTTL(expirationTime);

      redisTemplate.opsForValue().set(key, "blacklisted", ttl, TimeUnit.SECONDS);
      log.debug("Token blacklisted with TTL: {} seconds", ttl);
    } catch (Exception e) {
      log.error("Failed to blacklist token: {}", e.getMessage());
    }
  }

  @Override
  public boolean isTokenBlacklisted(String token) {
    if (!redisConfig.isRedisEnabled()) {
      log.debug("Redis is disabled, token not blacklisted");
      return false;
    }

    try {
      String key = tokenBlacklistPrefix + token;
      Boolean exists = redisTemplate.hasKey(key);
      return Boolean.TRUE.equals(exists);
    } catch (Exception e) {
      log.error("Failed to check token blacklist: {}", e.getMessage());
      return false;
    }
  }

  @Override
  public long cleanupExpiredTokens() {
    if (!redisConfig.isRedisEnabled()) {
      return 0;
    }

    try {
      // Redis automatically expires keys, so we just log the cleanup
      log.debug("Token cleanup completed (automatic expiration)");
      return 0;
    } catch (Exception e) {
      log.error("Failed to cleanup expired tokens: {}", e.getMessage());
      return 0;
    }
  }

  @Override
  public boolean isRedisAvailable() {
    if (!redisConfig.isRedisEnabled()) {
      return false;
    }

    try {
      redisTemplate.opsForValue().get("health_check");
      return true;
    } catch (Exception e) {
      log.warn("Redis is not available: {}", e.getMessage());
      return false;
    }
  }

  /**
   * Calculate TTL (Time To Live) for token blacklisting.
   * Ensures blacklisted tokens are automatically removed when they would have
   * expired.
   * 
   * @param expirationTime token expiration time in milliseconds
   * @return TTL in seconds
   */
  private long calculateTTL(long expirationTime) {
    long currentTime = System.currentTimeMillis();
    long timeUntilExpiration = expirationTime - currentTime;

    // Add buffer time (5 minutes) to ensure cleanup
    return Math.max(300, timeUntilExpiration / 1000);
  }
}