package com.authbase.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * UserSession entity for tracking user sessions.
 * This is optional and can be used for session management and security
 * auditing.
 */
@Entity
@Table(name = "user_sessions")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString(exclude = { "user" })
@EqualsAndHashCode(exclude = { "createdAt", "lastActivityAt", "expiresAt" })
public class UserSession {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull(message = "User is required")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @NotBlank(message = "Session ID is required")
  @Column(name = "session_id", nullable = false, unique = true, length = 255)
  private String sessionId;

  @Column(name = "ip_address", length = 45) // IPv6 can be up to 45 characters
  private String ipAddress;

  @Column(name = "user_agent", length = 500)
  private String userAgent;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = true;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "last_activity_at")
  @Builder.Default
  private LocalDateTime lastActivityAt = LocalDateTime.now();

  @Column(name = "expires_at")
  private LocalDateTime expiresAt;

  // Constructor for creating sessions
  public UserSession(User user, String sessionId, String ipAddress, String userAgent) {
    this.user = user;
    this.sessionId = sessionId;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
    this.lastActivityAt = LocalDateTime.now();
  }

  // Constructor with expiry
  public UserSession(User user, String sessionId, String ipAddress, String userAgent, LocalDateTime expiresAt) {
    this.user = user;
    this.sessionId = sessionId;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
    this.expiresAt = expiresAt;
    this.lastActivityAt = LocalDateTime.now();
  }

  // Business Logic Methods
  public boolean isExpired() {
    return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
  }

  public boolean isValid() {
    return isActive && !isExpired();
  }

  public void updateLastActivity() {
    this.lastActivityAt = LocalDateTime.now();
  }

  public void deactivate() {
    this.isActive = false;
  }
}