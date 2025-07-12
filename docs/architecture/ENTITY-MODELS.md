# Entity Models

This document describes the core entity models used in the authentication system.

## Entity Relationships

```
User (1) ←→ (N) Role (M) ←→ (N) Permission
User (1) ←→ (N) RefreshToken
```

## Core Entities

### **1. User Entity**

**Purpose**: Represents user accounts in the system.

**Table**: `users`

**Key Features**:

- Email-based authentication (primary identifier)
- Optional username support
- Account lockout functionality
- Email verification status
- Multiple roles support
- Refresh token management

**Entity Class**:

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @NotBlank
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "username", nullable = true, unique = true)
    private String username;

    @NotBlank
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean isEnabled = true;

    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private Boolean isEmailVerified = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // Account lockout fields
    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    @Column(name = "last_failed_login_at")
    private LocalDateTime lastFailedLoginAt;

    // Relationships
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RefreshToken> refreshTokens = new HashSet<>();
}
```

**Business Logic Methods**:

- `addRole(Role role)` - Add role to user
- `removeRole(Role role)` - Remove role from user
- `incrementFailedLoginAttempts()` - Track failed login attempts
- `resetFailedLoginAttempts()` - Reset lockout counter
- `lockAccount(LocalDateTime lockUntil)` - Lock account
- `isAccountLocked()` - Check if account is locked
- `isAccountLockedExpired()` - Check if lockout period expired

### **2. Role Entity**

**Purpose**: Defines user roles and their associated permissions.

**Table**: `roles`

**Key Features**:

- Role-based access control (RBAC)
- Multiple permissions per role
- Hierarchical role structure support

**Entity Class**:

```java
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    private Set<User> users = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "role_permissions")
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();
}
```

**Business Logic Methods**:

- `addPermission(Permission permission)` - Add permission to role
- `removePermission(Permission permission)` - Remove permission from role
- `hasPermission(String permissionName)` - Check if role has permission

### **3. Permission Entity**

**Purpose**: Defines granular permissions for fine-grained access control.

**Table**: `permissions`

**Key Features**:

- Granular permission system
- Reusable across multiple roles
- Permission-based access control

**Entity Class**:

```java
@Entity
@Table(name = "permissions")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToMany(mappedBy = "permissions")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
```

**Business Logic Methods**:

- `addRole(Role role)` - Add role to permission
- `removeRole(Role role)` - Remove role from permission

### **4. RefreshToken Entity**

**Purpose**: Manages refresh tokens for JWT token renewal.

**Table**: `refresh_tokens`

**Key Features**:

- Secure token storage
- Automatic expiration
- User association
- Token rotation support

**Entity Class**:

```java
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
}
```

**Business Logic Methods**:

- `isExpired()` - Check if token is expired
- `updateLastUsed()` - Update last used timestamp
- `extendExpiry(LocalDateTime newExpiry)` - Extend token expiration

## Relationship Details

### **User → Role (Many-to-Many)**

**Junction Table**: `user_roles`

```java
// User side
@ManyToMany(fetch = FetchType.EAGER)
@JoinTable(name = "user_roles")
private Set<Role> roles = new HashSet<>();

// Role side
@ManyToMany(mappedBy = "roles")
private Set<User> users = new HashSet<>();
```

**Usage**:

```java
user.addRole(adminRole);
user.removeRole(userRole);
```

### **Role → Permission (Many-to-Many)**

**Junction Table**: `role_permissions`

```java
// Role side
@ManyToMany(fetch = FetchType.EAGER)
@JoinTable(name = "role_permissions")
private Set<Permission> permissions = new HashSet<>();

// Permission side
@ManyToMany(mappedBy = "permissions")
private Set<Role> roles = new HashSet<>();
```

**Usage**:

```java
adminRole.addPermission(readPermission);
adminRole.removePermission(writePermission);
```

### **User → RefreshToken (One-to-Many)**

```java
// User side
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
private Set<RefreshToken> refreshTokens = new HashSet<>();

// RefreshToken side
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;
```

**Usage**:

```java
user.addRefreshToken(refreshToken);
user.removeRefreshToken(refreshToken);
```

## Database Schema

### **Users Table**

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(100) UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    account_locked_until TIMESTAMP,
    last_failed_login_at TIMESTAMP
);
```

### **Roles Table**

```sql
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### **Permissions Table**

```sql
CREATE TABLE permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### **Refresh Tokens Table**

```sql
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### **Junction Tables**

**User Roles**:

```sql
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);
```

**Role Permissions**:

```sql
CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);
```

## Best Practices

### **Entity Design**

- Use `@Builder` pattern for flexible object creation
- Implement proper `equals()` and `hashCode()` methods
- Use `@ToString(exclude = "sensitiveFields")` for security
- Implement business logic methods in entities

### **Relationship Management**

- Use bidirectional relationships for consistency
- Implement helper methods for relationship management
- Use appropriate fetch types (LAZY vs EAGER)
- Consider cascade operations carefully

### **Performance Considerations**

- Use indexes on frequently queried fields
- Consider pagination for large datasets
- Use appropriate fetch strategies
- Monitor N+1 query problems

### **Security Considerations**

- Never expose sensitive fields in toString()
- Use proper validation annotations
- Implement proper access control
- Consider audit trails for sensitive operations
