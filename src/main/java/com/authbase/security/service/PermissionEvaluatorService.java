package com.authbase.security.service;

import com.authbase.entity.User;
import com.authbase.util.PermissionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service for evaluating user permissions and roles for authorization.
 * Provides methods to check if the current user can perform specific actions.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PermissionEvaluatorService {

  /**
   * Check if the current user has a specific permission.
   * 
   * @param permissionName the permission to check
   * @return true if user has the permission, false otherwise
   */
  public boolean hasPermission(String permissionName) {
    User user = getCurrentUser();
    if (user == null) {
      log.warn("No authenticated user found for permission check: {}", permissionName);
      return false;
    }

    boolean hasPermission = PermissionUtil.hasPermission(user, permissionName);
    log.debug("Permission check for user {}: {} -> {}", user.getEmail(), permissionName, hasPermission);
    return hasPermission;
  }

  /**
   * Check if the current user has any of the specified permissions.
   * 
   * @param permissionNames the permissions to check
   * @return true if user has any of the permissions, false otherwise
   */
  public boolean hasAnyPermission(String... permissionNames) {
    User user = getCurrentUser();
    if (user == null) {
      log.warn("No authenticated user found for permission check");
      return false;
    }

    boolean hasPermission = PermissionUtil.hasAnyPermission(user, permissionNames);
    log.debug("Permission check for user {}: any of {} -> {}", user.getEmail(), permissionNames, hasPermission);
    return hasPermission;
  }

  /**
   * Check if the current user has all of the specified permissions.
   * 
   * @param permissionNames the permissions to check
   * @return true if user has all permissions, false otherwise
   */
  public boolean hasAllPermissions(String... permissionNames) {
    User user = getCurrentUser();
    if (user == null) {
      log.warn("No authenticated user found for permission check");
      return false;
    }

    boolean hasPermission = PermissionUtil.hasAllPermissions(user, permissionNames);
    log.debug("Permission check for user {}: all of {} -> {}", user.getEmail(), permissionNames, hasPermission);
    return hasPermission;
  }

  /**
   * Check if the current user has a specific role.
   * 
   * @param roleName the role to check
   * @return true if user has the role, false otherwise
   */
  public boolean hasRole(String roleName) {
    User user = getCurrentUser();
    if (user == null) {
      log.warn("No authenticated user found for role check: {}", roleName);
      return false;
    }

    boolean hasRole = PermissionUtil.hasRole(user, roleName);
    log.debug("Role check for user {}: {} -> {}", user.getEmail(), roleName, hasRole);
    return hasRole;
  }

  /**
   * Check if the current user has any of the specified roles.
   * 
   * @param roleNames the roles to check
   * @return true if user has any of the roles, false otherwise
   */
  public boolean hasAnyRole(String... roleNames) {
    User user = getCurrentUser();
    if (user == null) {
      log.warn("No authenticated user found for role check");
      return false;
    }

    boolean hasRole = PermissionUtil.hasAnyRole(user, roleNames);
    log.debug("Role check for user {}: any of {} -> {}", user.getEmail(), roleNames, hasRole);
    return hasRole;
  }

  /**
   * Check if the current user can perform a specific action based on permissions.
   * This is a convenience method that combines common permission checks.
   * 
   * @param action   the action to check (e.g., "READ", "WRITE", "DELETE")
   * @param resource the resource to check (e.g., "USER", "ROLE")
   * @return true if user can perform the action, false otherwise
   */
  public boolean canPerformAction(String action, String resource) {
    String permissionName = resource.toUpperCase() + "_" + action.toUpperCase();
    return hasPermission(permissionName);
  }

  /**
   * Check if the current user can read a specific resource.
   * 
   * @param resource the resource to check
   * @return true if user can read the resource, false otherwise
   */
  public boolean canRead(String resource) {
    return canPerformAction("READ", resource);
  }

  /**
   * Check if the current user can write (create/update) a specific resource.
   * 
   * @param resource the resource to check
   * @return true if user can write the resource, false otherwise
   */
  public boolean canWrite(String resource) {
    return canPerformAction("WRITE", resource);
  }

  /**
   * Check if the current user can delete a specific resource.
   * 
   * @param resource the resource to check
   * @return true if user can delete the resource, false otherwise
   */
  public boolean canDelete(String resource) {
    return canPerformAction("DELETE", resource);
  }

  /**
   * Check if the current user has admin access.
   * 
   * @return true if user has admin access, false otherwise
   */
  public boolean hasAdminAccess() {
    return hasPermission("ADMIN_ACCESS") || hasRole("ADMIN");
  }

  /**
   * Get the current authenticated user.
   * 
   * @return the current user or null if not authenticated
   */
  private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof User) {
      return (User) principal;
    }

    log.warn("Principal is not a User instance: {}", principal.getClass().getSimpleName());
    return null;
  }

  /**
   * Log the current user's permissions for debugging purposes.
   * 
   * @param user the user to log permissions for
   */
  public void logUserPermissions(User user) {
    if (user != null) {
      PermissionUtil.printUserPermissions(user);
    }
  }
}