package com.authbase.util;

import com.authbase.entity.Permission;
import com.authbase.entity.Role;
import com.authbase.entity.User;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for working with user permissions and roles.
 * Demonstrates how multiple roles per user work and how permissions are
 * calculated.
 */
public class PermissionUtil {

  /**
   * Get all permissions for a user from all their roles.
   * This demonstrates the power of multiple roles - permissions are combined from
   * all roles.
   * 
   * @param user the user to get permissions for
   * @return set of all permissions the user has
   */
  public static Set<Permission> getAllUserPermissions(User user) {
    return user.getRoles().stream()
        .flatMap(role -> role.getPermissions().stream())
        .collect(Collectors.toSet());
  }

  /**
   * Check if a user has a specific permission.
   * This checks across ALL roles the user has.
   * 
   * @param user           the user to check
   * @param permissionName the permission name to check for
   * @return true if user has the permission, false otherwise
   */
  public static boolean hasPermission(User user, String permissionName) {
    return user.getRoles().stream()
        .flatMap(role -> role.getPermissions().stream())
        .anyMatch(permission -> permission.getName().equals(permissionName));
  }

  /**
   * Check if a user has any of the specified permissions.
   * 
   * @param user            the user to check
   * @param permissionNames the permission names to check for
   * @return true if user has any of the permissions, false otherwise
   */
  public static boolean hasAnyPermission(User user, String... permissionNames) {
    Set<String> userPermissions = user.getRoles().stream()
        .flatMap(role -> role.getPermissions().stream())
        .map(Permission::getName)
        .collect(Collectors.toSet());

    for (String permissionName : permissionNames) {
      if (userPermissions.contains(permissionName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check if a user has all of the specified permissions.
   * 
   * @param user            the user to check
   * @param permissionNames the permission names to check for
   * @return true if user has all permissions, false otherwise
   */
  public static boolean hasAllPermissions(User user, String... permissionNames) {
    Set<String> userPermissions = user.getRoles().stream()
        .flatMap(role -> role.getPermissions().stream())
        .map(Permission::getName)
        .collect(Collectors.toSet());

    for (String permissionName : permissionNames) {
      if (!userPermissions.contains(permissionName)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Get all role names for a user.
   * 
   * @param user the user to get roles for
   * @return set of role names
   */
  public static Set<String> getUserRoleNames(User user) {
    return user.getRoles().stream()
        .map(Role::getName)
        .collect(Collectors.toSet());
  }

  /**
   * Check if a user has a specific role.
   * 
   * @param user     the user to check
   * @param roleName the role name to check for
   * @return true if user has the role, false otherwise
   */
  public static boolean hasRole(User user, String roleName) {
    return user.getRoles().stream()
        .anyMatch(role -> role.getName().equals(roleName));
  }

  /**
   * Check if a user has any of the specified roles.
   * 
   * @param user      the user to check
   * @param roleNames the role names to check for
   * @return true if user has any of the roles, false otherwise
   */
  public static boolean hasAnyRole(User user, String... roleNames) {
    Set<String> userRoles = user.getRoles().stream()
        .map(Role::getName)
        .collect(Collectors.toSet());

    for (String roleName : roleNames) {
      if (userRoles.contains(roleName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Print a detailed summary of user's roles and permissions.
   * Useful for debugging and understanding the multiple role system.
   * 
   * @param user the user to analyze
   */
  public static void printUserPermissions(User user) {
    System.out.println("=== User Permission Analysis ===");
    System.out.println("User: " + user.getFullName() + " (" + user.getEmail() + ")");
    System.out.println();

    System.out.println("Roles (" + user.getRoles().size() + "):");
    for (Role role : user.getRoles()) {
      System.out.println("  - " + role.getName() + ": " + role.getDescription());
    }
    System.out.println();

    Set<Permission> allPermissions = getAllUserPermissions(user);
    System.out.println("Total Permissions (" + allPermissions.size() + "):");
    for (Permission permission : allPermissions) {
      System.out.println("  - " + permission.getName() + ": " + permission.getDescription());
    }
    System.out.println();

    // Show which roles contribute which permissions
    System.out.println("Permission Breakdown by Role:");
    for (Role role : user.getRoles()) {
      System.out.println("  " + role.getName() + " role provides:");
      for (Permission permission : role.getPermissions()) {
        System.out.println("    - " + permission.getName());
      }
    }
    System.out.println("================================");
  }
}