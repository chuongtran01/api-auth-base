package com.authbase.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * SecurityEvent entity for logging security-related events.
 * Tracks login attempts, account lockouts, password changes, and other security
 * events.
 */
@Entity
@Table(name = "security_events")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
@EqualsAndHashCode(exclude = { "createdAt" })
public class SecurityEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false, length = 50)
  private SecurityEventType eventType;

  @Column(name = "description", nullable = false, length = 500)
  private String description;

  @Column(name = "ip_address", length = 45) // IPv6 compatible
  private String ipAddress;

  @Column(name = "user_agent", length = 500)
  private String userAgent;

  @Column(name = "success", nullable = false)
  private Boolean success;

  @Column(name = "details", columnDefinition = "TEXT")
  private String details;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  // Many-to-One relationship with User (optional - some events may not be
  // user-specific)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  // Constructor for user-specific events
  public SecurityEvent(SecurityEventType eventType, String description, String ipAddress,
      String userAgent, Boolean success, String details, User user) {
    this.eventType = eventType;
    this.description = description;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
    this.success = success;
    this.details = details;
    this.user = user;
  }

  // Constructor for system events (no user)
  public SecurityEvent(SecurityEventType eventType, String description, String ipAddress,
      String userAgent, Boolean success, String details) {
    this.eventType = eventType;
    this.description = description;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
    this.success = success;
    this.details = details;
  }

  /**
   * Security event types enumeration.
   */
  public enum SecurityEventType {
    LOGIN_ATTEMPT("Login attempt"),
    LOGIN_SUCCESS("Successful login"),
    LOGIN_FAILURE("Failed login"),
    LOGOUT("User logout"),
    PASSWORD_CHANGE("Password change"),
    PASSWORD_RESET_REQUEST("Password reset request"),
    PASSWORD_RESET_SUCCESS("Password reset successful"),
    ACCOUNT_LOCKED("Account locked"),
    ACCOUNT_UNLOCKED("Account unlocked"),
    EMAIL_VERIFICATION("Email verification"),
    REGISTRATION("User registration"),
    ACCOUNT_DISABLED("Account disabled"),
    ACCOUNT_ENABLED("Account enabled"),
    ROLE_ASSIGNED("Role assigned"),
    ROLE_REMOVED("Role removed"),
    SUSPICIOUS_ACTIVITY("Suspicious activity detected"),
    TOKEN_REFRESH("Token refresh"),
    TOKEN_INVALID("Invalid token"),
    SESSION_EXPIRED("Session expired");

    private final String displayName;

    SecurityEventType(String displayName) {
      this.displayName = displayName;
    }

    public String getDisplayName() {
      return displayName;
    }
  }
}