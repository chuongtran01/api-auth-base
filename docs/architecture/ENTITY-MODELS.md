# Entity Models Documentation

## 🎯 **Overview**

This document describes all the JPA entity models used in the authentication system. These entities map to database tables and provide the foundation for user management, role-based access control, and session tracking.

## 📊 **Entity Relationships**

```
User (1) ←→ (N) Role (N) ←→ (N) Permission
User (1) ←→ (N) RefreshToken
User (1) ←→ (N) UserSession (optional)
```

## 🏗️ **Entity Models**

### **1. User Entity**

**Purpose**: Core user account entity for authentication and user management.

**Table**: `users`

**Key Features**:

- ✅ **Email-based authentication** (email is the unique identifier)
- ✅ **Optional username** for display purposes
- ✅ Profile information (first name, last name)
- ✅ Account status tracking (enabled, email verified)
- ✅ Timestamp tracking (created, updated, last login)
- ✅ Many-to-many relationship with roles
- ✅ One-to-many relationship with refresh tokens

**Fields**:

```java
@Entity
@Table(name = "users")
public class User {
    private Long id;                    // Primary key
    private String email;               // Unique email address (required)
    private String username;            // Unique username (optional)
    private String password;            // Encrypted password
    private String firstName;           // First name
    private String lastName;            // Last name
    private Boolean isEnabled;          // Account enabled status
    private Boolean isEmailVerified;    // Email verification status
    private LocalDateTime createdAt;    // Account creation time
    private LocalDateTime updatedAt;    // Last update time
    private LocalDateTime lastLoginAt;  // Last login time
    private Set<Role> roles;            // User roles
    private Set<RefreshToken> refreshTokens; // Refresh tokens
}
```

**Validation**:

- **Email**: Must be valid format and unique (required)
- **Username**: Must be 3-100 characters and unique (optional)
- **Password**: Required
- **First/Last name**: Max 100 characters each (optional)

**Authentication Strategy**:

- **Primary Identifier**: Email address
- **Login**: Users authenticate using email and password
- **Username**: Optional display name for user-friendly identification
- **Fallback**: If username is not provided, email is used for display purposes

**Business Methods**:

```java
// Registration with email and password (username optional)
User user = new User("user@example.com", "password", "optionalUsername", "John", "Doe");

// Registration with email and password only
User user = new User("user@example.com", "password");

// Get display name (prioritizes: firstName+lastName > firstName > lastName > username > email)
String displayName = user.getFullName();
```

**Constructor Examples**:

```java
// Minimal registration (email + password only)
User user1 = new User("john@example.com", "password123");

// Full registration with optional username
User user2 = new User("jane@example.com", "password123", "jane_doe", "Jane", "Doe");

// Registration without username
User user3 = new User("admin@company.com", "password123", null, "Admin", "User");
```

### **2. Role Entity**

**Purpose**: Defines user roles and their associated permissions.

**Table**: `roles`

**Key Features**:

- ✅ Role name and description
- ✅ Many-to-many relationship with permissions
- ✅ Many-to-many relationship with users
- ✅ Timestamp tracking

**Fields**:

```java
@Entity
@Table(name = "roles")
public class Role {
    private Long id;                    // Primary key
    private String name;                // Unique role name
    private String description;         // Role description
    private LocalDateTime createdAt;    // Creation time
    private LocalDateTime updatedAt;    // Last update time
    private Set<Permission> permissions; // Role permissions
    private Set<User> users;            // Users with this role
}
```

**Validation**:

- Role name is required and unique (max 50 characters)
- Description max 255 characters

**Helper Methods**:

```java
public void addPermission(Permission permission)    // Add permission to role
public void removePermission(Permission permission) // Remove permission from role
public void addUser(User user)                      // Assign role to user
public void removeUser(User user)                   // Remove role from user
```

### **3. Permission Entity**

**Purpose**: Defines system permissions that can be assigned to roles.

**Table**: `permissions`

**Key Features**:

- ✅ Permission name and description
- ✅ Many-to-many relationship with roles
- ✅ Timestamp tracking

**Fields**:

```java
@Entity
@Table(name = "permissions")
public class Permission {
    private Long id;                    // Primary key
    private String name;                // Unique permission name
    private String description;         // Permission description
    private LocalDateTime createdAt;    // Creation time
    private LocalDateTime updatedAt;    // Last update time
    private Set<Role> roles;            // Roles with this permission
}
```

**Validation**:

- Permission name is required and unique (max 100 characters)
- Description max 255 characters

**Helper Methods**:

```java
public void addRole(Role role)          // Add role to permission
public void removeRole(Role role)       // Remove role from permission
```

### **4. RefreshToken Entity**

**Purpose**: Stores JWT refresh tokens for token renewal without re-authentication.

**Table**: `refresh_tokens`

**Key Features**:

- ✅ Unique token storage
- ✅ User association
- ✅ Expiry date tracking
- ✅ Creation timestamp

**Fields**:

```java
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    private Long id;                    // Primary key
    private String token;               // Unique refresh token
    private User user;                  // Associated user
    private LocalDateTime expiryDate;   // Token expiry time
    private LocalDateTime createdAt;    // Creation time
}
```

**Validation**:

- Token is required and unique (max 500 characters)
- User is required
- Expiry date is required

**Business Methods**:

```java
public boolean isExpired()              // Check if token is expired
public boolean isValid()                // Check if token is valid
```

### **5. UserSession Entity (Optional)**

**Purpose**: Tracks user sessions for security auditing and session management.

**Table**: `user_sessions`

**Key Features**:

- ✅ Session ID tracking
- ✅ IP address and user agent logging
- ✅ Session status (active/inactive)
- ✅ Activity and expiry tracking

