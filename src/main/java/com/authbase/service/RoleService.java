package com.authbase.service;

import com.authbase.entity.Permission;
import com.authbase.entity.Role;
import com.authbase.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service interface for role and permission management operations.
 * Handles role creation, permission assignment, and user-role management.
 */
public interface RoleService {

  /**
   * Create a new role.
   * 
   * @param name        role name
   * @param description role description
   * @return created role
   * @throws IllegalArgumentException if role already exists
   */
  Role createRole(String name, String description);

  /**
   * Find role by name.
   * 
   * @param name role name
   * @return Optional containing role if found
   */
  Optional<Role> findByName(String name);

  /**
   * Find role by ID.
   * 
   * @param id role ID
   * @return Optional containing role if found
   */
  Optional<Role> findById(Long id);

  /**
   * Get all roles.
   * 
   * @return list of all roles
   */
  List<Role> findAllRoles();

  /**
   * Update role description.
   * 
   * @param roleId      role ID
   * @param description new description
   * @return updated role
   * @throws IllegalArgumentException if role not found
   */
  Role updateRoleDescription(Long roleId, String description);

  /**
   * Delete role.
   * 
   * @param roleId role ID
   * @return true if deleted successfully
   * @throws IllegalArgumentException if role not found or has users
   */
  boolean deleteRole(Long roleId);

  /**
   * Add permission to role.
   * 
   * @param roleId       role ID
   * @param permissionId permission ID
   * @return updated role
   * @throws IllegalArgumentException if role or permission not found
   */
  Role addPermissionToRole(Long roleId, Long permissionId);

  /**
   * Remove permission from role.
   * 
   * @param roleId       role ID
   * @param permissionId permission ID
   * @return updated role
   * @throws IllegalArgumentException if role or permission not found
   */
  Role removePermissionFromRole(Long roleId, Long permissionId);

  /**
   * Assign role to user.
   * 
   * @param userId user ID
   * @param roleId role ID
   * @return updated user
   * @throws IllegalArgumentException if user or role not found
   */
  User assignRoleToUser(Long userId, Long roleId);

  /**
   * Remove role from user.
   * 
   * @param userId user ID
   * @param roleId role ID
   * @return updated user
   * @throws IllegalArgumentException if user or role not found
   */
  User removeRoleFromUser(Long userId, Long roleId);

  /**
   * Get all permissions for a role.
   * 
   * @param roleId role ID
   * @return set of permissions
   * @throws IllegalArgumentException if role not found
   */
  Set<Permission> getRolePermissions(Long roleId);

  /**
   * Get all roles for a user.
   * 
   * @param userId user ID
   * @return set of roles
   * @throws IllegalArgumentException if user not found
   */
  Set<Role> getUserRoles(Long userId);

  /**
   * Check if user has specific role.
   * 
   * @param userId   user ID
   * @param roleName role name
   * @return true if user has the role
   */
  boolean userHasRole(Long userId, String roleName);

  /**
   * Check if role has specific permission.
   * 
   * @param roleId         role ID
   * @param permissionName permission name
   * @return true if role has the permission
   */
  boolean roleHasPermission(Long roleId, String permissionName);

  /**
   * Get role count.
   * 
   * @return total number of roles
   */
  long getRoleCount();

  /**
   * Check if role exists by name.
   * 
   * @param name role name
   * @return true if role exists
   */
  boolean existsByName(String name);
}