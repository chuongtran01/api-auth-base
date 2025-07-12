# Service Layer Implementation

This document describes the service layer implementation for the JWT authentication backend, covering all business logic, data access patterns, and service interactions.

## 📋 Overview

The service layer provides a clean separation between the presentation layer (controllers) and the data access layer (repositories). It encapsulates all business logic and provides a consistent interface for the application.

## 🏗️ Service Architecture

### Core Services

1. **UserService** - User management operations
2. **AuthenticationService** - Authentication and token management
3. **RoleService** - Role and permission management
4. **EmailService** - Email notifications (placeholder implementation)

### Service Dependencies

```
┌─────────────────┐    ┌─────────────────────┐    ┌─────────────────┐
│   Controllers   │───▶│   Service Layer     │───▶│   Repositories   │
└─────────────────┘    └─────────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │   Entities      │
                       └─────────────────┘
```

### Interface-Based Design

The service layer follows the **Interface Segregation Principle** and **Dependency Inversion Principle**:

```
┌─────────────────┐    ┌─────────────────────┐
│   Interface     │◄───│   Implementation    │
│   (Contract)    │    │   (Business Logic)  │
└─────────────────┘    └─────────────────────┘
```

**Benefits:**

- **Loose Coupling**: Controllers depend on interfaces, not implementations
- **Testability**: Easy to mock interfaces for unit testing
- **Flexibility**: Multiple implementations can be swapped
- **Maintainability**: Clear separation of concerns

## 🔧 UserService

### Interface: `UserService`

Defines the contract for user management operations.

### Implementation: `UserServiceImpl`

Handles all user-related business operations including registration, profile management, and account status updates.

### Key Features

- **User Registration**: Create new user accounts with email and password (username optional)
- **Profile Management**: Update user information and preferences
- **Password Management**: Secure password changes with current password verification
- **Account Status**: Enable/disable accounts and email verification
- **User Lookup**: Find users by various criteria (ID, email, username)

### Usage Examples

```java
@Service
public class SomeController {
    private final UserService userService; // Interface injection

    // Register a new user with email and password only
    public User registerUser(String email, String password) {
        return userService.registerUser(email, password, null, "John", "Doe");
    }

    // Register a new user with optional username
    public User registerUserWithUsername(String email, String password, String username) {
        return userService.registerUser(email, password, username, "John", "Doe");
    }

    // Update user profile
    public User updateProfile(Long userId, String firstName, String lastName) {
        return userService.updateProfile(userId, firstName, lastName);
    }

    // Change password
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        return userService.changePassword(userId, currentPassword, newPassword);
    }
}
```

### Transaction Management

- **Read Operations**: Marked with `@Transactional(readOnly = true)` for performance
- **Write Operations**: Full transaction support with automatic rollback on exceptions
- **Validation**: Comprehensive input validation and business rule enforcement

## 🔐 AuthenticationService

### Interface: `AuthenticationService`

Defines the contract for authentication operations.

### Implementation: `AuthenticationServiceImpl`

Manages the complete authentication flow including login, token generation, refresh, and logout operations.

### Key Features

- **User Authentication**: Validate credentials and generate JWT tokens
- **Token Management**: Generate and validate access and refresh tokens
- **Session Management**: Track active sessions and handle logout
- **Token Cleanup**: Automatic cleanup of expired refresh tokens

### Authentication Flow

```java
@Service
public class AuthController {
    private final AuthenticationService authService; // Interface injection

    // Login flow
    public AuthenticationResult login(String username, String password) {
        return authService.authenticate(username, password);
    }

    // Refresh token flow
    public AuthenticationResult refresh(String refreshToken) {
        return authService.refreshToken(refreshToken);
    }

    // Logout flow
    public boolean logout(String refreshToken) {
        return authService.logout(refreshToken);
    }
}
```

### Token Lifecycle

