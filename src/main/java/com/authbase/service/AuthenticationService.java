package com.authbase.service;

import com.authbase.entity.User;

import java.util.Optional;

/**
 * Service interface for authentication operations.
 * Handles login, JWT token generation, refresh token management, and logout.
 */
public interface AuthenticationService {

  /**
   * Authenticate user and generate JWT tokens.
   * 
   * @param username username or email
   * @param password password
   * @return AuthenticationResult containing access and refresh tokens
   * @throws org.springframework.security.authentication.BadCredentialsException if
   *                                                                             credentials
   *                                                                             are
   *                                                                             invalid
   * @throws IllegalArgumentException                                            if
   *                                                                             user
   *                                                                             account
   *                                                                             is
   *                                                                             disabled
   */
  AuthenticationResult authenticate(String username, String password);

  /**
   * Refresh access token using refresh token.
   * 
   * @param refreshTokenString refresh token string
   * @return new AuthenticationResult with new access token
   * @throws IllegalArgumentException if refresh token is invalid or expired
   */
  AuthenticationResult refreshToken(String refreshTokenString);

  /**
   * Logout user by invalidating refresh token and blacklisting access token.
   * 
   * @param refreshTokenString refresh token to invalidate
   * @param accessToken        access token to blacklist (can be null)
   * @return true if logout successful
   */
  boolean logout(String refreshTokenString, String accessToken);

  /**
   * Logout user by user ID (admin function).
   * 
   * @param userId user ID to logout
   * @return true if logout successful
   */
  boolean logoutByUserId(Long userId);

  /**
   * Validate refresh token.
   * 
   * @param refreshTokenString refresh token to validate
   * @return true if valid, false otherwise
   */
  boolean validateRefreshToken(String refreshTokenString);

  /**
   * Get user from refresh token.
   * 
   * @param refreshTokenString refresh token string
   * @return Optional containing user if token is valid
   */
  Optional<User> getUserFromRefreshToken(String refreshTokenString);

  /**
   * Clean up expired refresh tokens.
   * 
   * @return number of tokens deleted
   */
  long cleanupExpiredTokens();

  /**
   * Get active sessions count for user.
   * 
   * @param userId user ID
   * @return number of active refresh tokens
   */
  long getActiveSessionsCount(Long userId);

  /**
   * Result class for authentication operations.
   */
  class AuthenticationResult {
    private final String accessToken;
    private final String refreshToken;
    private final User user;

    public AuthenticationResult(String accessToken, String refreshToken, User user) {
      this.accessToken = accessToken;
      this.refreshToken = refreshToken;
      this.user = user;
    }

    public String getAccessToken() {
      return accessToken;
    }

    public String getRefreshToken() {
      return refreshToken;
    }

    public User getUser() {
      return user;
    }
  }
}