**Fields**:

```java
@Entity
@Table(name = "user_sessions")
public class UserSession {
    private Long id;                    // Primary key
    private User user;                  // Associated user
    private String sessionId;           // Unique session ID
    private String ipAddress;           // Client IP address
    private String userAgent;           // Client user agent
    private Boolean isActive;           // Session active status
    private LocalDateTime createdAt;    // Session creation time
    private LocalDateTime lastActivityAt; // Last activity time
    private LocalDateTime expiresAt;    // Session expiry time
}
```

**Validation**:

- Session ID is required and unique (max 255 characters)
- User is required
- IP address max 45 characters (IPv6 support)
- User agent max 500 characters

**Business Methods**:

```java
public boolean isExpired()              // Check if session is expired
public boolean isValid()                // Check if session is valid
public void updateLastActivity()        // Update last activity time
public void deactivate()                // Deactivate session
```

## 🔗 **JPA Relationships**

### **Many-to-Many Relationships**

#### **User ↔ Role**

```java
// User side
@ManyToMany(fetch = FetchType.EAGER)
@JoinTable(
    name = "user_roles",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id")
)
private Set<Role> roles = new HashSet<>();

// Role side
@ManyToMany(mappedBy = "roles")
private Set<User> users = new HashSet<>();
```

#### **Role ↔ Permission**

```java
// Role side
@ManyToMany(fetch = FetchType.EAGER)
@JoinTable(
    name = "role_permissions",
    joinColumns = @JoinColumn(name = "role_id"),
    inverseJoinColumns = @JoinColumn(name = "permission_id")
)
private Set<Permission> permissions = new HashSet<>();

// Permission side
@ManyToMany(mappedBy = "permissions")
private Set<Role> roles = new HashSet<>();
```

### **One-to-Many Relationships**

#### **User → RefreshToken**

```java
// User side
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
private Set<RefreshToken> refreshTokens = new HashSet<>();

// RefreshToken side
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;
```

#### **User → UserSession**

```java
// User side (can be added if needed)
@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
private Set<UserSession> sessions = new HashSet<>();

// UserSession side
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;
```

## ⏰ **Auditing Support**

All entities support automatic timestamp management using Spring Data JPA auditing:

```java
@CreatedDate
@Column(name = "created_at", nullable = false, updatable = false)
private LocalDateTime createdAt;

@LastModifiedDate
@Column(name = "updated_at", nullable = false)
private LocalDateTime updatedAt;
```

**Configuration**:

```java
@SpringBootApplication
@EnableJpaAuditing
public class ApiAuthBaseApplication {
    // Application configuration
}
```

## 🔒 **Security Considerations**

### **Password Security**

- Passwords are stored as encrypted hashes (BCrypt)
- Never expose passwords in logs or responses
- Use `@JsonIgnore` on password fields in DTOs

### **Token Security**

- Refresh tokens are stored securely in database
- Tokens have explicit expiry dates
- Implement token rotation for security

### **Session Security**

- Track IP addresses and user agents
- Implement session timeout
- Support session invalidation

## 🧪 **Testing Entities**

### **Unit Testing**

```java
@Test
void testUserCreation() {
    User user = new User("test@example.com", "testuser", "password");
    user.setFirstName("John");
    user.setLastName("Doe");

    assertEquals("test@example.com", user.getEmail());
    assertEquals("John Doe", user.getFullName());
    assertTrue(user.isEnabled());
}

@Test
void testRolePermissionAssignment() {
    Role adminRole = new Role("ADMIN", "Administrator");
    Permission userRead = new Permission("USER_READ", "Read user information");

    adminRole.addPermission(userRead);

    assertTrue(adminRole.getPermissions().contains(userRead));
    assertTrue(userRead.getRoles().contains(adminRole));
}
```

### **Integration Testing**

```java
@SpringBootTest
@Transactional
class UserEntityIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testUserPersistence() {
        User user = new User("test@example.com", "testuser", "password");
        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertEquals("test@example.com", savedUser.getEmail());
    }
}
```

## 📚 **Best Practices**

### **1. Entity Design**

- Use meaningful field names
- Include proper validation annotations
- Implement proper equals/hashCode methods
- Use appropriate fetch types (LAZY vs EAGER)

### **2. Relationship Management**

- Use helper methods for bidirectional relationships
- Implement proper cascade operations
- Consider performance implications of fetch types

### **3. Validation**

- Use Bean Validation annotations
- Provide meaningful error messages
- Validate business rules in entities

### **4. Auditing**

- Enable JPA auditing for timestamp management
- Use `@CreatedDate` and `@LastModifiedDate`
- Consider custom auditing for user tracking

## 🎯 **Usage Examples**

### **Creating a User with Roles**

```java
// Create user
User user = new User("john@example.com", "john", "password");
user.setFirstName("John");
user.setLastName("Doe");

// Create and assign role
Role userRole = new Role("USER", "Standard user");
user.addRole(userRole);

// Save to database
userRepository.save(user);
```

### **Checking User Permissions**

```java
public boolean hasPermission(User user, String permissionName) {
    return user.getRoles().stream()
        .flatMap(role -> role.getPermissions().stream())
        .anyMatch(permission -> permission.getName().equals(permissionName));
}
```

### **Managing Refresh Tokens**

```java
public RefreshToken createRefreshToken(User user, String token, Duration validity) {
    RefreshToken refreshToken = new RefreshToken(
        token,
        user,
        LocalDateTime.now().plus(validity)
    );
    user.addRefreshToken(refreshToken);
    return refreshTokenRepository.save(refreshToken);
}
```

This entity model design provides a solid foundation for a comprehensive authentication and authorization system with proper separation of concerns and extensibility.