1. **Login**: User provides credentials → AuthenticationService validates → Returns access + refresh tokens
2. **API Calls**: Client uses access token for authenticated requests
3. **Token Refresh**: When access token expires → Client uses refresh token → New access token issued
4. **Logout**: Client provides refresh token → Token invalidated → Session terminated

### Security Features

- **BCrypt Password Hashing**: Secure password storage and verification
- **Token Expiration**: Configurable token lifetimes
- **Session Tracking**: Monitor active sessions per user
- **Automatic Cleanup**: Remove expired tokens to prevent database bloat

## 👥 RoleService

### Interface: `RoleService`

Defines the contract for role and permission management.

### Implementation: `RoleServiceImpl`

Manages role-based access control (RBAC) including role creation, permission assignment, and user-role relationships.

### Key Features

- **Role Management**: Create, update, and delete roles
- **Permission Assignment**: Add/remove permissions from roles
- **User-Role Management**: Assign/remove roles from users
- **Permission Checking**: Verify user permissions and role membership

### Usage Examples

```java
@Service
public class AdminController {
    private final RoleService roleService; // Interface injection

    // Create a new role
    public Role createRole(String name, String description) {
        return roleService.createRole(name, description);
    }

    // Assign role to user
    public User assignRoleToUser(Long userId, Long roleId) {
        return roleService.assignRoleToUser(userId, roleId);
    }

    // Check user permissions
    public boolean userHasRole(Long userId, String roleName) {
        return roleService.userHasRole(userId, roleName);
    }
}
```

### Role Hierarchy

The system supports **multiple roles per user**, allowing for flexible permission management:

```java
// User with multiple roles
User user = userService.findById(1L).orElseThrow();
user.addRole(employeeRole);     // Basic access
user.addRole(salesRole);        // Sales operations
user.addRole(supportRole);      // Customer support

// Check permissions across all roles
boolean canProcessOrders = roleService.userHasRole(user.getId(), "SALES");
boolean canHandleSupport = roleService.userHasRole(user.getId(), "SUPPORT");
```

## 📧 EmailService

### Interface: `EmailService`

Defines the contract for email operations.

### Implementation: `EmailServiceImpl`

Handles email notifications for various user events (currently a placeholder implementation).

### Supported Email Types

- **Verification Emails**: Email verification links for new accounts
- **Password Reset**: Password reset links for forgotten passwords
- **Welcome Emails**: Welcome messages for new users
- **Security Alerts**: Notifications for security events
- **Account Lockout**: Notifications when accounts are locked

### Implementation Notes

The current implementation logs email operations instead of sending actual emails. In production, integrate with:

- **SendGrid**: Popular email service provider
- **AWS SES**: Amazon's email service
- **JavaMailSender**: Spring's email abstraction
- **SMTP Server**: Direct SMTP integration

### Example Integration

```java
@Service
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Override
    public boolean sendVerificationEmail(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Verify Your Email");
        message.setText("Click here to verify: " + verificationLink + token);

        mailSender.send(message);
        return true;
    }
}
```

## 🛡️ Exception Handling

### Custom Exceptions

The service layer uses custom exceptions for better error handling:

- **AuthenticationException**: Authentication-related errors
- **UserNotFoundException**: User not found scenarios
- **RoleNotFoundException**: Role not found scenarios

### Exception Patterns

```java
// Service method with proper exception handling
public User findById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
}

// Controller handling service exceptions
@ExceptionHandler(UserNotFoundException.class)
public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse("User not found", ex.getMessage()));
}
```

## 📊 Database Performance

### Indexes

Performance indexes have been created for frequently queried columns:

```sql
-- User table indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_enabled ON users(is_enabled);

-- Refresh token indexes
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expiry_date ON refresh_tokens(expiry_date);

-- Junction table indexes
CREATE INDEX idx_user_roles_user_role ON user_roles(user_id, role_id);
CREATE INDEX idx_role_permissions_role_permission ON role_permissions(role_id, permission_id);
```

### Query Optimization

