package com.authbase.service.impl;

import com.authbase.entity.RefreshToken;
import com.authbase.entity.User;
import com.authbase.repository.RefreshTokenRepository;
import com.authbase.security.JwtTokenProvider;
import com.authbase.service.AuthenticationService;
import com.authbase.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of AuthenticationService interface.
 * Handles login, JWT token generation, refresh token management, and logout.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserService userService;
  private final RefreshTokenRepository refreshTokenRepository;

  @Override
  public AuthenticationResult authenticate(String username, String password) {
    log.info("Authenticating user: {}", username);

    try {
      // Authenticate with Spring Security
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(username, password));

      User user = (User) authentication.getPrincipal();

      // Check if user account is enabled
      if (!user.isEnabled()) {
        throw new IllegalArgumentException("User account is disabled");
      }

      // Update last login timestamp
      userService.updateLastLogin(user.getId());

      // Generate tokens
      String accessToken = jwtTokenProvider.generateAccessToken(authentication);
      String refreshToken = generateRefreshToken(user);

      log.info("User authenticated successfully: {}", user.getEmail());

      return new AuthenticationResult(accessToken, refreshToken, user);

    } catch (AuthenticationException e) {
      log.warn("Authentication failed for user: {}", username);
      throw new BadCredentialsException("Invalid username or password");
    }
  }

  @Override
  public AuthenticationResult refreshToken(String refreshTokenString) {
    log.debug("Refreshing token");

    // Find refresh token in database
    Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(refreshTokenString);
    if (refreshTokenOpt.isEmpty()) {
      throw new IllegalArgumentException("Invalid refresh token");
    }

    RefreshToken refreshToken = refreshTokenOpt.get();

    // Check if refresh token is expired
    if (refreshToken.isExpired()) {
      refreshTokenRepository.delete(refreshToken);
      throw new IllegalArgumentException("Refresh token has expired");
    }

    // Get user
    User user = refreshToken.getUser();
    if (!user.isEnabled()) {
      throw new IllegalArgumentException("User account is disabled");
    }

    // Generate new access token
    String newAccessToken = jwtTokenProvider.generateAccessToken(
        new UsernamePasswordAuthenticationToken(user, null, createAuthorities(user)));

    log.info("Token refreshed successfully for user: {}", user.getEmail());

    return new AuthenticationResult(newAccessToken, refreshTokenString, user);
  }

  @Override
  public boolean logout(String refreshTokenString) {
    log.info("Logging out user with refresh token");

    Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(refreshTokenString);
    if (refreshTokenOpt.isPresent()) {
      refreshTokenRepository.delete(refreshTokenOpt.get());
      log.info("User logged out successfully");
      return true;
    }

    log.warn("Refresh token not found for logout");
    return false;
  }

  @Override
  public boolean logoutByUserId(Long userId) {
    log.info("Logging out user by ID: {}", userId);

    // Delete all refresh tokens for the user
    refreshTokenRepository.deleteByUserId(userId);
    log.info("All sessions terminated for user ID: {}", userId);
    return true;
  }

  @Override
  @Transactional(readOnly = true)
  public boolean validateRefreshToken(String refreshTokenString) {
    Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(refreshTokenString);

    if (refreshTokenOpt.isEmpty()) {
      return false;
    }

    RefreshToken refreshToken = refreshTokenOpt.get();
    return !refreshToken.isExpired() && refreshToken.getUser().isEnabled();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<User> getUserFromRefreshToken(String refreshTokenString) {
    Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(refreshTokenString);

    if (refreshTokenOpt.isEmpty()) {
      return Optional.empty();
    }

    RefreshToken refreshToken = refreshTokenOpt.get();
    if (refreshToken.isExpired()) {
      return Optional.empty();
    }

    return Optional.of(refreshToken.getUser());
  }

  @Override
  public long cleanupExpiredTokens() {
    log.info("Cleaning up expired refresh tokens");

    long deletedCount = refreshTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    log.info("Deleted {} expired refresh tokens", deletedCount);

    return deletedCount;
  }

  @Override
  @Transactional(readOnly = true)
  public long getActiveSessionsCount(Long userId) {
    return refreshTokenRepository.countByUserIdAndExpiryDateAfter(userId, LocalDateTime.now());
  }

  /**
   * Generate a new refresh token for user.
   * 
   * @param user user to generate token for
   * @return refresh token string
   */
  private String generateRefreshToken(User user) {
    // Generate unique token
    String token = UUID.randomUUID().toString();

    // Calculate expiry date (7 days from now)
    LocalDateTime expiryDate = LocalDateTime.now().plusDays(7);

    // Create refresh token entity
    RefreshToken refreshToken = new RefreshToken(token, user, expiryDate);
    refreshTokenRepository.save(refreshToken);

    log.debug("Generated refresh token for user: {}", user.getEmail());
    return token;
  }

  /**
   * Create authorities from user roles.
   * 
   * @param user the user
   * @return list of authorities
   */
  private List<SimpleGrantedAuthority> createAuthorities(User user) {
    return user.getRoles().stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
        .collect(Collectors.toList());
  }
}