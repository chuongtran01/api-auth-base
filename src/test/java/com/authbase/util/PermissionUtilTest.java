package com.authbase.util;

import com.authbase.entity.Permission;
import com.authbase.entity.Role;
import com.authbase.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class demonstrating the power of multiple roles per user.
 * Shows how permissions are combined from multiple roles.
 */
class PermissionUtilTest {

  private User user;
  private Role employeeRole;
  private Role salesRole;
  private Role supportRole;
  private Role adminRole;

  @BeforeEach
  void setUp() {
    // Create permissions
    Permission userRead = new Permission("USER_READ", "Read user information");
    Permission userWrite = new Permission("USER_WRITE", "Create and update users");
    Permission salesProcess = new Permission("SALES_PROCESS_ORDER", "Process sales orders");
    Permission salesView = new Permission("SALES_VIEW_CUSTOMER", "View customer information");
    Permission supportView = new Permission("SUPPORT_VIEW_TICKET", "View support tickets");
    Permission supportUpdate = new Permission("SUPPORT_UPDATE_TICKET", "Update support tickets");
    Permission adminAccess = new Permission("ADMIN_ACCESS", "Full administrative access");

    // Create roles with permissions
    employeeRole = new Role("EMPLOYEE", "Basic employee access");
    employeeRole.addPermission(userRead);

    salesRole = new Role("SALES", "Sales operations");
    salesRole.addPermission(salesProcess);
    salesRole.addPermission(salesView);
    salesRole.addPermission(userRead); // Sales can also read users

    supportRole = new Role("SUPPORT", "Customer support");
    supportRole.addPermission(supportView);
    supportRole.addPermission(supportUpdate);
    supportRole.addPermission(userRead); // Support can also read users

    adminRole = new Role("ADMIN", "Administrator");
    adminRole.addPermission(adminAccess);
    adminRole.addPermission(userRead);
    adminRole.addPermission(userWrite);

    // Create user
    user = new User("john@company.com", "john", "password");
    user.setFirstName("John");
    user.setLastName("Smith");
  }

  @Test
  void testSingleRole() {
    // User has only employee role
    user.addRole(employeeRole);

    // Check permissions
    assertTrue(PermissionUtil.hasPermission(user, "USER_READ"));
    assertFalse(PermissionUtil.hasPermission(user, "SALES_PROCESS_ORDER"));
    assertFalse(PermissionUtil.hasPermission(user, "ADMIN_ACCESS"));

    // Check roles
    assertTrue(PermissionUtil.hasRole(user, "EMPLOYEE"));
    assertFalse(PermissionUtil.hasRole(user, "SALES"));
    assertFalse(PermissionUtil.hasRole(user, "ADMIN"));

    // Get all permissions
    Set<Permission> permissions = PermissionUtil.getAllUserPermissions(user);
    assertEquals(1, permissions.size());
    assertTrue(permissions.stream().anyMatch(p -> p.getName().equals("USER_READ")));
  }

  @Test
  void testMultipleRoles() {
    // User has multiple roles
    user.addRole(employeeRole);
    user.addRole(salesRole);
    user.addRole(supportRole);

    // Check permissions from all roles
    assertTrue(PermissionUtil.hasPermission(user, "USER_READ")); // From employee, sales, support
    assertTrue(PermissionUtil.hasPermission(user, "SALES_PROCESS_ORDER")); // From sales
    assertTrue(PermissionUtil.hasPermission(user, "SALES_VIEW_CUSTOMER")); // From sales
    assertTrue(PermissionUtil.hasPermission(user, "SUPPORT_VIEW_TICKET")); // From support
    assertTrue(PermissionUtil.hasPermission(user, "SUPPORT_UPDATE_TICKET")); // From support
    assertFalse(PermissionUtil.hasPermission(user, "ADMIN_ACCESS")); // Not from any role

    // Check roles
    assertTrue(PermissionUtil.hasRole(user, "EMPLOYEE"));
    assertTrue(PermissionUtil.hasRole(user, "SALES"));
    assertTrue(PermissionUtil.hasRole(user, "SUPPORT"));
    assertFalse(PermissionUtil.hasRole(user, "ADMIN"));

    // Get all permissions (should be 5 unique permissions)
    Set<Permission> permissions = PermissionUtil.getAllUserPermissions(user);
    assertEquals(5, permissions.size());

    // Verify all expected permissions are present
    Set<String> permissionNames = permissions.stream()
        .map(Permission::getName)
        .collect(java.util.stream.Collectors.toSet());

    assertTrue(permissionNames.contains("USER_READ"));
    assertTrue(permissionNames.contains("SALES_PROCESS_ORDER"));
    assertTrue(permissionNames.contains("SALES_VIEW_CUSTOMER"));
    assertTrue(permissionNames.contains("SUPPORT_VIEW_TICKET"));
    assertTrue(permissionNames.contains("SUPPORT_UPDATE_TICKET"));
  }

