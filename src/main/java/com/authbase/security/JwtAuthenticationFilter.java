package com.authbase.security;

import com.authbase.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
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

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    try {
      String jwt = getJwtFromRequest(request);

      if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
        // Extract all user information from the token (stateless approach)
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

          authentication.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource()
              .buildDetails(request));

          SecurityContextHolder.getContext().setAuthentication(authentication);

          log.debug("Set stateless authentication for user: {} (ID: {})", username, userId);
        }
      }
    } catch (Exception ex) {
      log.error("Could not set user authentication in security context", ex);
    }

    filterChain.doFilter(request, response);
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
   * Skip filtering for certain paths like authentication endpoints.
   * 
   * @param request HTTP request
   * @return true if should be filtered, false otherwise
   */
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String path = request.getRequestURI();

    // Skip filtering for authentication endpoints and public resources
    return path.startsWith("/api/auth/") ||
        path.startsWith("/api/swagger-ui") ||
        path.startsWith("/api/api-docs") ||
        path.equals("/api/swagger-ui.html") ||
        path.equals("/api/api-docs");
  }
}