- **Read-Only Transactions**: Used for query operations to improve performance
- **Eager Loading**: Critical relationships loaded eagerly to avoid N+1 queries
- **Batch Operations**: Bulk operations for better performance

## 🔄 Transaction Management

### Transaction Patterns

```java
@Service
@Transactional
public class UserServiceImpl implements UserService {

    // Read-only transaction for queries
    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Full transaction for write operations
    @Override
    public User updateProfile(Long userId, String firstName, String lastName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setFirstName(firstName);
        user.setLastName(lastName);

        return userRepository.save(user);
    }
}
```

### Transaction Benefits

- **ACID Properties**: Ensures data consistency
- **Automatic Rollback**: Failed operations are automatically rolled back
- **Performance**: Read-only transactions improve query performance
- **Isolation**: Concurrent operations are properly isolated

## 🧪 Testing Strategy

### Service Testing

Each service should be tested with:

- **Unit Tests**: Test individual methods in isolation
- **Integration Tests**: Test service interactions
- **Exception Tests**: Verify proper exception handling
- **Transaction Tests**: Ensure transaction boundaries are respected

### Test Examples

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService; // Test implementation directly

    @Test
    void registerUser_WithValidData_ShouldCreateUser() {
        // Given
        String email = "test@example.com";
        String username = "testuser";
        String password = "password";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // When
        User result = userService.registerUser(email, username, password);

        // Then
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getUsername()).isEqualTo(username);
        verify(userRepository).save(any(User.class));
    }
}
```

### Interface Testing

```java
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationService authService; // Mock the interface

    @InjectMocks
    private AuthController authController;

    @Test
    void login_WithValidCredentials_ShouldReturnTokens() {
        // Given
        String username = "testuser";
        String password = "password";
        AuthenticationResult expectedResult = new AuthenticationResult("token", "refresh", user);

        when(authService.authenticate(username, password)).thenReturn(expectedResult);

        // When
        AuthenticationResult result = authController.login(username, password);

        // Then
        assertThat(result).isEqualTo(expectedResult);
        verify(authService).authenticate(username, password);
    }
}
```

## 🚀 Best Practices

### Service Design Principles

1. **Interface Segregation**: Each service has a focused interface
2. **Dependency Inversion**: Depend on abstractions, not concretions
3. **Single Responsibility**: Each service has a clear, focused purpose
4. **Constructor Injection**: Use constructor injection for dependencies
5. **Transaction Boundaries**: Define clear transaction boundaries
6. **Exception Handling**: Use custom exceptions for business logic errors
7. **Logging**: Comprehensive logging for debugging and monitoring

### Performance Considerations

1. **Read-Only Transactions**: Use for query operations
2. **Lazy Loading**: Load relationships only when needed
3. **Batch Operations**: Use for bulk data operations
4. **Caching**: Consider caching for frequently accessed data
5. **Database Indexes**: Ensure proper indexing for query performance

### Security Considerations

1. **Input Validation**: Validate all inputs at service boundaries
2. **Password Security**: Use BCrypt for password hashing
3. **Token Security**: Implement proper token validation and expiration
4. **Access Control**: Verify permissions before sensitive operations
5. **Audit Logging**: Log security-relevant operations

### Interface Design Guidelines

1. **Clear Contracts**: Define clear method signatures and contracts
2. **Consistent Naming**: Use consistent naming conventions
3. **Documentation**: Provide comprehensive JavaDoc
4. **Exception Declarations**: Declare checked exceptions in interface
5. **Versioning**: Consider interface versioning for backward compatibility

## 📚 Related Documentation

- [Security Configuration](SECURITY-CONFIGURATION.md) - Security setup and configuration
- [Entity Models](ENTITY-MODELS.md) - Database entity definitions
- [Repository Layer](REPOSITORY-LAYER.md) - Data access layer documentation
- [Controller Layer](CONTROLLER-LAYER.md) - REST API endpoints
- [Testing Guide](TESTING-GUIDE.md) - Testing strategies and examples
