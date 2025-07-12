package com.authbase.entity;

import jakarta.persistence.*;
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
 * Role entity representing user roles in the system.
 * Roles are collections of permissions that define what users can do.
 */
@Entity
@Table(name = "roles")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString(exclude = { "users" })
@EqualsAndHashCode(exclude = { "users", "createdAt", "updatedAt" })
public class Role {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Role name is required")
  @Size(max = 50, message = "Role name must not exceed 50 characters")
  @Column(name = "name", nullable = false, unique = true, length = 50)
  private String name;

  @Size(max = 255, message = "Role description must not exceed 255 characters")
  @Column(name = "description", length = 255)
  private String description;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  // Many-to-Many relationship with Permission
  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
  @Builder.Default
  private Set<Permission> permissions = new HashSet<>();

  // Many-to-Many relationship with User
  @ManyToMany(mappedBy = "roles")
  @Builder.Default
  private Set<User> users = new HashSet<>();

  // Constructor for creating roles
  public Role(String name, String description) {
    this.name = name;
    this.description = description;
    this.permissions = new HashSet<>();
    this.users = new HashSet<>();
  }

  // Business Logic Methods
  public void addPermission(Permission permission) {
    this.permissions.add(permission);
    permission.getRoles().add(this);
  }

  public void removePermission(Permission permission) {
    this.permissions.remove(permission);
    permission.getRoles().remove(this);
  }

  public void addUser(User user) {
    this.users.add(user);
    user.getRoles().add(this);
  }

  public void removeUser(User user) {
    this.users.remove(user);
    user.getRoles().remove(this);
  }
}