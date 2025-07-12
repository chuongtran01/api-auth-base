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
 * Permission entity representing system permissions.
 * Permissions define what actions users can perform in the system.
 */
@Entity
@Table(name = "permissions")
@EntityListeners(AuditingEntityListener.class)
public class Permission {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Permission name is required")
  @Size(max = 100, message = "Permission name must not exceed 100 characters")
  @Column(name = "name", nullable = false, unique = true, length = 100)
  private String name;

  @Size(max = 255, message = "Permission description must not exceed 255 characters")
  @Column(name = "description", length = 255)
  private String description;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  // Many-to-Many relationship with Role
  @ManyToMany(mappedBy = "permissions")
  private Set<Role> roles = new HashSet<>();

  // Default constructor for JPA
  protected Permission() {
  }

  // Constructor for creating permissions
  public Permission(String name, String description) {
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

  public Set<Role> getRoles() {
    return roles;
  }

  public void setRoles(Set<Role> roles) {
    this.roles = roles;
  }

  // Helper methods for managing relationships
  public void addRole(Role role) {
    this.roles.add(role);
    role.getPermissions().add(this);
  }

  public void removeRole(Role role) {
    this.roles.remove(role);
    role.getPermissions().remove(this);
  }

  // equals and hashCode
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Permission that = (Permission) o;
    return name != null ? name.equals(that.name) : that.name == null;
  }

  @Override
  public int hashCode() {
    return name != null ? name.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "Permission{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", description='" + description + '\'' +
        ", createdAt=" + createdAt +
        ", updatedAt=" + updatedAt +
        '}';
  }
}
