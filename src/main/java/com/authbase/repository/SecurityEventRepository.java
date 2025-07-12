package com.authbase.repository;

import com.authbase.entity.SecurityEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for SecurityEvent entity.
 * Provides methods for querying and analyzing security events.
 */
@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {

  /**
   * Find security events by user ID.
   * 
   * @param userId   user ID
   * @param pageable pagination
   * @return page of security events
   */
  Page<SecurityEvent> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

  /**
   * Find security events by event type.
   * 
   * @param eventType security event type
   * @param pageable  pagination
   * @return page of security events
   */
  Page<SecurityEvent> findByEventTypeOrderByCreatedAtDesc(SecurityEvent.SecurityEventType eventType, Pageable pageable);

  /**
   * Find security events by success status.
   * 
   * @param success  success status
   * @param pageable pagination
   * @return page of security events
   */
  Page<SecurityEvent> findBySuccessOrderByCreatedAtDesc(Boolean success, Pageable pageable);

  /**
   * Find security events by IP address.
   * 
   * @param ipAddress IP address
   * @param pageable  pagination
   * @return page of security events
   */
  Page<SecurityEvent> findByIpAddressOrderByCreatedAtDesc(String ipAddress, Pageable pageable);

  /**
   * Find security events within a time range.
   * 
   * @param startDate start date
   * @param endDate   end date
   * @param pageable  pagination
   * @return page of security events
   */
  Page<SecurityEvent> findByCreatedAtBetweenOrderByCreatedAtDesc(
      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

  /**
   * Find failed login attempts for a user within a time range.
   * 
   * @param userId    user ID
   * @param startDate start date
   * @param endDate   end date
   * @return list of failed login events
   */
  @Query("SELECT se FROM SecurityEvent se WHERE se.user.id = :userId " +
      "AND se.eventType = 'LOGIN_FAILURE' " +
      "AND se.createdAt BETWEEN :startDate AND :endDate " +
      "ORDER BY se.createdAt DESC")
  List<SecurityEvent> findFailedLoginAttemptsByUserAndTimeRange(
      @Param("userId") Long userId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  /**
   * Count failed login attempts for a user within a time range.
   * 
   * @param userId    user ID
   * @param startDate start date
   * @param endDate   end date
   * @return count of failed login attempts
   */
  @Query("SELECT COUNT(se) FROM SecurityEvent se WHERE se.user.id = :userId " +
      "AND se.eventType = 'LOGIN_FAILURE' " +
      "AND se.createdAt BETWEEN :startDate AND :endDate")
  long countFailedLoginAttemptsByUserAndTimeRange(
      @Param("userId") Long userId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  /**
   * Find suspicious activity events (multiple failed attempts from same IP).
   * 
   * @param ipAddress IP address
   * @param startDate start date
   * @param endDate   end date
   * @return list of suspicious events
   */
  @Query("SELECT se FROM SecurityEvent se WHERE se.ipAddress = :ipAddress " +
      "AND se.eventType = 'LOGIN_FAILURE' " +
      "AND se.createdAt BETWEEN :startDate AND :endDate " +
      "ORDER BY se.createdAt DESC")
  List<SecurityEvent> findSuspiciousActivityByIpAndTimeRange(
      @Param("ipAddress") String ipAddress,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  /**
   * Count events by type within a time range.
   * 
   * @param eventType event type
   * @param startDate start date
   * @param endDate   end date
   * @return count of events
   */
  @Query("SELECT COUNT(se) FROM SecurityEvent se WHERE se.eventType = :eventType " +
      "AND se.createdAt BETWEEN :startDate AND :endDate")
  long countEventsByTypeAndTimeRange(
      @Param("eventType") SecurityEvent.SecurityEventType eventType,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  /**
   * Find recent security events for dashboard.
   * 
   * @param limit maximum number of events
   * @return list of recent security events
   */
  @Query("SELECT se FROM SecurityEvent se ORDER BY se.createdAt DESC")
  List<SecurityEvent> findRecentSecurityEvents(Pageable pageable);

  /**
   * Delete old security events (cleanup).
   * 
   * @param cutoffDate cutoff date for deletion
   * @return number of deleted events
   */
  @Query("DELETE FROM SecurityEvent se WHERE se.createdAt < :cutoffDate")
  int deleteOldSecurityEvents(@Param("cutoffDate") LocalDateTime cutoffDate);
}