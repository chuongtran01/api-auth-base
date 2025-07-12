package com.authbase.service;

/**
 * Service interface for Redis-based token blacklisting.
 * Handles token blacklisting and cleanup operations.
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
   * Clean up expired tokens from blacklist.
   * 
   * @return number of tokens cleaned up
   */
  long cleanupExpiredTokens();

  /**
   * Check if Redis is available and enabled.
   * 
   * @return true if Redis is available, false otherwise
   */
  boolean isRedisAvailable();
}