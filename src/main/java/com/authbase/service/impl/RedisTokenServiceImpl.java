package com.authbase.service.impl;

import com.authbase.config.RedisConfig;
import com.authbase.service.RedisTokenService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of RedisTokenService for token blacklisting and session
 * management.
 * Provides Redis-based operations for secure logout and session tracking.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RedisTokenServiceImpl implements RedisTokenService {

  private final RedisTemplate<String, Object> redisTemplate;
  private final RedisConfig redisConfig;
  private final ObjectMapper objectMapper;

  @Value("${auth.redis.token-blacklist-prefix:blacklist:}")
  private String tokenBlacklistPrefix;

  @Value("${auth.redis.session-prefix:session:}")
  private String sessionPrefix;

  @Value("${auth.redis.user-sessions-prefix:user_sessions:}")
  private String userSessionsPrefix;

  @Value("${auth.session.session-timeout:3600}")
  private long sessionTimeout;

  @Value("${auth.session.cleanup-interval:300}")
  private long cleanupInterval;

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
  public void storeUserSession(String userId, String sessionId, String token, String userAgent, String ipAddress) {
    if (!redisConfig.isRedisEnabled()) {
      log.debug("Redis is disabled, skipping session storage");
      return;
    }

    try {
      // Store session information
      String sessionKey = sessionPrefix + sessionId;
      Map<String, Object> sessionInfo = new HashMap<>();
      sessionInfo.put("userId", userId);
      sessionInfo.put("token", token);
      sessionInfo.put("userAgent", userAgent);
      sessionInfo.put("ipAddress", ipAddress);
      sessionInfo.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
      sessionInfo.put("lastActivity", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

      redisTemplate.opsForValue().set(sessionKey, sessionInfo, sessionTimeout, TimeUnit.SECONDS);

      // Add session to user's session set
      String userSessionsKey = userSessionsPrefix + userId;
      SetOperations<String, Object> setOps = redisTemplate.opsForSet();
      setOps.add(userSessionsKey, sessionId);
      redisTemplate.expire(userSessionsKey, sessionTimeout, TimeUnit.SECONDS);

      log.debug("Session stored for user: {} with session ID: {}", userId, sessionId);
    } catch (Exception e) {
      log.error("Failed to store user session: {}", e.getMessage());
    }
  }

  @Override
  public void removeUserSession(String userId, String sessionId) {
    if (!redisConfig.isRedisEnabled()) {
      log.debug("Redis is disabled, skipping session removal");
      return;
    }

    try {
      // Remove session information
      String sessionKey = sessionPrefix + sessionId;
      redisTemplate.delete(sessionKey);

      // Remove session from user's session set
      String userSessionsKey = userSessionsPrefix + userId;
      SetOperations<String, Object> setOps = redisTemplate.opsForSet();
      setOps.remove(userSessionsKey, sessionId);

      log.debug("Session removed for user: {} with session ID: {}", userId, sessionId);
    } catch (Exception e) {
      log.error("Failed to remove user session: {}", e.getMessage());
    }
  }

  @Override
  public Set<String> getUserSessions(String userId) {
    if (!redisConfig.isRedisEnabled()) {
      log.debug("Redis is disabled, returning empty session set");
      return Set.of();
    }

    try {
      String userSessionsKey = userSessionsPrefix + userId;
      SetOperations<String, Object> setOps = redisTemplate.opsForSet();
      Set<Object> sessionObjects = setOps.members(userSessionsKey);

      return sessionObjects.stream()
          .map(Object::toString)
          .collect(java.util.stream.Collectors.toSet());
    } catch (Exception e) {
      log.error("Failed to get user sessions: {}", e.getMessage());
      return Set.of();
    }
  }

  @Override
  public long getUserSessionCount(String userId) {
    if (!redisConfig.isRedisEnabled()) {
      return 0;
    }

    try {
      String userSessionsKey = userSessionsPrefix + userId;
      SetOperations<String, Object> setOps = redisTemplate.opsForSet();
      Long size = setOps.size(userSessionsKey);
      return size != null ? size : 0;
    } catch (Exception e) {
      log.error("Failed to get user session count: {}", e.getMessage());
      return 0;
    }
  }

  @Override
  public long removeAllUserSessions(String userId) {
    if (!redisConfig.isRedisEnabled()) {
      log.debug("Redis is disabled, skipping session removal");
      return 0;
    }

    try {
      Set<String> sessions = getUserSessions(userId);
      long removedCount = 0;

      for (String sessionId : sessions) {
        removeUserSession(userId, sessionId);
        removedCount++;
      }

      // Remove the user sessions set
      String userSessionsKey = userSessionsPrefix + userId;
      redisTemplate.delete(userSessionsKey);

      log.info("Removed {} sessions for user: {}", removedCount, userId);
      return removedCount;
    } catch (Exception e) {
      log.error("Failed to remove all user sessions: {}", e.getMessage());
      return 0;
    }
  }

  @Override
  public void updateSessionActivity(String sessionId) {
    if (!redisConfig.isRedisEnabled()) {
      return;
    }

    try {
      String sessionKey = sessionPrefix + sessionId;
      Object sessionInfo = redisTemplate.opsForValue().get(sessionKey);

      if (sessionInfo instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> session = (Map<String, Object>) sessionInfo;
        session.put("lastActivity", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        redisTemplate.opsForValue().set(sessionKey, session, sessionTimeout, TimeUnit.SECONDS);
        log.debug("Updated session activity for session: {}", sessionId);
      }
    } catch (Exception e) {
      log.error("Failed to update session activity: {}", e.getMessage());
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
  public long cleanupExpiredSessions() {
    if (!redisConfig.isRedisEnabled()) {
      return 0;
    }

    try {
      // Redis automatically expires keys, so we just log the cleanup
      log.debug("Session cleanup completed (automatic expiration)");
      return 0;
    } catch (Exception e) {
      log.error("Failed to cleanup expired sessions: {}", e.getMessage());
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

  @Override
  public String getSessionInfo(String sessionId) {
    if (!redisConfig.isRedisEnabled()) {
      return null;
    }

    try {
      String sessionKey = sessionPrefix + sessionId;
      Object sessionInfo = redisTemplate.opsForValue().get(sessionKey);

      if (sessionInfo != null) {
        return objectMapper.writeValueAsString(sessionInfo);
      }
      return null;
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize session info: {}", e.getMessage());
      return null;
    } catch (Exception e) {
      log.error("Failed to get session info: {}", e.getMessage());
      return null;
    }
  }

  @Override
  public String getUserActivity(String userId) {
    if (!redisConfig.isRedisEnabled()) {
      return "{}";
    }

    try {
      Map<String, Object> activity = new HashMap<>();
      activity.put("userId", userId);
      activity.put("activeSessions", getUserSessionCount(userId));
      activity.put("sessionIds", getUserSessions(userId));
      activity.put("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

      return objectMapper.writeValueAsString(activity);
    } catch (JsonProcessingException e) {
      log.error("Failed to serialize user activity: {}", e.getMessage());
      return "{}";
    } catch (Exception e) {
      log.error("Failed to get user activity: {}", e.getMessage());
      return "{}";
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

  /**
   * Generate a unique session ID.
   * 
   * @return unique session identifier
   */
  public String generateSessionId() {
    return UUID.randomUUID().toString();
  }
}