package com.authbase.controller;

import com.authbase.dto.LoginRequest;
import com.authbase.dto.RefreshTokenRequest;
import com.authbase.dto.LogoutRequest;
import com.authbase.dto.RegisterRequest;
import com.authbase.dto.AuthenticationResponse;
import com.authbase.dto.UserResponse;
import com.authbase.dto.ForgotPasswordRequest;
import com.authbase.dto.ResetPasswordRequest;
import com.authbase.dto.VerifyEmailRequest;
import com.authbase.dto.ApiResponse;
import com.authbase.mapper.UserMapper;
import com.authbase.service.AuthenticationService;
import com.authbase.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * REST Controller for authentication operations.
 * Handles login, logout, token refresh, and user registration.
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

  private final AuthenticationService authenticationService;
  private final UserService userService;
  private final UserMapper userMapper;

  /**
   * User login endpoint.
   * 
   * @param request     login request containing username/email and password
   * @param httpRequest HTTP request to get path for response
   * @return authentication result with access and refresh tokens
   */
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
      @Valid @RequestBody LoginRequest request,
      HttpServletRequest httpRequest) {
    log.info("Login attempt for user: {}", request.email());

    try {
      // Extract IP address and user agent for security logging
      String ipAddress = getClientIpAddress(httpRequest);
      String userAgent = httpRequest.getHeader("User-Agent");

      AuthenticationService.AuthenticationResult result = authenticationService.authenticate(
          request.email(), request.password(), ipAddress, userAgent);

      AuthenticationResponse authResponse = new AuthenticationResponse(
          result.getAccessToken(),
          result.getRefreshToken(),
          userMapper.toDto(result.getUser()),
          "Login successful");

      ApiResponse<AuthenticationResponse> response = ApiResponse.success(
          "Login successful",
          authResponse,
          httpRequest.getRequestURI());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.warn("Login failed for user: {} - {}", request.email(), e.getMessage());
      throw new RuntimeException("Authentication failed: " + e.getMessage());
    }
  }

  /**
   * Refresh access token using refresh token.
   * 
   * @param request     refresh token request
   * @param httpRequest HTTP request to get path for response
   * @return new authentication result with new access token
   */
  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(
      @Valid @RequestBody RefreshTokenRequest request,
      HttpServletRequest httpRequest) {
    log.debug("Token refresh request");

    try {
      AuthenticationService.AuthenticationResult result = authenticationService.refreshToken(
          request.refreshToken());

      AuthenticationResponse authResponse = new AuthenticationResponse(
          result.getAccessToken(),
          result.getRefreshToken(),
          userMapper.toDto(result.getUser()),
          "Token refreshed successfully");

      ApiResponse<AuthenticationResponse> response = ApiResponse.success(
          "Token refreshed successfully",
          authResponse,
          httpRequest.getRequestURI());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.warn("Token refresh failed: {}", e.getMessage());
      throw new RuntimeException("Token refresh failed: " + e.getMessage());
    }
  }

  /**
   * User logout endpoint.
   * Blacklists the access token and invalidates the refresh token.
   * 
   * @param request     logout request containing refresh token
   * @param httpRequest HTTP request to extract access token from Authorization
   *                    header
   * @return logout result
   */
  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(
      @Valid @RequestBody LogoutRequest request,
      HttpServletRequest httpRequest) {
    log.info("Logout request");

    try {
      // Extract access token from Authorization header
      String accessToken = extractAccessToken(httpRequest);

      boolean success = authenticationService.logout(request.refreshToken(), accessToken);

      if (success) {
        ApiResponse<Void> response = ApiResponse.success(
            "Logout successful",
            httpRequest.getRequestURI());
        return ResponseEntity.ok(response);
      } else {
        throw new RuntimeException("Invalid refresh token");
      }
    } catch (Exception e) {
      log.warn("Logout failed: {}", e.getMessage());
      throw new RuntimeException("Logout failed: " + e.getMessage());
    }
  }

  /**
   * User registration endpoint.
   * 
   * @param request     registration request containing email and password
   * @param httpRequest HTTP request to get path for response
   * @return registration result
   */
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<UserResponse>> register(
      @Valid @RequestBody RegisterRequest request,
      HttpServletRequest httpRequest) {
    log.info("Registration attempt for email: {}", request.email());

    try {
      var user = userService.registerUser(request.email(), request.password());

      UserResponse userResponse = new UserResponse("User registered successfully", userMapper.toDto(user), true);
      ApiResponse<UserResponse> response = ApiResponse.success(
          "User registered successfully",
          userResponse,
          httpRequest.getRequestURI());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.warn("Registration failed for email: {} - {}", request.email(), e.getMessage());
      throw new RuntimeException("Registration failed: " + e.getMessage());
    }
  }

  /**
   * Forgot password endpoint.
   * Sends password reset email to the user.
   * 
   * @param request     forgot password request containing email
   * @param httpRequest HTTP request to get path for response
   * @return success response
   */
  @PostMapping("/forgot-password")
  public ResponseEntity<ApiResponse<Void>> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequest request,
      HttpServletRequest httpRequest) {
    log.info("Password reset request for email: {}", request.email());

    try {
      boolean success = userService.sendPasswordResetEmail(request.email());

      if (success) {
        ApiResponse<Void> response = ApiResponse.success(
            "Password reset email sent successfully",
            httpRequest.getRequestURI());
        return ResponseEntity.ok(response);
      } else {
        throw new RuntimeException("Failed to send password reset email");
      }
    } catch (Exception e) {
      log.warn("Password reset request failed for email: {} - {}", request.email(), e.getMessage());
      throw new RuntimeException("Password reset request failed: " + e.getMessage());
    }
  }

  /**
   * Reset password endpoint.
   * Resets user password using reset token.
   * 
   * @param request     reset password request containing token and new password
   * @param httpRequest HTTP request to get path for response
   * @return success response
   */
  @PostMapping("/reset-password")
  public ResponseEntity<ApiResponse<Void>> resetPassword(
      @Valid @RequestBody ResetPasswordRequest request,
      HttpServletRequest httpRequest) {
    log.info("Password reset attempt with token");

    try {
      boolean success = userService.resetPassword(
          request.token(),
          request.newPassword());

      if (success) {
        ApiResponse<Void> response = ApiResponse.success(
            "Password reset successfully",
            httpRequest.getRequestURI());
        return ResponseEntity.ok(response);
      } else {
        throw new RuntimeException("Password reset failed");
      }
    } catch (Exception e) {
      log.warn("Password reset failed: {}", e.getMessage());
      throw new RuntimeException("Password reset failed: " + e.getMessage());
    }
  }

  /**
   * Email verification endpoint.
   * Verifies user email using verification token.
   * 
   * @param request     verify email request containing token
   * @param httpRequest HTTP request to get path for response
   * @return success response
   */
  @PostMapping("/verify-email")
  public ResponseEntity<ApiResponse<Void>> verifyEmail(
      @Valid @RequestBody VerifyEmailRequest request,
      HttpServletRequest httpRequest) {
    log.info("Email verification attempt with token");

    try {
      boolean success = userService.verifyEmail(request.token());

      if (success) {
        ApiResponse<Void> response = ApiResponse.success(
            "Email verified successfully",
            httpRequest.getRequestURI());
        return ResponseEntity.ok(response);
      } else {
        throw new RuntimeException("Email verification failed");
      }
    } catch (Exception e) {
      log.warn("Email verification failed: {}", e.getMessage());
      throw new RuntimeException("Email verification failed: " + e.getMessage());
    }
  }

  /**
   * Extract access token from Authorization header.
   * 
   * @param request HTTP request
   * @return access token or null if not found
   */
  private String extractAccessToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  /**
   * Extract client IP address from the request.
   * 
   * @param request HTTP request
   * @return client IP address
   */
  private String getClientIpAddress(HttpServletRequest request) {
    String ipAddress = request.getHeader("X-Forwarded-For");
    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("Proxy-Client-IP");
    }
    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("HTTP_CLIENT_IP");
    }
    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
    }
    if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getRemoteAddr();
    }
    return ipAddress;
  }
}