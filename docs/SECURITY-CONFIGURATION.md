# Security Configuration Documentation

## 🎯 **Overview**

This document describes the complete security configuration for the JWT authentication system. The security infrastructure is built on Spring Security with custom JWT components for token-based authentication and role-based authorization.

## 🏗️ **Security Architecture**

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   HTTP Request  │───▶│ JWT Auth Filter  │───▶│ Security Context│
└─────────────────┘    └──────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌──────────────────┐
                       │ JWT Token Provider│
                       └──────────────────┘
                              │
                              ▼
                       ┌──────────────────┐
                       │ UserDetailsService│
                       └──────────────────┘
                              │
                              ▼
                       ┌──────────────────┐
                       │   Database       │
                       └──────────────────┘
```

## 🔧 **Core Components**

### **1. SecurityConfig**

**Purpose**: Main security configuration class that sets up the entire security infrastructure.

**Key Features**:

- ✅ JWT-based authentication
- ✅ Role-based authorization
- ✅ CORS configuration
- ✅ CSRF disabled (stateless JWT)
- ✅ Stateless session management
- ✅ Custom authentication provider
- ✅ BCrypt password encoding

**Configuration**:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)  // Disable CSRF for JWT
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll()           // Public auth endpoints
                .requestMatchers("/api/swagger-ui/**").permitAll()     // Swagger UI
                .requestMatchers("/api/health/**").permitAll()         // Health checks
                .requestMatchers("/api/admin/**").hasRole("ADMIN")     // Admin only
                .requestMatchers("/api/users/**").authenticated()      // Authenticated users
                .anyRequest().authenticated())                         // All others need auth
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler));

        return http.build();
    }
}
```

**Authorization Rules**:

| Endpoint Pattern     | Access Level  | Description                                      |
| -------------------- | ------------- | ------------------------------------------------ |
| `/api/auth/**`       | Public        | Authentication endpoints (login, register, etc.) |
| `/api/swagger-ui/**` | Public        | API documentation                                |
| `/api/health/**`     | Public        | Health check endpoints                           |
| `/api/admin/**`      | ADMIN role    | Administrative functions                         |
| `/api/users/**`      | Authenticated | User management (own profile)                    |
| All others           | Authenticated | Default for all other endpoints                  |

### **2. JwtTokenProvider**

**Purpose**: Handles JWT token generation, validation, and claim extraction.

**Key Features**:

- ✅ Generate access tokens with user information
- ✅ Generate refresh tokens for token renewal
- ✅ Validate token signatures and expiration
- ✅ Extract user claims (username, roles, etc.)
- ✅ Support for different token types (access vs refresh)

**Token Structure**:

```json
{
  "sub": "username",
  "userId": 123,
  "email": "user@example.com",
  "roles": "USER,ADMIN",
  "iat": 1640995200,
  "exp": 1640998800
}
```

**Usage Examples**:

```java
// Generate access token
String accessToken = tokenProvider.generateAccessToken(authentication);

// Generate refresh token
String refreshToken = tokenProvider.generateRefreshToken(user);

// Validate token
boolean isValid = tokenProvider.validateToken(token);

// Extract user information
String username = tokenProvider.getUsernameFromToken(token);
Long userId = tokenProvider.getUserIdFromToken(token);
String roles = tokenProvider.getRolesFromToken(token);
```

### **3. JwtAuthenticationFilter**

**Purpose**: Intercepts HTTP requests, extracts JWT tokens, and sets up authentication.

**Key Features**:

- ✅ Extract JWT from Authorization header
- ✅ Validate tokens using JwtTokenProvider
- ✅ Load user details from database
- ✅ Set up Spring Security context
- ✅ Skip filtering for public endpoints

**Request Flow**:

1. **Extract Token**: Get JWT from `Authorization: Bearer <token>` header
2. **Validate Token**: Check signature, expiration, and format
3. **Load User**: Fetch user details from database
4. **Create Authorities**: Convert roles to Spring Security authorities
5. **Set Authentication**: Establish security context for the request

**Filter Configuration**:

```java
@Override
protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();

    // Skip filtering for public endpoints
    return path.startsWith("/api/auth/") ||
           path.startsWith("/api/swagger-ui") ||
           path.startsWith("/api/api-docs") ||
           path.equals("/api/swagger-ui.html") ||
           path.equals("/api/api-docs");
}
```

### **4. CustomUserDetailsService**

**Purpose**: Loads user details from database and converts them to Spring Security UserDetails.

**Key Features**:

- ✅ Load users by username or email
- ✅ Convert User entities to UserDetails
- ✅ Map user roles to Spring Security authorities
- ✅ Handle account status (enabled, locked, etc.)

**UserDetails Creation**:

```java
private UserDetails createUserDetails(User user) {
    // Convert user roles to Spring Security authorities
    List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
            .collect(Collectors.toList());

    return User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .authorities(authorities)
            .accountExpired(!user.isAccountNonExpired())
            .accountLocked(!user.isAccountNonLocked())
            .credentialsExpired(!user.isCredentialsNonExpired())
            .disabled(!user.isEnabled())
            .build();
}
```

### **5. JwtAuthenticationEntryPoint**

**Purpose**: Handles unauthorized requests (no valid JWT token).

