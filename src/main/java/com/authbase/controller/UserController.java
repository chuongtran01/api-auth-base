package com.authbase.controller;

import com.authbase.dto.UserDto;
import com.authbase.dto.ProfileUpdateRequest;
import com.authbase.dto.PasswordChangeRequest;
import com.authbase.dto.ApiResponse;
import com.authbase.entity.User;
import com.authbase.mapper.UserMapper;
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
@RequestMapping("/api/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final UserMapper userMapper;

  /**
   * Get current user's profile.
   * 
   * @param authentication current user authentication
   * @param httpRequest    HTTP request to get path for response
   * @return user profile information
   */
  @GetMapping("/profile")
  public ResponseEntity<ApiResponse<UserDto>> getProfile(
      Authentication authentication,
      HttpServletRequest httpRequest) {
    // Get user from authentication - handle both UserDetails and User entities
    User user;
    if (authentication.getPrincipal() instanceof User) {
      user = (User) authentication.getPrincipal();
    } else {
      // If it's UserDetails, we need to get the user from the service
      String email = authentication.getName();
      user = userService.findByEmail(email)
          .orElseThrow(() -> new RuntimeException("User not found"));
    }

    log.debug("Getting profile for user: {}", user.getEmail());

    ApiResponse<UserDto> response = ApiResponse.success(
        "Profile retrieved successfully",
        userMapper.toDto(user),
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
  public ResponseEntity<ApiResponse<UserDto>> updateProfile(
      Authentication authentication,
      @Valid @RequestBody ProfileUpdateRequest request,
      HttpServletRequest httpRequest) {

    // Get user from authentication - handle both UserDetails and User entities
    User user;
    if (authentication.getPrincipal() instanceof User) {
      user = (User) authentication.getPrincipal();
    } else {
      // If it's UserDetails, we need to get the user from the service
      String email = authentication.getName();
      user = userService.findByEmail(email)
          .orElseThrow(() -> new RuntimeException("User not found"));
    }

    log.info("Updating profile for user: {}", user.getEmail());

    try {
      User updatedUser = userService.updateProfile(
          user.getId(),
          request.username(),
          request.firstName(),
          request.lastName());

      ApiResponse<UserDto> response = ApiResponse.success(
          "Profile updated successfully",
          userMapper.toDto(updatedUser),
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

    // Get user from authentication - handle both UserDetails and User entities
    User user;
    if (authentication.getPrincipal() instanceof User) {
      user = (User) authentication.getPrincipal();
    } else {
      // If it's UserDetails, we need to get the user from the service
      String email = authentication.getName();
      user = userService.findByEmail(email)
          .orElseThrow(() -> new RuntimeException("User not found"));
    }

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
    // Get user from authentication - handle both UserDetails and User entities
    User user;
    if (authentication.getPrincipal() instanceof User) {
      user = (User) authentication.getPrincipal();
    } else {
      // If it's UserDetails, we need to get the user from the service
      String email = authentication.getName();
      user = userService.findByEmail(email)
          .orElseThrow(() -> new RuntimeException("User not found"));
    }

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