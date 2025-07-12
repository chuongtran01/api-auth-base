# Redis Integration for JWT Token Blacklisting

This document describes the Redis integration for JWT token blacklisting in the authentication system.

## Overview

Redis is used to implement secure logout functionality by blacklisting JWT access tokens. When a user logs out, their access token is added to a Redis blacklist with an expiration time matching the token's original expiration.

## Architecture

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │    │   Spring    │    │    Redis    │
│             │    │   Boot      │    │             │
└─────────────┘    └─────────────┘    └─────────────┘
       │                   │                   │
       │ 1. Login          │                   │
       │──────────────────▶│                   │
       │                   │                   │
       │ 2. Return JWT     │                   │
       │◀──────────────────│                   │
       │                   │                   │
       │ 3. API Request    │                   │
       │──────────────────▶│                   │
       │                   │ 4. Check          │
       │                   │ blacklist         │
       │                   │──────────────────▶│
       │                   │◀──────────────────│
       │                   │                   │
       │ 5. Logout         │                   │
       │──────────────────▶│                   │
       │                   │ 6. Blacklist      │
       │                   │ token             │
       │                   │──────────────────▶│
       │                   │                   │
       │ 7. API Request    │                   │
       │ (with blacklisted │                   │
       │  token)           │                   │
       │──────────────────▶│                   │
       │                   │ 8. Check          │
       │                   │ blacklist         │
       │                   │──────────────────▶│
       │                   │◀──────────────────│
       │ 9. 401 Unauthorized│                   │
       │◀──────────────────│                   │
```

## Dependencies

Add Redis dependencies to `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-redis</artifactId>
</dependency>
```

## Configuration

### Application Properties

```yaml
# Redis Configuration
spring:
  redis:
    host: localhost
    port: 6379
    password: # optional - set in production
    database: 0
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms

# Redis Logout Configuration
auth:
  redis:
    enabled: true
    token-blacklist-prefix: "blacklist:"

  logout:
    blacklist-access-tokens: true
    remove-refresh-tokens: true
    log-events: true
```

### Redis Configuration Class

```java
@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${auth.redis.enabled:true}")
    private boolean redisEnabled;

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Configure serializers
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }

    public boolean isRedisEnabled() {
        return redisEnabled;
    }
}
```

## Service Layer

### RedisTokenService Interface

```java
public interface RedisTokenService {

    /**
     * Blacklist a JWT token to prevent its use after logout.
     */
    void blacklistToken(String token, long expirationTime);

    /**
     * Check if a JWT token is blacklisted.
     */
    boolean isTokenBlacklisted(String token);

    /**
     * Clean up expired tokens from blacklist.
     */
    long cleanupExpiredTokens();

    /**
     * Check if Redis is available and enabled.
     */
    boolean isRedisAvailable();
}
```

### Implementation

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class RedisTokenServiceImpl implements RedisTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisConfig redisConfig;

    @Value("${auth.redis.token-blacklist-prefix:blacklist:}")
    private String tokenBlacklistPrefix;

    @Override
    public void blacklistToken(String token, long expirationTime) {
        if (!redisConfig.isRedisEnabled()) {
            log.debug("Redis is disabled, skipping token blacklisting");
            return;
        }

        try {
            String key = tokenBlacklistPrefix + token;
            long ttl = calculateTTL(expirationTime);

            redisTemplate.opsForValue().set(key, "blacklisted", ttl, TimeUnit.SECONDS);
            log.debug("Token blacklisted with TTL: {} seconds", ttl);
        } catch (Exception e) {
            log.error("Failed to blacklist token: {}", e.getMessage());
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        if (!redisConfig.isRedisEnabled()) {
            log.debug("Redis is disabled, token not blacklisted");
            return false;
        }

        try {
            String key = tokenBlacklistPrefix + token;
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Failed to check token blacklist: {}", e.getMessage());
            return false;
        }
    }

    private long calculateTTL(long expirationTime) {
        long currentTime = System.currentTimeMillis();
        long timeUntilExpiration = expirationTime - currentTime;
        return Math.max(300, timeUntilExpiration / 1000);
    }
}
```

## Integration with Authentication

### JWT Filter Integration

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final RedisTokenService redisTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) {

        String jwt = getJwtFromRequest(request);

        if (StringUtils.hasText(jwt)) {
            // Check if token is blacklisted first
            if (redisTokenService.isTokenBlacklisted(jwt)) {
                log.warn("Request with blacklisted token rejected: {}", request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token has been invalidated");
                return;
            }

            // Continue with normal JWT validation...
        }

        filterChain.doFilter(request, response);
    }
}
```

### Logout Integration

```java
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    @Override
    public boolean logout(String refreshTokenString, String accessToken) {
        log.info("Logging out user with refresh token and blacklisting access token");

        // Blacklist the access token
        if (accessToken != null && !accessToken.trim().isEmpty()) {
            try {
                long expirationTime = jwtTokenProvider.getExpirationDateFromToken(accessToken).getTime();
                redisTokenService.blacklistToken(accessToken, expirationTime);
                log.info("Access token blacklisted successfully");
            } catch (Exception e) {
                log.warn("Failed to blacklist access token: {}", e.getMessage());
            }
        }

        // Delete refresh token from database
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(refreshTokenString);
        if (refreshTokenOpt.isPresent()) {
            refreshTokenRepository.delete(refreshTokenOpt.get());
            log.info("User logged out successfully");
            return true;
        }

        return false;
    }
}
```

## Redis Key Structure

### Token Blacklist

```
blacklist:{jwt_token} -> "blacklisted"
```

Example:

```
blacklist:eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... -> "blacklisted"
```

## Usage Examples

### Basic Logout

```java
@PostMapping("/logout")
public ResponseEntity<?> logout(@RequestBody LogoutRequest request) {
    boolean success = authenticationService.logout(
        request.refreshToken(),
        request.accessToken()
    );

    if (success) {
        return ResponseEntity.ok(new SuccessResponse("Logout successful"));
    } else {
        return ResponseEntity.badRequest().body(new ErrorResponse("Logout failed"));
    }
}
```

### Admin Force Logout

```java
@PostMapping("/admin/users/{userId}/logout")
@RequirePermission("ADMIN")
public ResponseEntity<?> forceLogoutUser(@PathVariable Long userId) {
    // Delete all refresh tokens for the user
    refreshTokenRepository.deleteByUserId(userId);

    return ResponseEntity.ok(new SuccessResponse("User logged out from all devices"));
}
```

## Testing

### Test Configuration

```java
@TestConfiguration
public class TestRedisConfig {

