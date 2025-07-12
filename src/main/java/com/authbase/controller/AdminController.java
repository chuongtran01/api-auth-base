package com.authbase.controller;

import com.authbase.dto.UserResponse;
import com.authbase.dto.SuccessResponse;
import com.authbase.dto.AccountStatusRequest;
import com.authbase.dto.UserCountResponse;
import com.authbase.dto.RoleCreateRequest;
import com.authbase.dto.RoleUpdateRequest;
import com.authbase.dto.ApiResponse;
import com.authbase.entity.User;
import com.authbase.entity.Role;
import com.authbase.mapper.UserMapper;
import com.authbase.service.UserService;
import com.authbase.service.RoleService;
import com.authbase.security.annotation.RequirePermission;
import com.authbase.security.service.PermissionEvaluatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.List;

/**
 * REST Controller for admin operations.
 * Handles user management, role management, and administrative functions.
 * All endpoints require specific permissions for access.
 */
@RestController
@RequestMapping("/api/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController {

  private final UserService userService;
  private final RoleService roleService;
  private final PermissionEvaluatorService permissionEvaluatorService;

  private final UserMapper userMapper;

  // ============================================================================
  // USER MANAGEMENT ENDPOINTS
  // ============================================================================

  /**
   * Get all users (requires USER_READ permission).
   * 
   * @param httpRequest HTTP request to get path for response
   * @return list of all users
   */
  @GetMapping("/users")
  @RequirePermission("USER_READ")
  public ResponseEntity<ApiResponse<List<User>>> getAllUsers(HttpServletRequest httpRequest) {
    log.info("Admin requesting all users");

    List<User> users = userService.findAllUsers();
    ApiResponse<List<User>> response = ApiResponse.success(
        "Users retrieved successfully",
        users,
        httpRequest.getRequestURI());

    return ResponseEntity.ok(response);
  }

  /**
   * Get user by ID (requires USER_READ permission).
   * 
   * @param userId      user ID
   * @param httpRequest HTTP request to get path for response
   * @return user information
   */
  @GetMapping("/users/{userId}")
  @RequirePermission("USER_READ")
  public ResponseEntity<ApiResponse<UserResponse>> getUserById(
      @PathVariable Long userId,
      HttpServletRequest httpRequest) {
    log.info("Admin requesting user by ID: {}", userId);

    try {
      User user = userService.findById(userId)
          .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

      UserResponse userResponse = new UserResponse("User retrieved successfully", userMapper.toDto(user), true);
      ApiResponse<UserResponse> response = ApiResponse.success(
          "User retrieved successfully",
          userResponse,
          httpRequest.getRequestURI());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.warn("Failed to get user by ID: {} - {}", userId, e.getMessage());
      throw new RuntimeException("Failed to get user: " + e.getMessage());
    }
  }

  /**
   * Update user account status (enable/disable) (requires USER_WRITE permission).
   * 
   * @param userId      user ID
   * @param request     account status update request
   * @param httpRequest HTTP request to get path for response
   * @return updated user
   */
  @PutMapping("/users/{userId}/status")
  @RequirePermission("USER_WRITE")
  public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
      @PathVariable Long userId,
      @Valid @RequestBody AccountStatusRequest request,
      HttpServletRequest httpRequest) {

    log.info("Admin updating user status for user ID: {} to: {}", userId, request.enabled());

    try {
      User updatedUser = userService.updateAccountStatus(userId, request.enabled());

      UserResponse userResponse = new UserResponse(
          "User status updated successfully",
          userMapper.toDto(updatedUser),
          true);
      ApiResponse<UserResponse> response = ApiResponse.success(
          "User status updated successfully",
          userResponse,
          httpRequest.getRequestURI());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.warn("Failed to update user status for user ID: {} - {}", userId, e.getMessage());
      throw new RuntimeException("Failed to update user status: " + e.getMessage());
    }
  }

  /**
   * Delete user account (requires USER_DELETE permission).
   * 
   * @param userId      user ID
   * @param httpRequest HTTP request to get path for response
   * @return success response
   */
  @DeleteMapping("/users/{userId}")
  @RequirePermission("USER_DELETE")
  public ResponseEntity<ApiResponse<Void>> deleteUser(
      @PathVariable Long userId,
      HttpServletRequest httpRequest) {
    log.info("Admin deleting user account: {}", userId);

    try {
      boolean success = userService.deleteUser(userId);

      if (success) {
        ApiResponse<Void> response = ApiResponse.success(
            "User deleted successfully",
            httpRequest.getRequestURI());
        return ResponseEntity.ok(response);
      } else {
        throw new RuntimeException("User deletion failed");
      }
    } catch (Exception e) {
      log.warn("Failed to delete user: {} - {}", userId, e.getMessage());
      throw new RuntimeException("Failed to delete user: " + e.getMessage());
    }
  }

  /**
   * Get user count (requires USER_READ permission).
   * 
   * @param httpRequest HTTP request to get path for response
   * @return total number of users
   */
  @GetMapping("/users/count")
  @RequirePermission("USER_READ")
  public ResponseEntity<ApiResponse<UserCountResponse>> getUserCount(HttpServletRequest httpRequest) {
    log.info("Admin requesting user count");

    long count = userService.getUserCount();
    UserCountResponse userCountResponse = new UserCountResponse(count);
    ApiResponse<UserCountResponse> response = ApiResponse.success(
        "User count retrieved successfully",
        userCountResponse,
        httpRequest.getRequestURI());

    return ResponseEntity.ok(response);
  }

  // ============================================================================
  // ROLE MANAGEMENT ENDPOINTS
  // ============================================================================

  /**
   * Get all roles (requires ROLE_READ permission).
   * 
   * @param httpRequest HTTP request to get path for response
   * @return list of all roles
   */
  @GetMapping("/roles")
  @RequirePermission("ROLE_READ")
  public ResponseEntity<ApiResponse<List<Role>>> getAllRoles(HttpServletRequest httpRequest) {
    log.info("Admin requesting all roles");

    List<Role> roles = roleService.findAllRoles();
    ApiResponse<List<Role>> response = ApiResponse.success(
        "Roles retrieved successfully",
        roles,
        httpRequest.getRequestURI());

    return ResponseEntity.ok(response);
  }

  /**
   * Get role by ID (requires ROLE_READ permission).
   * 
   * @param roleId      role ID
   * @param httpRequest HTTP request to get path for response
   * @return role information
   */
  @GetMapping("/roles/{roleId}")
  @RequirePermission("ROLE_READ")
  public ResponseEntity<ApiResponse<Role>> getRoleById(
      @PathVariable Long roleId,
      HttpServletRequest httpRequest) {
    log.info("Admin requesting role by ID: {}", roleId);

    try {
      Role role = roleService.findById(roleId)
          .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

      ApiResponse<Role> response = ApiResponse.success(
          "Role retrieved successfully",
          role,
          httpRequest.getRequestURI());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.warn("Failed to get role by ID: {} - {}", roleId, e.getMessage());
      throw new RuntimeException("Failed to get role: " + e.getMessage());
    }
  }

  /**
   * Create new role (requires ROLE_WRITE permission).
   * 
   * @param request     role creation request
   * @param httpRequest HTTP request to get path for response
   * @return created role
   */
  @PostMapping("/roles")
  @RequirePermission("ROLE_WRITE")
  public ResponseEntity<ApiResponse<Role>> createRole(
      @Valid @RequestBody RoleCreateRequest request,
      HttpServletRequest httpRequest) {
    log.info("Admin creating new role: {}", request.name());

    try {
      Role role = roleService.createRole(request.name(), request.description());
      ApiResponse<Role> response = ApiResponse.success(
          "Role created successfully",
          role,
          httpRequest.getRequestURI());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.warn("Failed to create role: {} - {}", request.name(), e.getMessage());
      throw new RuntimeException("Failed to create role: " + e.getMessage());
    }
  }

  /**
   * Update role (requires ROLE_WRITE permission).
   * 
   * @param roleId      role ID
   * @param request     role update request
   * @param httpRequest HTTP request to get path for response
   * @return updated role
   */
  @PutMapping("/roles/{roleId}")
  @RequirePermission("ROLE_WRITE")
  public ResponseEntity<ApiResponse<Role>> updateRole(
      @PathVariable Long roleId,
      @Valid @RequestBody RoleUpdateRequest request,
      HttpServletRequest httpRequest) {

    log.info("Admin updating role ID: {}", roleId);

    try {
      Role role = roleService.updateRoleDescription(roleId, request.description());
      ApiResponse<Role> response = ApiResponse.success(
          "Role updated successfully",
          role,
          httpRequest.getRequestURI());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.warn("Failed to update role ID: {} - {}", roleId, e.getMessage());
      throw new RuntimeException("Failed to update role: " + e.getMessage());
    }
  }

  /**
   * Delete role (requires ROLE_DELETE permission).
   * 
   * @param roleId      role ID
   * @param httpRequest HTTP request to get path for response
   * @return success response
   */
  @DeleteMapping("/roles/{roleId}")
  @RequirePermission("ROLE_DELETE")
  public ResponseEntity<ApiResponse<Void>> deleteRole(
      @PathVariable Long roleId,
      HttpServletRequest httpRequest) {
    log.info("Admin deleting role ID: {}", roleId);

    try {
      boolean success = roleService.deleteRole(roleId);

      if (success) {
        ApiResponse<Void> response = ApiResponse.success(
            "Role deleted successfully",
            httpRequest.getRequestURI());
        return ResponseEntity.ok(response);
      } else {
        throw new RuntimeException("Role deletion failed");
      }
    } catch (Exception e) {
      log.warn("Failed to delete role ID: {} - {}", roleId, e.getMessage());
      throw new RuntimeException("Failed to delete role: " + e.getMessage());
    }
  }

  /**
   * Assign role to user (requires ROLE_WRITE permission).
   * 
   * @param userId      user ID
   * @param roleId      role ID
   * @param httpRequest HTTP request to get path for response
   * @return success response
   */
  @PostMapping("/users/{userId}/roles/{roleId}")
  @RequirePermission("ROLE_WRITE")
  public ResponseEntity<ApiResponse<Void>> assignRoleToUser(
      @PathVariable Long userId,
      @PathVariable Long roleId,
      HttpServletRequest httpRequest) {

    log.info("Admin assigning role {} to user {}", roleId, userId);

    try {
      User updatedUser = roleService.assignRoleToUser(userId, roleId);

      ApiResponse<Void> response = ApiResponse.success(
          "Role assigned successfully",
          httpRequest.getRequestURI());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.warn("Failed to assign role {} to user {} - {}", roleId, userId, e.getMessage());
      throw new RuntimeException("Failed to assign role: " + e.getMessage());
    }
  }

  /**
   * Remove role from user (requires ROLE_WRITE permission).
   * 
   * @param userId      user ID
   * @param roleId      role ID
   * @param httpRequest HTTP request to get path for response
   * @return success response
   */
  @DeleteMapping("/users/{userId}/roles/{roleId}")
  @RequirePermission("ROLE_WRITE")
  public ResponseEntity<ApiResponse<Void>> removeRoleFromUser(
      @PathVariable Long userId,
      @PathVariable Long roleId,
      HttpServletRequest httpRequest) {

    log.info("Admin removing role {} from user {}", roleId, userId);

    try {
      User updatedUser = roleService.removeRoleFromUser(userId, roleId);

      ApiResponse<Void> response = ApiResponse.success(
          "Role removed successfully",
          httpRequest.getRequestURI());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.warn("Failed to remove role {} from user {} - {}", roleId, userId, e.getMessage());
      throw new RuntimeException("Failed to remove role: " + e.getMessage());
    }
  }

  // ============================================================================
  // DEMONSTRATION ENDPOINTS
  // ============================================================================

  /**
   * Example of programmatic permission checking.
   * This endpoint demonstrates how to check permissions dynamically based on
   * business logic.
   * 
   * @param userId      user ID to check permissions for
   * @param httpRequest HTTP request to get path for response
   * @return permission information
   */
  @GetMapping("/users/{userId}/permissions")
  @RequirePermission("USER_READ")
  public ResponseEntity<ApiResponse<String>> checkUserPermissions(
      @PathVariable Long userId,
      HttpServletRequest httpRequest) {
    log.info("Checking permissions for user ID: {}", userId);

    // Example of programmatic permission checking
    boolean canReadUsers = permissionEvaluatorService.canRead("USER");
    boolean canWriteUsers = permissionEvaluatorService.canWrite("USER");
    boolean canDeleteUsers = permissionEvaluatorService.canDelete("USER");
    boolean hasAdminAccess = permissionEvaluatorService.hasAdminAccess();

    String message = String.format(
        "Permission check results - Read: %s, Write: %s, Delete: %s, Admin: %s",
        canReadUsers, canWriteUsers, canDeleteUsers, hasAdminAccess);

    log.info(message);
    ApiResponse<String> response = ApiResponse.success(
        "Permission check completed",
        message,
        httpRequest.getRequestURI());

    return ResponseEntity.ok(response);
  }
}