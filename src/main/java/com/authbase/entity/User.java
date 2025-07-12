package com.authbase.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be a valid email address")
  @Column(name = "email", nullable = false, unique = true, length = 255)
  private String email;

  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
  @Column(name = "username", nullable = false, unique = true, length = 100)
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
  private Boolean isEnabled = true;

  @Column(name = "is_email_verified", nullable = false)
  private Boolean isEmailVerified = false;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

  // Many-to-Many relationship with Role
  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>();

  // One-to-Many relationship with RefreshToken
  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<RefreshToken> refreshTokens = new HashSet<>();

  // Default constructor for JPA
  protected User() {
  }

  // Constructor for creating users
  public User(String email, String username, String password) {
    this.email = email;
    this.username = username;
    this.password = password;
  }

  // Constructor with all fields
  public User(String email, String username, String password, String firstName, String lastName) {
    this.email = email;
    this.username = username;
    this.password = password;
    this.firstName = firstName;
    this.lastName = lastName;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public Boolean getIsEnabled() {
    return isEnabled;
  }

  public void setIsEnabled(Boolean isEnabled) {
    this.isEnabled = isEnabled;
  }

  public Boolean getIsEmailVerified() {
    return isEmailVerified;
  }

  public void setIsEmailVerified(Boolean isEmailVerified) {
    this.isEmailVerified = isEmailVerified;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public LocalDateTime getLastLoginAt() {
    return lastLoginAt;
  }

  public void setLastLoginAt(LocalDateTime lastLoginAt) {
    this.lastLoginAt = lastLoginAt;
  }

  public Set<Role> getRoles() {
    return roles;
  }

  public void setRoles(Set<Role> roles) {
    this.roles = roles;
  }

  public Set<RefreshToken> getRefreshTokens() {
    return refreshTokens;
  }

  public void setRefreshTokens(Set<RefreshToken> refreshTokens) {
    this.refreshTokens = refreshTokens;
  }

  // Helper methods for managing relationships
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

  // Business logic methods
  public boolean isAccountNonExpired() {
    return true; // Can be extended for account expiration logic
  }

  public boolean isAccountNonLocked() {
    return isEnabled;
  }

  public boolean isCredentialsNonExpired() {
    return true; // Can be extended for password expiration logic
  }

  public boolean isEnabled() {
    return isEnabled;
  }

  public String getFullName() {
    if (firstName != null && lastName != null) {
      return firstName + " " + lastName;
    } else if (firstName != null) {
      return firstName;
    } else if (lastName != null) {
      return lastName;
    } else {
      return username;
    }
  }

  // equals and hashCode
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    User user = (User) o;
    return email != null ? email.equals(user.email) : user.email == null;
  }

  @Override
  public int hashCode() {
    return email != null ? email.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "User{" +
        "id=" + id +
        ", email='" + email + '\'' +
        ", username='" + username + '\'' +
        ", firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", isEnabled=" + isEnabled +
        ", isEmailVerified=" + isEmailVerified +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        ", lastLoginAt=" + lastLoginAt +
        '}';
  }
}