package com.authbase.service;

import com.authbase.entity.SecurityEvent;
import com.authbase.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for managing security events.
 * Provides methods for logging security events and analyzing security data.
 */
public interface SecurityEventService {

  /**
   * Log a security event.
   * 
   * @param eventType   type of security event
   * @param description event description
   * @param ipAddress   IP address of the request
   * @param userAgent   user agent string
   * @param success     whether the event was successful
   * @param details     additional details
   * @param user        associated user (can be null for system events)
   * @return the created security event
   */
  SecurityEvent logSecurityEvent(SecurityEvent.SecurityEventType eventType, String description,
      String ipAddress, String userAgent, Boolean success, String details, User user);

  /**
   * Log a security event without user association.
   * 
   * @param eventType   type of security event
   * @param description event description
   * @param ipAddress   IP address of the request
   * @param userAgent   user agent string
   * @param success     whether the event was successful
   * @param details     additional details
   * @return the created security event
   */
  SecurityEvent logSecurityEvent(SecurityEvent.SecurityEventType eventType, String description,
      String ipAddress, String userAgent, Boolean success, String details);

  /**
   * Log a login attempt.
   * 
   * @param user      user attempting to login
   * @param ipAddress IP address
   * @param userAgent user agent
   * @param success   whether login was successful
   * @param details   additional details
   * @return the created security event
   */
  SecurityEvent logLoginAttempt(User user, String ipAddress, String userAgent, Boolean success, String details);

  /**
   * Log a logout event.
   * 
   * @param user      user logging out
   * @param ipAddress IP address
   * @param userAgent user agent
   * @return the created security event
   */
  SecurityEvent logLogout(User user, String ipAddress, String userAgent);

  /**
   * Log an account lockout.
   * 
   * @param user      user whose account was locked
   * @param ipAddress IP address
   * @param userAgent user agent
   * @param reason    reason for lockout
   * @return the created security event
   */
  SecurityEvent logAccountLockout(User user, String ipAddress, String userAgent, String reason);

  /**
   * Log an account unlock.
   * 
   * @param user      user whose account was unlocked
   * @param ipAddress IP address
   * @param userAgent user agent
   * @param reason    reason for unlock
   * @return the created security event
   */
  SecurityEvent logAccountUnlock(User user, String ipAddress, String userAgent, String reason);

  /**
   * Log a password change.
   * 
   * @param user      user changing password
   * @param ipAddress IP address
   * @param userAgent user agent
   * @param success   whether password change was successful
   * @return the created security event
   */
  SecurityEvent logPasswordChange(User user, String ipAddress, String userAgent, Boolean success);

  /**
   * Log a password reset request.
   * 
   * @param user      user requesting password reset
   * @param ipAddress IP address
   * @param userAgent user agent
   * @return the created security event
   */
  SecurityEvent logPasswordResetRequest(User user, String ipAddress, String userAgent);

  /**
   * Log a password reset success.
   * 
   * @param user      user who reset password
   * @param ipAddress IP address
   * @param userAgent user agent
   * @return the created security event
   */
  SecurityEvent logPasswordResetSuccess(User user, String ipAddress, String userAgent);

  /**
   * Log suspicious activity.
   * 
   * @param ipAddress IP address
   * @param userAgent user agent
   * @param details   details of suspicious activity
   * @return the created security event
   */
  SecurityEvent logSuspiciousActivity(String ipAddress, String userAgent, String details);

  /**
   * Get security events for a user.
   * 
   * @param userId   user ID
   * @param pageable pagination
   * @return page of security events
   */
  Page<SecurityEvent> getSecurityEventsByUser(Long userId, Pageable pageable);

  /**
   * Get security events by type.
   * 
   * @param eventType event type
   * @param pageable  pagination
   * @return page of security events
   */
  Page<SecurityEvent> getSecurityEventsByType(SecurityEvent.SecurityEventType eventType, Pageable pageable);

  /**
   * Get security events within a time range.
   * 
   * @param startDate start date
   * @param endDate   end date
   * @param pageable  pagination
   * @return page of security events
   */
  Page<SecurityEvent> getSecurityEventsByTimeRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

  /**
   * Get recent security events for dashboard.
   * 
   * @param pageable pagination
   * @return list of recent security events
   */
  List<SecurityEvent> getRecentSecurityEvents(Pageable pageable);

  /**
   * Count failed login attempts for a user within a time range.
   * 
   * @param userId    user ID
   * @param startDate start date
   * @param endDate   end date
   * @return count of failed login attempts
   */
  long countFailedLoginAttempts(Long userId, LocalDateTime startDate, LocalDateTime endDate);

  /**
   * Check for suspicious activity from an IP address.
   * 
   * @param ipAddress  IP address
   * @param timeWindow time window to check
   * @return list of suspicious events
   */
  List<SecurityEvent> checkSuspiciousActivity(String ipAddress, LocalDateTime timeWindow);

  /**
   * Clean up old security events.
   * 
   * @param cutoffDate cutoff date for deletion
   * @return number of deleted events
   */
  int cleanupOldSecurityEvents(LocalDateTime cutoffDate);
}