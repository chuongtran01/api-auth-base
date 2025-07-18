package com.authbase.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity representing user accounts in the system.
 * Users can have multiple roles and are the primary authentication entities.
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString(exclude = { "password", "refreshTokens" })
@EqualsAndHashCode(exclude = { "password", "refreshTokens", "createdAt", "updatedAt", "lastLoginAt" })
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Email(message = "Email must be a valid email address")
  @NotBlank(message = "Email is required")
  @Column(name = "email", nullable = false, unique = true, length = 255)
  private String email;

  @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
  @Column(name = "username", nullable = true, unique = true, length = 100)
  private String username;

  @NotBlank(message = "Password is required")
  @Column(name = "password", nullable = false, length = 255)
  private String password;

  @Size(max = 100, message = "First name must not exceed 100 characters")
  @Column(name = "first_name", length = 100)
  private String firstName;

  @Size(max = 100, message = "Last name must not exceed 100 characters")
  @Column(name = "last_name", length = 100)
  private String lastName;

  @Column(name = "is_enabled", nullable = false)
  @Builder.Default
  private Boolean isEnabled = true;

  @Column(name = "is_email_verified", nullable = false)
  @Builder.Default
  private Boolean isEmailVerified = false;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

  // Account lockout fields
  @Column(name = "failed_login_attempts", nullable = false)
  @Builder.Default
  private Integer failedLoginAttempts = 0;

  @Column(name = "account_locked_until")
  private LocalDateTime accountLockedUntil;

  @Column(name = "last_failed_login_at")
  private LocalDateTime lastFailedLoginAt;

  // Many-to-Many relationship with Role
  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
  @Builder.Default
  private Set<Role> roles = new HashSet<>();

  // One-to-Many relationship with RefreshToken
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private Set<RefreshToken> refreshTokens = new HashSet<>();

  // Constructor for creating users
  public User(String email, String password) {
    this.email = email;
    this.password = password;
    this.roles = new HashSet<>();
    this.refreshTokens = new HashSet<>();
  }

  // Constructor with all fields
  public User(String email, String password, String username, String firstName, String lastName) {
    this.email = email;
    this.password = password;
    this.username = username;
    this.firstName = firstName;
    this.lastName = lastName;
    this.roles = new HashSet<>();
    this.refreshTokens = new HashSet<>();
  }

  // Business Logic Methods
  public void addRole(Role role) {
    this.roles.add(role);
    role.getUsers().add(this);
  }

  public void removeRole(Role role) {
    this.roles.remove(role);
    role.getUsers().remove(this);
  }

  public void addRefreshToken(RefreshToken refreshToken) {
    this.refreshTokens.add(refreshToken);
    refreshToken.setUser(this);
  }

  public void removeRefreshToken(RefreshToken refreshToken) {
    this.refreshTokens.remove(refreshToken);
    refreshToken.setUser(null);
  }

  // Account lockout methods
  public void incrementFailedLoginAttempts() {
    this.failedLoginAttempts++;
    this.lastFailedLoginAt = LocalDateTime.now();
  }

  public void resetFailedLoginAttempts() {
    this.failedLoginAttempts = 0;
    this.accountLockedUntil = null;
  }

  public void lockAccount(LocalDateTime lockUntil) {
    this.accountLockedUntil = lockUntil;
  }

  public boolean isAccountLocked() {
    if (accountLockedUntil == null) {
      return false;
    }
    return LocalDateTime.now().isBefore(accountLockedUntil);
  }

  public boolean isAccountLockedExpired() {
    if (accountLockedUntil == null) {
      return true;
    }
    return LocalDateTime.now().isAfter(accountLockedUntil);
  }

  // Spring Security UserDetails methods
  public boolean isAccountNonExpired() {
    return true;
  }

  public boolean isAccountNonLocked() {
    return !isAccountLocked();
  }

  public boolean isCredentialsNonExpired() {
    return true;
  }

  public boolean isEnabled() {
    return this.isEnabled;
  }

  public String getFullName() {
    if (firstName != null && lastName != null) {
      return firstName + " " + lastName;
    } else if (firstName != null) {
      return firstName;
    } else if (lastName != null) {
      return lastName;
    } else if (username != null) {
      return username;
    } else {
      return email;
    }
  }
}