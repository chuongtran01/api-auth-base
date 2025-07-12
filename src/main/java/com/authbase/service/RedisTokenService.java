package com.authbase.service;

import java.util.Set;

/**
 * Service interface for Redis-based token and session management.
 * Handles token blacklisting, session tracking, and cleanup operations.
 */
public interface RedisTokenService {

  /**
   * Blacklist a JWT token to prevent its use after logout.
   * 
   * @param token          JWT token to blacklist
   * @param expirationTime token expiration time in milliseconds
   */
  void blacklistToken(String token, long expirationTime);

  /**
   * Check if a JWT token is blacklisted.
   * 
   * @param token JWT token to check
   * @return true if token is blacklisted, false otherwise
   */
  boolean isTokenBlacklisted(String token);

  /**
   * Store user session information in Redis.
   * 
   * @param userId    user ID
   * @param sessionId session identifier
   * @param token     JWT token associated with the session
   * @param userAgent user agent string
   * @param ipAddress IP address of the session
   */
  void storeUserSession(String userId, String sessionId, String token, String userAgent, String ipAddress);

  /**
   * Remove a specific user session from Redis.
   * 
   * @param userId    user ID
   * @param sessionId session identifier to remove
   */
  void removeUserSession(String userId, String sessionId);

  /**
   * Get all active sessions for a user.
   * 
   * @param userId user ID
   * @return set of session IDs
   */
  Set<String> getUserSessions(String userId);

  /**
   * Get the count of active sessions for a user.
   * 
   * @param userId user ID
   * @return number of active sessions
   */
  long getUserSessionCount(String userId);

  /**
   * Remove all sessions for a user (force logout from all devices).
   * 
   * @param userId user ID
   * @return number of sessions removed
   */
  long removeAllUserSessions(String userId);

  /**
   * Update session activity timestamp.
   * 
   * @param sessionId session identifier
   */
  void updateSessionActivity(String sessionId);

  /**
   * Clean up expired tokens from blacklist.
   * 
   * @return number of tokens cleaned up
   */
  long cleanupExpiredTokens();

  /**
   * Clean up expired sessions.
   * 
   * @return number of sessions cleaned up
   */
  long cleanupExpiredSessions();

  /**
   * Check if Redis is available and enabled.
   * 
   * @return true if Redis is available, false otherwise
   */
  boolean isRedisAvailable();

  /**
   * Get session information for a specific session.
   * 
   * @param sessionId session identifier
   * @return session information as JSON string, or null if not found
   */
  String getSessionInfo(String sessionId);

  /**
   * Get user activity statistics.
   * 
   * @param userId user ID
   * @return user activity information as JSON string
   */
  String getUserActivity(String userId);
}