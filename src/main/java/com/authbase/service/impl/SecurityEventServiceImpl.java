package com.authbase.service.impl;

import com.authbase.entity.SecurityEvent;
import com.authbase.entity.User;
import com.authbase.repository.SecurityEventRepository;
import com.authbase.service.SecurityEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of SecurityEventService for logging and managing security
 * events.
 * Provides comprehensive security event logging and analysis capabilities.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SecurityEventServiceImpl implements SecurityEventService {

  private final SecurityEventRepository securityEventRepository;

  @Override
  public SecurityEvent logSecurityEvent(SecurityEvent.SecurityEventType eventType, String description,
      String ipAddress, String userAgent, Boolean success, String details, User user) {
    try {
      SecurityEvent securityEvent = new SecurityEvent(eventType, description, ipAddress, userAgent, success, details,
          user);
      SecurityEvent savedEvent = securityEventRepository.save(securityEvent);

      log.info("Security event logged: {} - {} - User: {} - IP: {} - Success: {}",
          eventType, description, user != null ? user.getEmail() : "SYSTEM", ipAddress, success);

      return savedEvent;
    } catch (Exception e) {
      log.error("Failed to log security event: {} - {}", eventType, e.getMessage(), e);
      throw new RuntimeException("Failed to log security event", e);
    }
  }

  @Override
  public SecurityEvent logSecurityEvent(SecurityEvent.SecurityEventType eventType, String description,
      String ipAddress, String userAgent, Boolean success, String details) {
    return logSecurityEvent(eventType, description, ipAddress, userAgent, success, details, null);
  }

  @Override
  public SecurityEvent logLoginAttempt(User user, String ipAddress, String userAgent, Boolean success, String details) {
    SecurityEvent.SecurityEventType eventType = success ? SecurityEvent.SecurityEventType.LOGIN_SUCCESS
        : SecurityEvent.SecurityEventType.LOGIN_FAILURE;

    String description = success ? "Successful login attempt" : "Failed login attempt";

    return logSecurityEvent(eventType, description, ipAddress, userAgent, success, details, user);
  }

  @Override
  public SecurityEvent logLogout(User user, String ipAddress, String userAgent) {
    return logSecurityEvent(SecurityEvent.SecurityEventType.LOGOUT,
        "User logout", ipAddress, userAgent, true, "User logged out successfully", user);
  }

  @Override
  public SecurityEvent logAccountLockout(User user, String ipAddress, String userAgent, String reason) {
    return logSecurityEvent(SecurityEvent.SecurityEventType.ACCOUNT_LOCKED,
        "Account locked due to multiple failed login attempts", ipAddress, userAgent, false, reason, user);
  }

  @Override
  public SecurityEvent logAccountUnlock(User user, String ipAddress, String userAgent, String reason) {
    return logSecurityEvent(SecurityEvent.SecurityEventType.ACCOUNT_UNLOCKED,
        "Account unlocked", ipAddress, userAgent, true, reason, user);
  }

  @Override
  public SecurityEvent logPasswordChange(User user, String ipAddress, String userAgent, Boolean success) {
    SecurityEvent.SecurityEventType eventType = SecurityEvent.SecurityEventType.PASSWORD_CHANGE;
    String description = "Password change attempt";

    return logSecurityEvent(eventType, description, ipAddress, userAgent, success,
        success ? "Password changed successfully" : "Password change failed", user);
  }

  @Override
  public SecurityEvent logPasswordResetRequest(User user, String ipAddress, String userAgent) {
    return logSecurityEvent(SecurityEvent.SecurityEventType.PASSWORD_RESET_REQUEST,
        "Password reset request", ipAddress, userAgent, true, "Password reset email sent", user);
  }

  @Override
  public SecurityEvent logPasswordResetSuccess(User user, String ipAddress, String userAgent) {
    return logSecurityEvent(SecurityEvent.SecurityEventType.PASSWORD_RESET_SUCCESS,
        "Password reset successful", ipAddress, userAgent, true, "Password reset completed", user);
  }

  @Override
  public SecurityEvent logSuspiciousActivity(String ipAddress, String userAgent, String details) {
    return logSecurityEvent(SecurityEvent.SecurityEventType.SUSPICIOUS_ACTIVITY,
        "Suspicious activity detected", ipAddress, userAgent, false, details, null);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<SecurityEvent> getSecurityEventsByUser(Long userId, Pageable pageable) {
    return securityEventRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<SecurityEvent> getSecurityEventsByType(SecurityEvent.SecurityEventType eventType, Pageable pageable) {
    return securityEventRepository.findByEventTypeOrderByCreatedAtDesc(eventType, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<SecurityEvent> getSecurityEventsByTimeRange(LocalDateTime startDate, LocalDateTime endDate,
      Pageable pageable) {
    return securityEventRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public List<SecurityEvent> getRecentSecurityEvents(Pageable pageable) {
    return securityEventRepository.findRecentSecurityEvents(pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public long countFailedLoginAttempts(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
    return securityEventRepository.countFailedLoginAttemptsByUserAndTimeRange(userId, startDate, endDate);
  }

  @Override
  @Transactional(readOnly = true)
  public List<SecurityEvent> checkSuspiciousActivity(String ipAddress, LocalDateTime timeWindow) {
    return securityEventRepository.findSuspiciousActivityByIpAndTimeRange(ipAddress, timeWindow, LocalDateTime.now());
  }

  @Override
  public int cleanupOldSecurityEvents(LocalDateTime cutoffDate) {
    try {
      int deletedCount = securityEventRepository.deleteOldSecurityEvents(cutoffDate);
      log.info("Cleaned up {} old security events before {}", deletedCount, cutoffDate);
      return deletedCount;
    } catch (Exception e) {
      log.error("Failed to cleanup old security events: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to cleanup old security events", e);
    }
  }

  /**
   * Get security statistics for dashboard.
   * 
   * @param days number of days to look back
   * @return security statistics
   */
  public SecurityStatistics getSecurityStatistics(int days) {
    LocalDateTime startDate = LocalDateTime.now().minusDays(days);
    LocalDateTime endDate = LocalDateTime.now();

    long totalEvents = securityEventRepository.countEventsByTypeAndTimeRange(null, startDate, endDate);
    long failedLogins = securityEventRepository.countEventsByTypeAndTimeRange(
        SecurityEvent.SecurityEventType.LOGIN_FAILURE, startDate, endDate);
    long successfulLogins = securityEventRepository.countEventsByTypeAndTimeRange(
        SecurityEvent.SecurityEventType.LOGIN_SUCCESS, startDate, endDate);
    long accountLockouts = securityEventRepository.countEventsByTypeAndTimeRange(
        SecurityEvent.SecurityEventType.ACCOUNT_LOCKED, startDate, endDate);

    return SecurityStatistics.builder()
        .totalEvents(totalEvents)
        .failedLogins(failedLogins)
        .successfulLogins(successfulLogins)
        .accountLockouts(accountLockouts)
        .periodDays(days)
        .build();
  }

  /**
   * Security statistics data class.
   */
  public static class SecurityStatistics {
    private final long totalEvents;
    private final long failedLogins;
    private final long successfulLogins;
    private final long accountLockouts;
    private final int periodDays;

    public SecurityStatistics(long totalEvents, long failedLogins, long successfulLogins,
        long accountLockouts, int periodDays) {
      this.totalEvents = totalEvents;
      this.failedLogins = failedLogins;
      this.successfulLogins = successfulLogins;
      this.accountLockouts = accountLockouts;
      this.periodDays = periodDays;
    }

    // Getters
    public long getTotalEvents() {
      return totalEvents;
    }

    public long getFailedLogins() {
      return failedLogins;
    }

    public long getSuccessfulLogins() {
      return successfulLogins;
    }

    public long getAccountLockouts() {
      return accountLockouts;
    }

    public int getPeriodDays() {
      return periodDays;
    }

    // Builder
    public static SecurityStatisticsBuilder builder() {
      return new SecurityStatisticsBuilder();
    }

    public static class SecurityStatisticsBuilder {
      private long totalEvents;
      private long failedLogins;
      private long successfulLogins;
      private long accountLockouts;
      private int periodDays;

      public SecurityStatisticsBuilder totalEvents(long totalEvents) {
        this.totalEvents = totalEvents;
        return this;
      }

      public SecurityStatisticsBuilder failedLogins(long failedLogins) {
        this.failedLogins = failedLogins;
        return this;
      }

      public SecurityStatisticsBuilder successfulLogins(long successfulLogins) {
        this.successfulLogins = successfulLogins;
        return this;
      }

      public SecurityStatisticsBuilder accountLockouts(long accountLockouts) {
        this.accountLockouts = accountLockouts;
        return this;
      }

      public SecurityStatisticsBuilder periodDays(int periodDays) {
        this.periodDays = periodDays;
        return this;
      }

      public SecurityStatistics build() {
        return new SecurityStatistics(totalEvents, failedLogins, successfulLogins, accountLockouts, periodDays);
      }
    }
  }
}