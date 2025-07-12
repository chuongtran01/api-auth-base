package com.authbase.controller;

import com.authbase.dto.LoginRequest;
import com.authbase.dto.RefreshTokenRequest;
import com.authbase.dto.LogoutRequest;
import com.authbase.dto.RegisterRequest;
import com.authbase.dto.AuthenticationResponse;
import com.authbase.dto.SuccessResponse;
import com.authbase.dto.UserResponse;
import com.authbase.dto.ErrorResponse;
import com.authbase.service.AuthenticationService;
import com.authbase.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * REST Controller for authentication operations.
 * Handles login, logout, token refresh, and user registration.
 */
@RestController
@RequestMapping("/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

  private final AuthenticationService authenticationService;
  private final UserService userService;

  /**
   * User login endpoint.
   * 
   * @param request login request containing username/email and password
   * @return authentication result with access and refresh tokens
   */
  @PostMapping("/login")
  public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest request) {
    log.info("Login attempt for user: {}", request.username());

    try {
      AuthenticationService.AuthenticationResult result = authenticationService.authenticate(
          request.username(), request.password());

      AuthenticationResponse response = new AuthenticationResponse(
          result.getAccessToken(),
          result.getRefreshToken(),
          result.getUser(),
          "Login successful");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.warn("Login failed for user: {} - {}", request.username(), e.getMessage());
      throw new RuntimeException("Authentication failed: " + e.getMessage());
    }
  }

  /**
   * Refresh access token using refresh token.
   * 
   * @param request refresh token request
   * @return new authentication result with new access token
   */
  @PostMapping("/refresh")
  public ResponseEntity<AuthenticationResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
    log.debug("Token refresh request");

    try {
      AuthenticationService.AuthenticationResult result = authenticationService.refreshToken(
          request.refreshToken());

      AuthenticationResponse response = new AuthenticationResponse(
          result.getAccessToken(),
          result.getRefreshToken(),
          result.getUser(),
          "Token refreshed successfully");

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
  public ResponseEntity<SuccessResponse> logout(@RequestBody LogoutRequest request, HttpServletRequest httpRequest) {
    log.info("Logout request");

    try {
      // Extract access token from Authorization header
      String accessToken = extractAccessToken(httpRequest);

      boolean success = authenticationService.logout(request.refreshToken(), accessToken);

      if (success) {
        SuccessResponse response = new SuccessResponse("Logout successful", true);
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
   * @param request registration request containing email and password
   * @return registration result
   */
  @PostMapping("/register")
  public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
    log.info("Registration attempt for email: {}", request.email());

    try {
      var user = userService.registerUser(request.email(), request.password());

      UserResponse response = new UserResponse("User registered successfully", user, true);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.warn("Registration failed for email: {} - {}", request.email(), e.getMessage());
      throw new RuntimeException("Registration failed: " + e.getMessage());
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
}