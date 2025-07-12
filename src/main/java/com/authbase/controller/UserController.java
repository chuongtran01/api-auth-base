package com.authbase.controller;

import com.authbase.dto.UserResponse;
import com.authbase.dto.SuccessResponse;
import com.authbase.dto.ProfileUpdateRequest;
import com.authbase.dto.PasswordChangeRequest;
import com.authbase.dto.ApiResponse;
import com.authbase.entity.User;
import com.authbase.service.UserService;
import com.authbase.security.annotation.RequirePermission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * REST Controller for user profile management operations.
 * Handles user profile retrieval, updates, password changes, and account
 * deletion.
 */
@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  /**
   * Get current user's profile.
   * 
   * @param authentication current user authentication
   * @param httpRequest    HTTP request to get path for response
   * @return user profile information
   */
  @GetMapping("/profile")
  public ResponseEntity<ApiResponse<UserResponse>> getProfile(
      Authentication authentication,
      HttpServletRequest httpRequest) {
    User user = (User) authentication.getPrincipal();
    log.debug("Getting profile for user: {}", user.getEmail());

    UserResponse userResponse = new UserResponse("Profile retrieved successfully", user, true);
    ApiResponse<UserResponse> response = ApiResponse.success(
        "Profile retrieved successfully",
        userResponse,
        httpRequest.getRequestURI());

    return ResponseEntity.ok(response);
  }

  /**
   * Update current user's profile.
   * 
   * @param authentication current user authentication
   * @param request        profile update request
   * @param httpRequest    HTTP request to get path for response
   * @return updated user profile
   */
  @PutMapping("/profile")
  public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
      Authentication authentication,
      @Valid @RequestBody ProfileUpdateRequest request,
      HttpServletRequest httpRequest) {

    User user = (User) authentication.getPrincipal();
    log.info("Updating profile for user: {}", user.getEmail());

    try {
      User updatedUser = userService.updateProfile(
          user.getId(),
          request.username(),
          request.firstName(),
          request.lastName());

      UserResponse userResponse = new UserResponse("Profile updated successfully", updatedUser, true);
      ApiResponse<UserResponse> response = ApiResponse.success(
          "Profile updated successfully",
          userResponse,
          httpRequest.getRequestURI());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.warn("Profile update failed for user: {} - {}", user.getEmail(), e.getMessage());
      throw new RuntimeException("Profile update failed: " + e.getMessage());
    }
  }

  /**
   * Change current user's password.
   * 
   * @param authentication current user authentication
   * @param request        password change request
   * @param httpRequest    HTTP request to get path for response
   * @return success response
   */
  @PutMapping("/password")
  public ResponseEntity<ApiResponse<Void>> changePassword(
      Authentication authentication,
      @Valid @RequestBody PasswordChangeRequest request,
      HttpServletRequest httpRequest) {

    User user = (User) authentication.getPrincipal();
    log.info("Changing password for user: {}", user.getEmail());

    try {
      boolean success = userService.changePassword(
          user.getId(),
          request.currentPassword(),
          request.newPassword());

      if (success) {
        ApiResponse<Void> response = ApiResponse.success(
            "Password changed successfully",
            httpRequest.getRequestURI());
        return ResponseEntity.ok(response);
      } else {
        throw new RuntimeException("Password change failed");
      }
    } catch (Exception e) {
      log.warn("Password change failed for user: {} - {}", user.getEmail(), e.getMessage());
      throw new RuntimeException("Password change failed: " + e.getMessage());
    }
  }

  /**
   * Delete current user's account (requires USER_DELETE permission or own
   * account).
   * 
   * @param authentication current user authentication
   * @param httpRequest    HTTP request to get path for response
   * @return success response
   */
  @DeleteMapping("/account")
  @RequirePermission(value = "USER_DELETE", orPermissions = { "ADMIN_ACCESS" })
  public ResponseEntity<ApiResponse<Void>> deleteAccount(
      Authentication authentication,
      HttpServletRequest httpRequest) {
    User user = (User) authentication.getPrincipal();
    log.info("Deleting account for user: {}", user.getEmail());

    try {
      boolean success = userService.deleteUser(user.getId());

      if (success) {
        ApiResponse<Void> response = ApiResponse.success(
            "Account deleted successfully",
            httpRequest.getRequestURI());
        return ResponseEntity.ok(response);
      } else {
        throw new RuntimeException("Account deletion failed");
      }
    } catch (Exception e) {
      log.warn("Account deletion failed for user: {} - {}", user.getEmail(), e.getMessage());
      throw new RuntimeException("Account deletion failed: " + e.getMessage());
    }
  }
}