package com.authbase.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
  private Boolean isActive = true;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "last_activity_at")
  private LocalDateTime lastActivityAt;

  @Column(name = "expires_at")
  private LocalDateTime expiresAt;

  // Default constructor for JPA
  protected UserSession() {
  }

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

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public Boolean getIsActive() {
    return isActive;
  }

  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getLastActivityAt() {
    return lastActivityAt;
  }

  public void setLastActivityAt(LocalDateTime lastActivityAt) {
    this.lastActivityAt = lastActivityAt;
  }

  public LocalDateTime getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(LocalDateTime expiresAt) {
    this.expiresAt = expiresAt;
  }

  // Business logic methods
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

  // equals and hashCode
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    UserSession that = (UserSession) o;
    return sessionId != null ? sessionId.equals(that.sessionId) : that.sessionId == null;
  }

  @Override
  public int hashCode() {
    return sessionId != null ? sessionId.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "UserSession{" +
        "id=" + id +
        ", userId=" + (user != null ? user.getId() : null) +
        ", sessionId='" + sessionId + '\'' +
        ", ipAddress='" + ipAddress + '\'' +
        ", isActive=" + isActive +
        ", createdAt=" + createdAt +
        ", lastActivityAt=" + lastActivityAt +
        ", expiresAt=" + expiresAt +
        '}';
  }
}