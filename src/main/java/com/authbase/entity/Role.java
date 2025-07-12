package com.authbase.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
  private Set<Permission> permissions = new HashSet<>();

  // Many-to-Many relationship with User
  @ManyToMany(mappedBy = "roles")
  private Set<User> users = new HashSet<>();

  // Default constructor for JPA
  protected Role() {
  }

  // Constructor for creating roles
  public Role(String name, String description) {
    this.name = name;
    this.description = description;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
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

  public Set<Permission> getPermissions() {
    return permissions;
  }

  public void setPermissions(Set<Permission> permissions) {
    this.permissions = permissions;
  }

  public Set<User> getUsers() {
    return users;
  }

  public void setUsers(Set<User> users) {
    this.users = users;
  }

  // Helper methods for managing relationships
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

  // equals and hashCode
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Role role = (Role) o;
    return name != null ? name.equals(role.name) : role.name == null;
  }

  @Override
  public int hashCode() {
    return name != null ? name.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "Role{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        '}';
  }
}