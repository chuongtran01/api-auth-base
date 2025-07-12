package com.authbase.security;

import com.authbase.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT Token Provider for generating and validating JWT tokens.
 * Handles access tokens and refresh tokens with proper security measures.
 */
@Component
public class JwtTokenProvider {

  private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.access-token-expiration}")
  private long accessTokenExpiration;

  @Value("${jwt.refresh-token-expiration}")
  private long refreshTokenExpiration;

  /**
   * Generate access token for authenticated user.
   * 
   * @param authentication the authentication object
   * @return JWT access token
   */
  public String generateAccessToken(Authentication authentication) {
    String username = authentication.getName();
    String email = username; // For UserDetails, username is typically the email

    // Extract user ID and roles from authorities
    Long userId = null;
    String roles = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(","));

    // Try to get additional user info if it's our custom User entity
    if (authentication.getPrincipal() instanceof com.authbase.entity.User) {
      com.authbase.entity.User user = (com.authbase.entity.User) authentication.getPrincipal();
      userId = user.getId();
      email = user.getEmail();
    }

    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

    return Jwts.builder()
        .subject(username)
        .claim("userId", userId)
        .claim("email", email)
        .claim("roles", roles)
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(getSigningKey(), Jwts.SIG.HS512)
        .compact();
  }

  /**
   * Generate access token for user entity.
   * 
   * @param user the user entity
   * @return JWT access token
   */
  public String generateAccessToken(com.authbase.entity.User user) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

    // Get user roles as comma-separated string
    String roles = user.getRoles().stream()
        .map(role -> "ROLE_" + role.getName())
        .collect(Collectors.joining(","));

    return Jwts.builder()
        .subject(user.getEmail())
        .claim("userId", user.getId())
        .claim("email", user.getEmail())
        .claim("roles", roles)
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(getSigningKey(), Jwts.SIG.HS512)
        .compact();
  }

  /**
   * Generate refresh token for user.
   * 
   * @param user the user
   * @return JWT refresh token
   */
  public String generateRefreshToken(User user) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

    return Jwts.builder()
        .subject(user.getUsername())
        .claim("userId", user.getId())
        .claim("type", "refresh")
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(getSigningKey(), Jwts.SIG.HS512)
        .compact();
  }

  /**
   * Get username from JWT token.
   * 
   * @param token JWT token
   * @return username
   */
  public String getUsernameFromToken(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();

    return claims.getSubject();
  }

  /**
   * Get user ID from JWT token.
   * 
   * @param token JWT token
   * @return user ID
   */
  public Long getUserIdFromToken(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();

    return claims.get("userId", Long.class);
  }

  /**
   * Get user roles from JWT token.
   * 
   * @param token JWT token
   * @return comma-separated roles string
   */
  public String getRolesFromToken(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();

    return claims.get("roles", String.class);
  }

  /**
   * Get user email from JWT token.
   * 
   * @param token JWT token
   * @return user email
   */
  public String getEmailFromToken(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();

    return claims.get("email", String.class);
  }

  /**
   * Get token expiration date.
   * 
   * @param token JWT token
   * @return expiration date
   */
  public Date getExpirationDateFromToken(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();

    return claims.getExpiration();
  }

  /**
   * Check if token is expired.
   * 
   * @param token JWT token
   * @return true if expired, false otherwise
   */
  public boolean isTokenExpired(String token) {
    try {
      Date expiration = getExpirationDateFromToken(token);
      return expiration.before(new Date());
    } catch (Exception e) {
      logger.error("Error checking token expiration: {}", e.getMessage());
      return true;
    }
  }

  /**
   * Validate JWT token.
   * 
   * @param token JWT token
   * @return true if valid, false otherwise
   */
  public boolean validateToken(String token) {
    try {
      Jwts.parser()
          .verifyWith(getSigningKey())
          .build()
          .parseSignedClaims(token);
      return true;
    } catch (SecurityException ex) {
      logger.error("Invalid JWT signature: {}", ex.getMessage());
    } catch (MalformedJwtException ex) {
      logger.error("Invalid JWT token: {}", ex.getMessage());
    } catch (ExpiredJwtException ex) {
      logger.error("Expired JWT token: {}", ex.getMessage());
    } catch (UnsupportedJwtException ex) {
      logger.error("Unsupported JWT token: {}", ex.getMessage());
    } catch (IllegalArgumentException ex) {
      logger.error("JWT claims string is empty: {}", ex.getMessage());
    }
    return false;
  }

  /**
   * Check if token is a refresh token.
   * 
   * @param token JWT token
   * @return true if refresh token, false otherwise
   */
  public boolean isRefreshToken(String token) {
    try {
      Claims claims = Jwts.parser()
          .verifyWith(getSigningKey())
          .build()
          .parseSignedClaims(token)
          .getPayload();

      String type = claims.get("type", String.class);
      return "refresh".equals(type);
    } catch (Exception e) {
      logger.error("Error checking token type: {}", e.getMessage());
      return false;
    }
  }

  /**
   * Get signing key for JWT operations.
   * 
   * @return SecretKey for signing
   */
  private SecretKey getSigningKey() {
    byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  /**
   * Get access token expiration time in milliseconds.
   * 
   * @return expiration time
   */
  public long getAccessTokenExpiration() {
    return accessTokenExpiration;
  }

  /**
   * Get refresh token expiration time in milliseconds.
   * 
   * @return expiration time
   */
  public long getRefreshTokenExpiration() {
    return refreshTokenExpiration;
  }
}