    @Bean
    public RedisTokenService redisTokenService() {
        return new RedisTokenService() {
            @Override
            public void blacklistToken(String token, long expirationTime) {
                // No-op for tests
            }

            @Override
            public boolean isTokenBlacklisted(String token) {
                return false; // No tokens blacklisted in tests
            }

            @Override
            public long cleanupExpiredTokens() {
                return 0;
            }

            @Override
            public boolean isRedisAvailable() {
                return false;
            }
        };
    }
}
```

### Integration Tests

```java
@SpringBootTest
@AutoConfigureTestDatabase
class AuthenticationServiceIntegrationTest {

    @Test
    void testLogoutWithTokenBlacklisting() {
        // Given: User logs in and gets tokens
        AuthenticationResult result = authenticationService.authenticate("user@example.com", "password");

        // When: User logs out
        boolean logoutSuccess = authenticationService.logout(
            result.refreshToken(),
            result.accessToken()
        );

        // Then: Logout should succeed
        assertTrue(logoutSuccess);

        // And: Token should be blacklisted
        assertTrue(redisTokenService.isTokenBlacklisted(result.accessToken()));
    }
}
```

## Health Monitoring

### Redis Health Check

```java
@Component
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisTokenService redisTokenService;

    @Override
    public Health health() {
        if (redisTokenService.isRedisAvailable()) {
            return Health.up()
                .withDetail("service", "Redis Token Blacklisting")
                .withDetail("status", "Available")
                .build();
        } else {
            return Health.down()
                .withDetail("service", "Redis Token Blacklisting")
                .withDetail("status", "Unavailable")
                .build();
        }
    }
}
```

## Security Considerations

### Token Expiration

- Blacklisted tokens are automatically removed when they would have expired
- TTL is calculated based on the original token expiration time
- Buffer time (5 minutes) is added to ensure cleanup

### Redis Security

- Use Redis authentication in production
- Configure Redis to bind only to localhost or private network
- Use SSL/TLS for Redis connections in production
- Regularly rotate Redis passwords

### Fallback Strategy

- If Redis is unavailable, the system falls back to token-only validation
- This ensures the application remains functional even if Redis is down
- Log warnings when Redis operations fail

## Performance Optimization

### Connection Pooling

- Configure appropriate connection pool settings
- Monitor Redis connection usage
- Use connection pooling for high-traffic applications

### Key Expiration

- Redis automatically handles key expiration
- No manual cleanup is required
- Memory usage is automatically managed

## Error Handling

### Redis Connection Failures

```java
try {
    redisTokenService.blacklistToken(token, expirationTime);
} catch (Exception e) {
    log.warn("Failed to blacklist token: {}", e.getMessage());
    // Continue with logout even if blacklisting fails
}
```

### Graceful Degradation

- If Redis is unavailable, tokens are not blacklisted
- Application continues to function with reduced security
- Alerts should be configured for Redis failures

## Migration Strategy

### From No Redis

1. Add Redis dependencies
2. Configure Redis connection
3. Implement RedisTokenService
4. Update authentication filter
5. Update logout logic
6. Test thoroughly

### From Session-Based

1. Remove session storage logic
2. Keep only blacklisting functionality
3. Update configuration
4. Remove session-related endpoints
5. Update documentation

## Benefits

### Security

- **Immediate Logout**: Tokens are invalidated instantly
- **No Token Reuse**: Blacklisted tokens cannot be used
- **Automatic Cleanup**: Expired tokens are automatically removed

### Performance

- **Fast Lookups**: Redis provides O(1) token lookups
- **Minimal Overhead**: Only one Redis operation per request
- **Automatic Expiration**: No manual cleanup required

### Scalability

- **Horizontal Scaling**: Redis can be clustered
- **High Availability**: Redis supports replication
- **Memory Efficient**: Automatic cleanup of expired tokens

## Next Steps

1. **Production Deployment**: Configure Redis for production environment
2. **Monitoring**: Set up Redis monitoring and alerting
3. **Backup Strategy**: Implement Redis backup and recovery
4. **Performance Tuning**: Optimize Redis configuration for your workload
5. **Security Hardening**: Implement additional security measures
