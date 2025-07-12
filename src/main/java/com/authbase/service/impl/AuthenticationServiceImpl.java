package com.authbase.service.impl;

import com.authbase.entity.RefreshToken;
import com.authbase.entity.User;
import com.authbase.repository.RefreshTokenRepository;
import com.authbase.security.JwtTokenProvider;
import com.authbase.service.AuthenticationService;
import com.authbase.service.RedisTokenService;
import com.authbase.service.SecurityEventService;
import com.authbase.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
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
  private final RedisTokenService redisTokenService;
  private final SecurityEventService securityEventService;

  // Account lockout configuration
  private static final int MAX_FAILED_ATTEMPTS = 5;
  private static final int LOCKOUT_DURATION_MINUTES = 15;

  @Override
  public AuthenticationResult authenticate(String username, String password, String ipAddress, String userAgent) {
    log.info("Authenticating user: {}", username);

    try {
      // Check if user exists and get user details
      Optional<User> userOpt = userService.findByEmail(username);
      if (userOpt.isEmpty()) {
        log.warn("Login attempt with non-existent email: {}", username);
        securityEventService.logLoginAttempt(null, ipAddress, userAgent, false,
            "Login attempt with non-existent email: " + username);
        throw new BadCredentialsException("Invalid username or password");
      }

      User user = userOpt.get();

      // Check if account is locked
      if (user.isAccountLocked()) {
        log.warn("Login attempt for locked account: {}", username);
        securityEventService.logLoginAttempt(user, ipAddress, userAgent, false,
            "Login attempt for locked account");
        throw new LockedException("Account is locked due to multiple failed login attempts");
      }

      // Check if lockout period has expired and reset if needed
      if (user.isAccountLockedExpired()) {
        user.resetFailedLoginAttempts();
        userService.saveUser(user);
        log.info("Account lockout expired for user: {}", username);
      }

      // Authenticate with Spring Security
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(username, password));

      // Check if user account is enabled
      if (!user.isEnabled()) {
        log.warn("Login attempt for disabled account: {}", username);
        securityEventService.logLoginAttempt(user, ipAddress, userAgent, false,
            "Login attempt for disabled account");
        throw new DisabledException("User account is disabled");
      }

      // Reset failed login attempts on successful login
      if (user.getFailedLoginAttempts() > 0) {
        user.resetFailedLoginAttempts();
        userService.saveUser(user);
        log.info("Reset failed login attempts for user: {}", username);
      }

      // Update last login timestamp
      userService.updateLastLogin(user.getId());

      // Log successful login
      securityEventService.logLoginAttempt(user, ipAddress, userAgent, true, "Login successful");

      // Generate tokens
      String accessToken = jwtTokenProvider.generateAccessToken(user);
      String refreshToken = generateRefreshToken(user);

      log.info("User authenticated successfully: {}", user.getEmail());

      return new AuthenticationResult(accessToken, refreshToken, user);

    } catch (BadCredentialsException e) {
      // Handle failed login attempt
      handleFailedLoginAttempt(username, ipAddress, userAgent);
      throw e;
    } catch (AuthenticationException e) {
      log.warn("Authentication failed for user: {}", username);
      securityEventService.logLoginAttempt(null, ipAddress, userAgent, false,
          "Authentication failed: " + e.getMessage());
      throw new BadCredentialsException("Invalid username or password");
    }
  }

  /**
   * Handle failed login attempt with account lockout logic.
   */
  private void handleFailedLoginAttempt(String email, String ipAddress, String userAgent) {
    Optional<User> userOpt = userService.findByEmail(email);

    if (userOpt.isPresent()) {
      User user = userOpt.get();

      // Increment failed login attempts
      user.incrementFailedLoginAttempts();

      // Check if account should be locked
      if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
        LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES);
        user.lockAccount(lockUntil);

        log.warn("Account locked for user: {} due to {} failed attempts", email, MAX_FAILED_ATTEMPTS);
        securityEventService.logAccountLockout(user, ipAddress, userAgent,
            "Account locked after " + MAX_FAILED_ATTEMPTS + " failed login attempts");
      }

      userService.saveUser(user);

      // Log failed login attempt
      securityEventService.logLoginAttempt(user, ipAddress, userAgent, false,
          "Failed login attempt #" + user.getFailedLoginAttempts());
    } else {
      // Log failed attempt for non-existent user
      securityEventService.logLoginAttempt(null, ipAddress, userAgent, false,
          "Failed login attempt for non-existent user: " + email);
    }
  }

  @Override
  public AuthenticationResult authenticate(String username, String password) {
    // Default implementation without IP and user agent for backward compatibility
    return authenticate(username, password, "unknown", "unknown");
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
      throw new DisabledException("User account is disabled");
    }

    // Generate new access token
    String newAccessToken = jwtTokenProvider.generateAccessToken(user);

    log.info("Token refreshed successfully for user: {}", user.getEmail());

    return new AuthenticationResult(newAccessToken, refreshTokenString, user);
  }

  @Override
  public boolean logout(String refreshTokenString, String accessToken) {
    log.info("Logging out user with refresh token and blacklisting access token");

    // Blacklist the access token
    if (accessToken != null && !accessToken.trim().isEmpty()) {
      try {
        // Get token expiration time
        long expirationTime = jwtTokenProvider.getExpirationDateFromToken(accessToken).getTime();
        redisTokenService.blacklistToken(accessToken, expirationTime);
        log.info("Access token blacklisted successfully");
      } catch (Exception e) {
        log.warn("Failed to blacklist access token: {}", e.getMessage());
        // Continue with logout even if blacklisting fails
      }
    }

    // Delete refresh token from database
    Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(refreshTokenString);
    if (refreshTokenOpt.isPresent()) {
      refreshTokenRepository.delete(refreshTokenOpt.get());
      log.info("User logged out successfully from current device");
      return true;
    }

    log.warn("Refresh token not found for logout");
    return false;
  }

  @Override
  public boolean logoutByUserId(Long userId) {
    log.info("Logging out user by ID: {}", userId);

    // Get all active refresh tokens for the user
    List<RefreshToken> userRefreshTokens = refreshTokenRepository.findByUserId(userId);

    // The user will need to wait for their access tokens to expire (15 minutes)
    log.info("Found {} active refresh tokens for user ID: {}", userRefreshTokens.size(), userId);

    // Delete all refresh tokens for the user
    refreshTokenRepository.deleteByUserId(userId);
    log.info("All refresh tokens deleted for user ID: {}", userId);

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