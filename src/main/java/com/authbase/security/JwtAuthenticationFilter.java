package com.authbase.security;

import com.authbase.config.WhitelistProperties;
import com.authbase.service.RedisTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Stateless JWT Authentication Filter that extracts all user information from
 * JWT tokens.
 * No database calls are made during request processing - all user data comes
 * from the token.
 * This makes the API truly stateless and improves performance.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider tokenProvider;
  private final RedisTokenService redisTokenService;
  private final WhitelistProperties whitelistProperties;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    try {
      String jwt = getJwtFromRequest(request);

      if (StringUtils.hasText(jwt)) {
        // Check if token is blacklisted first
        if (redisTokenService.isTokenBlacklisted(jwt)) {
          log.warn("Request with blacklisted token rejected: {}", request.getRequestURI());
          handleAuthenticationError(response, "Token has been invalidated", "TOKEN_BLACKLISTED");
          return;
        }

        // Validate token structure and signature
        try {
          tokenProvider.validateToken(jwt);
        } catch (ExpiredJwtException ex) {
          log.warn("Expired JWT token for request: {}", request.getRequestURI(), ex);
          handleAuthenticationError(response, "Token has expired", "TOKEN_EXPIRED");
          return;
        } catch (MalformedJwtException ex) {
          log.warn("Malformed JWT token for request: {}", request.getRequestURI(), ex);
          handleAuthenticationError(response, "Invalid token format", "TOKEN_MALFORMED");
          return;
        } catch (UnsupportedJwtException ex) {
          log.warn("Unsupported JWT token for request: {}", request.getRequestURI(), ex);
          handleAuthenticationError(response, "Unsupported token format", "TOKEN_UNSUPPORTED");
          return;
        } catch (SecurityException ex) {
          log.warn("Invalid JWT signature for request: {}", request.getRequestURI(), ex);
          handleAuthenticationError(response, "Invalid token signature", "TOKEN_INVALID_SIGNATURE");
          return;
        } catch (IllegalArgumentException ex) {
          log.warn("Invalid JWT claims for request: {}", request.getRequestURI(), ex);
          handleAuthenticationError(response, "Invalid token claims", "TOKEN_INVALID_CLAIMS");
          return;
        }

        // Extract user information from the token
        String username = tokenProvider.getUsernameFromToken(jwt);
        Long userId = tokenProvider.getUserIdFromToken(jwt);
        String email = tokenProvider.getEmailFromToken(jwt);
        String rolesString = tokenProvider.getRolesFromToken(jwt);

        if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
          // Create UserDetails from token claims (no database call)
          UserDetails userDetails = createUserDetailsFromToken(username, userId, email, rolesString);

          // Create authorities from roles in token
          List<SimpleGrantedAuthority> authorities = getAuthoritiesFromRoles(rolesString);

          UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
              userDetails, null, authorities);

          authentication
              .setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource()
                  .buildDetails(request));

          SecurityContextHolder.getContext().setAuthentication(authentication);

          log.debug("Set stateless authentication for user: {} (ID: {})", username, userId);
        }
      }
    } catch (Exception ex) {
      // Log the full exception for debugging (server-side only)
      log.error("Unexpected error during JWT authentication for request: {}", request.getRequestURI(), ex);

      // Return generic error to client (no stack trace)
      handleAuthenticationError(response, "Authentication failed", "AUTHENTICATION_ERROR");
      return;
    }

    filterChain.doFilter(request, response);
  }

  /**
   * Handle authentication errors by returning structured JSON responses.
   * This prevents stack traces from being exposed to the client.
   */
  private void handleAuthenticationError(HttpServletResponse response, String message, String errorCode)
      throws IOException {

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("success", false);
    errorResponse.put("message", message);
    errorResponse.put("errorCode", errorCode);
    errorResponse.put("timestamp", LocalDateTime.now().toString());

    String jsonResponse = objectMapper.writeValueAsString(errorResponse);
    response.getWriter().write(jsonResponse);
  }

  /**
   * Create UserDetails object from JWT token claims.
   * This eliminates the need for database calls during authentication.
   * 
   * @param username    username from token
   * @param userId      user ID from token
   * @param email       email from token
   * @param rolesString roles string from token
   * @return UserDetails object
   */
  private UserDetails createUserDetailsFromToken(String username, Long userId, String email, String rolesString) {
    return org.springframework.security.core.userdetails.User.builder()
        .username(username)
        .password("") // No password needed for JWT authentication
        .authorities(getAuthoritiesFromRoles(rolesString))
        .accountExpired(false)
        .accountLocked(false)
        .credentialsExpired(false)
        .disabled(false)
        .build();
  }

  /**
   * Extract JWT token from Authorization header.
   * 
   * @param request HTTP request
   * @return JWT token or null if not found
   */
  private String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");

    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }

    return null;
  }

  /**
   * Convert roles string from token to Spring Security authorities.
   * 
   * @param rolesString comma-separated roles string
   * @return list of SimpleGrantedAuthority
   */
  private List<SimpleGrantedAuthority> getAuthoritiesFromRoles(String rolesString) {
    if (!StringUtils.hasText(rolesString)) {
      return List.of();
    }

    return Arrays.stream(rolesString.split(","))
        .map(String::trim)
        .filter(StringUtils::hasText)
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
        .collect(Collectors.toList());
  }

  /**
   * Check if the request should be filtered.
   * Skip filtering for whitelisted endpoints that don't require authentication.
   * 
   * @param request HTTP request
   * @return true if should NOT be filtered (whitelisted), false otherwise
   */
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String path = request.getRequestURI();
    String method = request.getMethod();

    // Use WhitelistProperties to check if the path is whitelisted
    boolean isWhitelisted = whitelistProperties.isWhitelisted(path);

    if (isWhitelisted) {
      log.debug("JWT filter SKIPPED for whitelisted endpoint: {} {}", method, path);
    } else {
      log.debug("JWT filter will PROCESS protected endpoint: {} {}", method, path);
    }

    return isWhitelisted;
  }
}