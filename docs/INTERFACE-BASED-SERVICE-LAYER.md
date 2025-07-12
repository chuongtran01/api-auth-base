# Interface-Based Service Layer

This document explains the refactoring of the service layer to use interfaces and implementations, following Spring best practices and SOLID principles.

## 🎯 Overview

The service layer has been refactored to follow the **Interface Segregation Principle** and **Dependency Inversion Principle**, providing better testability, maintainability, and flexibility.

## 🔄 Refactoring Changes

### Before: Direct Implementation

```java
@Service
public class UserService {
    // Direct implementation with business logic
}
```

### After: Interface + Implementation

```java
// Interface (Contract)
public interface UserService {
    User registerUser(String email, String username, String password);
    // ... other method signatures
}

// Implementation (Business Logic)
@Service
public class UserServiceImpl implements UserService {
    // Implementation with business logic
}
```

## 📁 New File Structure

```
src/main/java/com/authbase/service/
├── UserService.java              # Interface
├── AuthenticationService.java    # Interface
├── RoleService.java              # Interface
├── EmailService.java             # Interface
└── impl/
    ├── UserServiceImpl.java      # Implementation
    ├── AuthenticationServiceImpl.java
    ├── RoleServiceImpl.java
    └── EmailServiceImpl.java
```

## 🏗️ Service Interfaces

### 1. UserService Interface

```java
public interface UserService {
    User registerUser(String email, String password);
    User registerUser(String email, String password, String username, String firstName, String lastName);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    List<User> findAllUsers();
    User updateProfile(Long userId, String username, String firstName, String lastName);
    boolean changePassword(Long userId, String currentPassword, String newPassword);
    User updateEmailVerificationStatus(Long userId, boolean isVerified);
    User updateAccountStatus(Long userId, boolean enabled);
    User updateLastLogin(Long userId);
    boolean deleteUser(Long userId);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    long getUserCount();
}
```

**Key Changes**:

- **Simplified registration**: Primary method only requires email and password
- **Optional profile**: Additional method for username and profile information
- **Email-first authentication**: Uses email as the unique identifier
- **Username**: Optional field for display purposes only

### 2. AuthenticationService Interface

```java
public interface AuthenticationService {
    AuthenticationResult authenticate(String username, String password);
    AuthenticationResult refreshToken(String refreshTokenString);
    boolean logout(String refreshTokenString);
    boolean logoutByUserId(Long userId);
    boolean validateRefreshToken(String refreshTokenString);
    Optional<User> getUserFromRefreshToken(String refreshTokenString);
    long cleanupExpiredTokens();
    long getActiveSessionsCount(Long userId);

    class AuthenticationResult {
        private final String accessToken;
        private final String refreshToken;
        private final User user;
        // ... getters
    }
}
```

### 3. RoleService Interface

```java
public interface RoleService {
    Role createRole(String name, String description);
    Optional<Role> findByName(String name);
    Optional<Role> findById(Long id);
    List<Role> findAllRoles();
    Role updateRoleDescription(Long roleId, String description);
    boolean deleteRole(Long roleId);
    Role addPermissionToRole(Long roleId, Long permissionId);
    Role removePermissionFromRole(Long roleId, Long permissionId);
    User assignRoleToUser(Long userId, Long roleId);
    User removeRoleFromUser(Long userId, Long roleId);
    Set<Permission> getRolePermissions(Long roleId);
    Set<Role> getUserRoles(Long userId);
    boolean userHasRole(Long userId, String roleName);
    boolean roleHasPermission(Long roleId, String permissionName);
    long getRoleCount();
    boolean existsByName(String name);
}
```

### 4. EmailService Interface

```java
public interface EmailService {
    boolean sendVerificationEmail(String email, String username, String verificationToken);
    boolean sendPasswordResetEmail(String email, String username, String resetToken);
    boolean sendWelcomeEmail(String email, String username, String firstName);
    boolean sendAccountLockoutEmail(String email, String username, String lockoutReason);
    boolean sendSecurityAlertEmail(String email, String username, String alertType, String details);
}
```

## ✅ Benefits of Interface-Based Design

### 1. **Loose Coupling**

- Controllers depend on interfaces, not concrete implementations
- Changes to implementation don't affect dependent classes
- Easier to swap implementations without breaking code

```java
@Service
public class AuthController {
    private final AuthenticationService authService; // Interface dependency

    public AuthController(AuthenticationService authService) {
        this.authService = authService; // Spring injects implementation
    }
}
```

### 2. **Enhanced Testability**

- Easy to mock interfaces for unit testing
- Can test controllers in isolation
- Better separation of concerns in tests