  @Test
  void testPermissionInheritance() {
    // User starts with employee role
    user.addRole(employeeRole);

    // Add sales role - user now has employee + sales permissions
    user.addRole(salesRole);

    // Add support role - user now has employee + sales + support permissions
    user.addRole(supportRole);

    // Add admin role - user now has ALL permissions
    user.addRole(adminRole);

    // User should have ALL permissions from ALL roles
    assertTrue(PermissionUtil.hasPermission(user, "USER_READ")); // From multiple roles
    assertTrue(PermissionUtil.hasPermission(user, "USER_WRITE")); // From admin
    assertTrue(PermissionUtil.hasPermission(user, "SALES_PROCESS_ORDER")); // From sales
    assertTrue(PermissionUtil.hasPermission(user, "SUPPORT_VIEW_TICKET")); // From support
    assertTrue(PermissionUtil.hasPermission(user, "ADMIN_ACCESS")); // From admin

    // Get total permissions
    Set<Permission> permissions = PermissionUtil.getAllUserPermissions(user);
    assertEquals(7, permissions.size()); // All unique permissions from all roles
  }

  @Test
  void testRoleRemoval() {
    // User has multiple roles
    user.addRole(employeeRole);
    user.addRole(salesRole);
    user.addRole(supportRole);

    // Verify user has sales permissions
    assertTrue(PermissionUtil.hasPermission(user, "SALES_PROCESS_ORDER"));

    // Remove sales role
    user.removeRole(salesRole);

    // User should no longer have sales permissions
    assertFalse(PermissionUtil.hasPermission(user, "SALES_PROCESS_ORDER"));
    assertFalse(PermissionUtil.hasPermission(user, "SALES_VIEW_CUSTOMER"));

    // But should still have other permissions
    assertTrue(PermissionUtil.hasPermission(user, "USER_READ")); // From employee and support
    assertTrue(PermissionUtil.hasPermission(user, "SUPPORT_VIEW_TICKET")); // From support
    assertTrue(PermissionUtil.hasPermission(user, "SUPPORT_UPDATE_TICKET")); // From support

    // Check roles
    assertTrue(PermissionUtil.hasRole(user, "EMPLOYEE"));
    assertFalse(PermissionUtil.hasRole(user, "SALES"));
    assertTrue(PermissionUtil.hasRole(user, "SUPPORT"));
  }

  @Test
  void testPermissionCheckingMethods() {
    // User has multiple roles
    user.addRole(employeeRole);
    user.addRole(salesRole);
    user.addRole(supportRole);

    // Test hasAnyPermission
    assertTrue(PermissionUtil.hasAnyPermission(user, "SALES_PROCESS_ORDER", "ADMIN_ACCESS"));
    assertTrue(PermissionUtil.hasAnyPermission(user, "USER_READ", "SALES_PROCESS_ORDER"));
    assertFalse(PermissionUtil.hasAnyPermission(user, "ADMIN_ACCESS", "USER_WRITE"));

    // Test hasAllPermissions
    assertTrue(PermissionUtil.hasAllPermissions(user, "USER_READ", "SALES_PROCESS_ORDER"));
    assertFalse(PermissionUtil.hasAllPermissions(user, "USER_READ", "ADMIN_ACCESS"));

    // Test hasAnyRole
    assertTrue(PermissionUtil.hasAnyRole(user, "SALES", "ADMIN"));
    assertTrue(PermissionUtil.hasAnyRole(user, "EMPLOYEE", "SUPPORT"));
    assertFalse(PermissionUtil.hasAnyRole(user, "ADMIN", "MANAGER"));
  }

  @Test
  void testGetUserRoleNames() {
    // User has multiple roles
    user.addRole(employeeRole);
    user.addRole(salesRole);
    user.addRole(supportRole);

    Set<String> roleNames = PermissionUtil.getUserRoleNames(user);
    assertEquals(3, roleNames.size());
    assertTrue(roleNames.contains("EMPLOYEE"));
    assertTrue(roleNames.contains("SALES"));
    assertTrue(roleNames.contains("SUPPORT"));
    assertFalse(roleNames.contains("ADMIN"));
  }

  @Test
  void testRealWorldScenario() {
    // Simulate a senior sales representative with multiple responsibilities
    user.addRole(employeeRole); // Basic employee access
    user.addRole(salesRole); // Sales operations
    user.addRole(supportRole); // Customer support capabilities

    // This user can:
    // - Read user information (from employee, sales, support roles)
    // - Process sales orders (from sales role)
    // - View customer information (from sales role)
    // - Handle support tickets (from support role)

    // Verify capabilities
    assertTrue(PermissionUtil.hasPermission(user, "USER_READ"));
    assertTrue(PermissionUtil.hasPermission(user, "SALES_PROCESS_ORDER"));
    assertTrue(PermissionUtil.hasPermission(user, "SALES_VIEW_CUSTOMER"));
    assertTrue(PermissionUtil.hasPermission(user, "SUPPORT_VIEW_TICKET"));
    assertTrue(PermissionUtil.hasPermission(user, "SUPPORT_UPDATE_TICKET"));

    // Cannot do admin tasks
    assertFalse(PermissionUtil.hasPermission(user, "ADMIN_ACCESS"));
    assertFalse(PermissionUtil.hasPermission(user, "USER_WRITE"));

    // Total permissions: 5 unique permissions from 3 roles
    Set<Permission> permissions = PermissionUtil.getAllUserPermissions(user);
    assertEquals(5, permissions.size());

    System.out.println("=== Senior Sales Representative Permissions ===");
    PermissionUtil.printUserPermissions(user);
  }
}