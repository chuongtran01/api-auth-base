package com.authbase.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
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
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString(exclude = { "token" })
@EqualsAndHashCode(exclude = { "createdAt" })
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

  // Constructor for creating refresh tokens
  public RefreshToken(String token, User user, LocalDateTime expiryDate) {
    this.token = token;
    this.user = user;
    this.expiryDate = expiryDate;
  }

  // Business Logic Methods
  public boolean isExpired() {
    return LocalDateTime.now().isAfter(expiryDate);
  }

  public boolean isValid() {
    return !isExpired();
  }
}