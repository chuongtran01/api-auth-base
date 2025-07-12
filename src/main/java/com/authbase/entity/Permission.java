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
 * Permission entity representing system permissions.
 * Permissions define what actions users can perform in the system.
 */
@Entity
@Table(name = "permissions")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString(exclude = { "roles" })
@EqualsAndHashCode(exclude = { "roles", "createdAt", "updatedAt" })
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
  @Builder.Default
  private Set<Role> roles = new HashSet<>();

  // Constructor for creating permissions
  public Permission(String name, String description) {
    this.name = name;
    this.description = description;
    this.roles = new HashSet<>();
  }

  // Business Logic Methods
  public void addRole(Role role) {
    this.roles.add(role);
    role.getPermissions().add(this);
  }

  public void removeRole(Role role) {
    this.roles.remove(role);
    role.getPermissions().remove(this);
  }
}
