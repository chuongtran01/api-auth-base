# DTO Organization

## Overview

All Data Transfer Objects (DTOs) have been organized in the dedicated `dto` package for better code organization and maintainability. This document outlines the structure and categorization of DTOs.

## DTO Package Structure

```
src/main/java/com/authbase/dto/
├── Authentication DTOs
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── RefreshTokenRequest.java
│   ├── LogoutRequest.java
│   ├── ForgotPasswordRequest.java
│   ├── ResetPasswordRequest.java
│   ├── VerifyEmailRequest.java
│   └── AuthenticationResponse.java
├── User Management DTOs
│   ├── ProfileUpdateRequest.java
│   ├── PasswordChangeRequest.java
│   ├── UserResponse.java
│   └── UserCountResponse.java
├── Admin Management DTOs
│   ├── AccountStatusRequest.java
│   ├── RoleCreateRequest.java
│   └── RoleUpdateRequest.java
└── Common Response DTOs
    ├── SuccessResponse.java
    └── ErrorResponse.java
```

## DTO Categories

### 1. Authentication DTOs

**Request DTOs:**

- `LoginRequest` - User login credentials
- `RegisterRequest` - User registration data
- `RefreshTokenRequest` - Token refresh request
- `LogoutRequest` - User logout request
- `ForgotPasswordRequest` - Password reset request
- `ResetPasswordRequest` - Password reset with token
- `VerifyEmailRequest` - Email verification with token

**Response DTOs:**

- `AuthenticationResponse` - Authentication result with tokens

### 2. User Management DTOs

**Request DTOs:**

- `ProfileUpdateRequest` - User profile update data
- `PasswordChangeRequest` - Password change request

**Response DTOs:**

- `UserResponse` - User information response
- `UserCountResponse` - User count for admin operations

### 3. Admin Management DTOs

**Request DTOs:**

- `AccountStatusRequest` - User account status update
- `RoleCreateRequest` - New role creation
- `RoleUpdateRequest` - Role update data

### 4. Common Response DTOs

**Response DTOs:**

- `SuccessResponse` - Generic success response
- `ErrorResponse` - Generic error response

## Validation Annotations

All DTOs include appropriate Bean Validation annotations:

### Common Validation Patterns

```java
// Required field validation
@NotBlank(message = "Field is required")

// Email validation
@Email(message = "Email must be a valid email address")

// String length validation
@Size(min = 8, max = 100, message = "Field must be between 8 and 100 characters")

// Password strength validation
@Size(min = 8, message = "Password must be at least 8 characters long")
```

### Validation Examples

#### LoginRequest

```java
public record LoginRequest(
    @NotBlank(message = "Username or email is required")
    String username,

    @NotBlank(message = "Password is required")
    String password
) {}
```

#### RegisterRequest

```java
public record RegisterRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    String password
) {}
```

#### ProfileUpdateRequest

```java
public record ProfileUpdateRequest(
    @Size(max = 50, message = "Username must be at most 50 characters")
    String username,

    @Size(max = 100, message = "First name must be at most 100 characters")
    String firstName,

    @Size(max = 100, message = "Last name must be at most 100 characters")
    String lastName
) {}
```

## Benefits of DTO Organization

### 1. **Separation of Concerns**

- Controllers focus on HTTP handling
- DTOs handle data transfer and validation
- Clear boundaries between layers

### 2. **Reusability**

- DTOs can be reused across different controllers
- Consistent validation rules
- Standardized response formats

### 3. **Maintainability**

- Easy to locate and modify DTOs
- Centralized validation logic
- Clear categorization by functionality

### 4. **Testing**

- DTOs can be tested independently
- Validation logic is isolated
- Easier to mock in controller tests

### 5. **Documentation**

- Clear structure for API documentation
- Easy to understand request/response formats
- Consistent naming conventions

## Usage in Controllers

### Import Statements

```java
import com.authbase.dto.LoginRequest;
import com.authbase.dto.AuthenticationResponse;
import com.authbase.dto.SuccessResponse;
// ... other imports
```

### Controller Method Example

```java
@PostMapping("/login")
public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
    // Controller logic
}
```

## Future Enhancements

### 1. **DTO Versioning**

- Consider versioning for API evolution
- Backward compatibility strategies

### 2. **Custom Validation**

- Custom validation annotations for complex rules
- Business logic validation

### 3. **DTO Mapping**

- Consider using MapStruct for entity-DTO mapping
- Reduce boilerplate code

### 4. **Documentation Generation**

- OpenAPI/Swagger integration
- Automatic API documentation from DTOs

## Best Practices

### 1. **Naming Conventions**

- Use descriptive names ending with `Request` or `Response`
- Follow Java naming conventions
- Be consistent across all DTOs

### 2. **Validation Messages**

- Provide clear, user-friendly error messages
- Use consistent message formatting
- Consider internationalization

### 3. **Immutability**

- Use Java records for immutability
- Prevent accidental modification
- Thread-safe by design

### 4. **Documentation**

- Include JavaDoc comments for complex DTOs
- Document validation rules
- Provide usage examples

## Migration Notes

### From Embedded DTOs

- All embedded DTOs have been moved to dedicated files
- Controllers now import DTOs from the `dto` package
- No functional changes to API behavior
- Validation rules remain the same

### Compilation

- All DTOs compile successfully
- Tests pass without modification
- No breaking changes to existing functionality