```java
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock
    private AuthenticationService authService; // Mock interface

    @InjectMocks
    private AuthController authController;

    @Test
    void login_ShouldReturnTokens() {
        when(authService.authenticate(any(), any()))
            .thenReturn(new AuthenticationResult("token", "refresh", user));

        // Test controller logic
    }
}
```

### 3. **Flexibility**

- Multiple implementations can be created
- Easy to switch between implementations
- Support for different strategies or algorithms

```java
// Could have different implementations
@Service
public class MockEmailServiceImpl implements EmailService {
    // For testing
}

@Service
public class SendGridEmailServiceImpl implements EmailService {
    // For production with SendGrid
}

@Service
public class AwsSesEmailServiceImpl implements EmailService {
    // For production with AWS SES
}
```

### 4. **Maintainability**

- Clear separation of concerns
- Easier to understand and modify
- Better code organization

### 5. **SOLID Principles Compliance**

- **S**ingle Responsibility: Each interface has a focused purpose
- **O**pen/Closed: Open for extension, closed for modification
- **L**iskov Substitution: Implementations can be substituted
- **I**nterface Segregation: Focused, cohesive interfaces
- **D**ependency Inversion: Depend on abstractions, not concretions

## 🔧 Implementation Details

### Constructor Injection

All implementations use constructor injection for dependencies:

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Spring automatically injects dependencies via constructor
}
```

### Transaction Management

Transaction annotations are applied to implementation methods:

```java
@Override
@Transactional(readOnly = true)
public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
}

@Override
@Transactional
public User updateProfile(Long userId, String firstName, String lastName) {
    // Full transaction for write operations
}
```

### Exception Handling

Custom exceptions are used for better error handling:

```java
@Override
public User findById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
}
```

## 🧪 Testing Strategy

### Unit Testing

Test implementations directly with mocked dependencies:

```java
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void registerUser_WithValidData_ShouldCreateUser() {
        // Test implementation logic
    }
}
```

### Integration Testing

Test controllers with mocked service interfaces:

```java
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @MockBean
    private AuthenticationService authService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void login_ShouldReturnTokens() {
        // Test controller endpoints
    }
}
```

## 🚀 Best Practices

### Interface Design

1. **Keep interfaces focused**: Each interface should have a single responsibility
2. **Use descriptive names**: Interface names should clearly indicate their purpose
3. **Document thoroughly**: Provide comprehensive JavaDoc for all methods
4. **Consider versioning**: Design interfaces with future extensibility in mind

### Implementation Design

1. **Follow naming convention**: Use `ServiceImpl` suffix for implementations
2. **Use constructor injection**: Avoid field injection for better testability
3. **Apply proper annotations**: Use `@Service`, `@Transactional`, etc.
4. **Handle exceptions properly**: Use custom exceptions for business logic errors

### Dependency Injection

1. **Inject interfaces**: Controllers should depend on service interfaces
2. **Use constructor injection**: Prefer constructor injection over field injection
3. **Mark dependencies as final**: Ensure immutability of injected dependencies

## 📚 Migration Guide

### For Existing Code

If you have existing code that depends on service classes:

1. **Update imports**: Change from concrete classes to interfaces
2. **Update dependency injection**: Spring will automatically inject implementations
3. **Update tests**: Mock interfaces instead of concrete classes

### For New Code

When adding new services:

1. **Create interface first**: Define the contract
2. **Create implementation**: Implement the business logic
3. **Add tests**: Test both interface and implementation
4. **Document**: Provide comprehensive documentation

## 🔄 Future Enhancements

### Multiple Implementations

The interface-based design allows for multiple implementations:

```java
// Different email service implementations
@Service("sendGridEmailService")
public class SendGridEmailServiceImpl implements EmailService { }

@Service("awsSesEmailService")
public class AwsSesEmailServiceImpl implements EmailService { }

// Different authentication strategies
@Service("jwtAuthService")
public class JwtAuthenticationServiceImpl implements AuthenticationService { }

@Service("oauthAuthService")
public class OAuthAuthenticationServiceImpl implements AuthenticationService { }
```

### Configuration-Based Selection

Use Spring profiles to select implementations:

```java
@Profile("sendgrid")
@Service
public class SendGridEmailServiceImpl implements EmailService { }

@Profile("aws-ses")
@Service
public class AwsSesEmailServiceImpl implements EmailService { }
```

## 📋 Summary

The interface-based service layer provides:

- ✅ **Better testability** through easy mocking
- ✅ **Loose coupling** between layers
- ✅ **Flexibility** for multiple implementations
- ✅ **Maintainability** through clear separation
- ✅ **SOLID principles** compliance
- ✅ **Future extensibility** for new features

This refactoring follows Spring Boot best practices and provides a solid foundation for building scalable, maintainable applications.