**Response Format**:

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Access denied. Please provide valid authentication credentials.",
  "path": "/api/users/profile",
  "timestamp": "2024-01-01T12:00:00"
}
```

### **6. JwtAccessDeniedHandler**

**Purpose**: Handles forbidden requests (valid token but insufficient permissions).

**Response Format**:

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. You don't have permission to access this resource.",
  "path": "/api/admin/users",
  "timestamp": "2024-01-01T12:00:00"
}
```

## 🔐 **Authentication Flow**

### **1. Login Process**

```
1. User sends credentials to /api/auth/login
2. Spring Security validates credentials
3. JwtTokenProvider generates access and refresh tokens
4. Tokens returned to client
5. Client stores tokens for future requests
```

### **2. Request Authentication**

```
1. Client sends request with Authorization header
2. JwtAuthenticationFilter intercepts request
3. Filter extracts and validates JWT token
4. UserDetailsService loads user from database
5. Security context established with user authorities
6. Request proceeds to controller
```

### **3. Authorization Check**

```
1. Controller method has @PreAuthorize annotation
2. Spring Security checks user authorities
3. If authorized: request proceeds
4. If not authorized: AccessDeniedHandler returns 403
```

## 🛡️ **Security Features**

### **1. Token Security**

- **HS512 Algorithm**: Strong cryptographic signing
- **Configurable Expiration**: Separate expiration for access and refresh tokens
- **Stateless**: No server-side session storage
- **Secure Headers**: Tokens transmitted via Authorization header

### **2. Password Security**

- **BCrypt Encoding**: Industry-standard password hashing
- **Configurable Strength**: Adjustable BCrypt rounds
- **Salt Generation**: Automatic salt generation for each password

### **3. CORS Configuration**

- **Flexible Origins**: Configurable allowed origins per environment
- **Secure Headers**: Proper header configuration
- **Credentials Support**: Support for cookies and authorization headers

### **4. Role-Based Access Control**

- **Multiple Roles**: Users can have multiple roles
- **Permission Inheritance**: Permissions combined from all roles
- **Method-Level Security**: @PreAuthorize annotations for fine-grained control

## 📊 **Repository Layer**

### **1. UserRepository**

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsernameOrEmail(String usernameOrEmail);
    List<User> findEnabledUsers();
    List<User> findByRoleName(String roleName);
}
```

### **2. RoleRepository**

```java
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
}
```

### **3. PermissionRepository**

```java
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
    boolean existsByName(String name);
}
```

### **4. RefreshTokenRepository**

```java
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findByUser(User user);
    List<RefreshToken> findValidTokensByUser(User user, LocalDateTime now);
    List<RefreshToken> findExpiredTokens(LocalDateTime now);
    void deleteByUser(User user);
    void deleteExpiredTokens(LocalDateTime now);
}
```

## 🧪 **Testing Security**

### **1. Health Check Endpoints**

```bash
# Test public health endpoint
curl http://localhost:8080/api/health

# Test detailed health endpoint
curl http://localhost:8080/api/health/details
```

### **2. Authentication Testing**

```bash
# Test unauthorized access (should return 401)
curl http://localhost:8080/api/users/profile

# Test with invalid token (should return 401)
curl -H "Authorization: Bearer invalid-token" http://localhost:8080/api/users/profile

# Test with valid token (should return 200)
curl -H "Authorization: Bearer valid-jwt-token" http://localhost:8080/api/users/profile
```

### **3. Authorization Testing**

```bash
# Test admin endpoint without admin role (should return 403)
curl -H "Authorization: Bearer user-token" http://localhost:8080/api/admin/users

# Test admin endpoint with admin role (should return 200)
curl -H "Authorization: Bearer admin-token" http://localhost:8080/api/admin/users
```

## 🔧 **Configuration Properties**

### **1. JWT Configuration**

```yaml
jwt:
  secret: your-secret-key-here-make-it-very-long-and-secure-in-production
  access-token-expiration: 900000 # 15 minutes
  refresh-token-expiration: 604800000 # 7 days
```

### **2. CORS Configuration**

```yaml
cors:
  allowed-origin-patterns:
    - "http://localhost:3000"
    - "https://*.example.com"
  allowed-methods:
    - "GET"
    - "POST"
    - "PUT"
    - "DELETE"
    - "OPTIONS"
  allowed-headers:
    - "Origin"
    - "Content-Type"
    - "Accept"
    - "Authorization"
  allow-credentials: true
  max-age: 3600
```

## 🚀 **Best Practices**

### **1. Token Management**

- **Short Access Tokens**: 15-30 minutes for security
- **Long Refresh Tokens**: 7-30 days for convenience
- **Token Rotation**: Implement refresh token rotation
- **Secure Storage**: Store tokens securely on client side

### **2. Security Headers**

- **HTTPS Only**: Use HTTPS in production
- **Secure Headers**: Implement security headers (HSTS, CSP, etc.)
- **Token Validation**: Always validate tokens on server side

### **3. Error Handling**

- **Generic Messages**: Don't leak sensitive information in error messages
- **Proper Status Codes**: Use appropriate HTTP status codes
- **Logging**: Log security events for monitoring

### **4. Performance**

- **Token Caching**: Cache validated tokens for performance
- **Database Optimization**: Use proper indexes for user queries
- **Connection Pooling**: Configure database connection pooling

This security configuration provides a robust, scalable, and secure foundation for JWT-based authentication and authorization in Spring Boot applications.
