package com.authbase.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * RefreshToken entity representing JWT refresh tokens.
 * Refresh tokens are used to obtain new access tokens without
 * re-authentication.
 */
@Entity
@Table(name = "refresh_tokens")
@EntityListeners(AuditingEntityListener.class)
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Token is required")
  @Column(name = "token", nullable = false, unique = true, length = 500)
  private String token;

  @NotNull(message = "User is required")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @NotNull(message = "Expiry date is required")
  @Column(name = "expiry_date", nullable = false)
  private LocalDateTime expiryDate;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  // Default constructor for JPA
  protected RefreshToken() {
  }

  // Constructor for creating refresh tokens
  public RefreshToken(String token, User user, LocalDateTime expiryDate) {
    this.token = token;
    this.user = user;
    this.expiryDate = expiryDate;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public LocalDateTime getExpiryDate() {
    return expiryDate;
  }

  public void setExpiryDate(LocalDateTime expiryDate) {
    this.expiryDate = expiryDate;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  // Business logic methods
  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiryDate);
  }

  public boolean isValid() {
    return !isExpired();
  }

  // equals and hashCode
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    RefreshToken that = (RefreshToken) o;
    return token != null ? token.equals(that.token) : that.token == null;
  }

  @Override
  public int hashCode() {
    return token != null ? token.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "RefreshToken{" +
        "id=" + id +
        ", token='" + token + '\'' +
        ", userId=" + (user != null ? user.getId() : null) +
        ", expiryDate=" + expiryDate +
        ", createdAt=" + createdAt +
        '}';
  }
}