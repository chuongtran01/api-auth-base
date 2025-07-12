# DTOs & Validation (Phase 9)

## 📋 Overview

Phase 9 focuses on comprehensive Data Transfer Objects (DTOs) and validation system implementation. This phase enhances the API with robust validation, custom validators, and standardized response formats.

## 🎯 Objectives

- ✅ **Request DTOs**: Complete set of validated request objects
- ✅ **Response DTOs**: Standardized response formats
- ✅ **Custom Validation**: Advanced validation annotations
- ✅ **Error Handling**: Enhanced validation error responses
- ✅ **Documentation**: Comprehensive validation guide

## 📦 DTO Structure

### Request DTOs

All request DTOs are implemented as Java records with comprehensive validation:

#### Authentication DTOs

- `RegisterRequest` - User registration
- `LoginRequest` - User login
- `RefreshTokenRequest` - Token refresh
- `LogoutRequest` - User logout
- `ForgotPasswordRequest` - Password reset request
- `ResetPasswordRequest` - Password reset
- `VerifyEmailRequest` - Email verification

#### User Management DTOs

- `ProfileUpdateRequest` - Profile updates
- `PasswordChangeRequest` - Password changes
- `AccountStatusRequest` - Account status updates

#### Admin DTOs

- `RoleCreateRequest` - Role creation
- `RoleUpdateRequest` - Role updates

### Response DTOs

#### Standard Response DTOs

- `AuthenticationResponse` - Login/registration responses
- `UserResponse` - User data responses
- `SuccessResponse` - Success operation responses
- `ErrorResponse` - Error responses
- `UserCountResponse` - User count responses

#### Enhanced Response DTOs

- `ApiResponse<T>` - Generic API response wrapper
- `PagedResponse<T>` - Paginated response wrapper
- `ValidationErrorResponse` - Detailed validation errors

## 🔧 Custom Validation Annotations

### @StrongPassword

Advanced password strength validation with configurable requirements:

```java
@StrongPassword(
    minLength = 8,
    maxLength = 128,
    requireUppercase = true,
    requireLowercase = true,
    requireDigit = true,
    requireSpecial = true,
    message = "Password must be at least 8 characters long and contain uppercase, lowercase, digit, and special character"
)
String password
```

**Features:**

- Configurable minimum/maximum length
- Optional uppercase requirement
- Optional lowercase requirement
- Optional digit requirement
- Optional special character requirement
- Customizable special character set
- Detailed error messages

### @ValidEmail

Enhanced email validation beyond standard @Email annotation:

```java
@ValidEmail(
    allowEmpty = false,
    maxLength = 254,
    message = "Please provide a valid email address"
)
String email
```

**Features:**

- RFC 5321 compliant validation
- Configurable maximum length (default: 254)
- Optional empty value allowance
- Enhanced structure validation
- Domain validation
- Local part validation

## 📝 Validation Examples

### Registration Request

```java
public record RegisterRequest(
    @NotBlank(message = "Email is required")
    @ValidEmail(message = "Please provide a valid email address")
    String email,

    @NotBlank(message = "Password is required")
    @StrongPassword(
        minLength = 8,
        maxLength = 128,
        requireUppercase = true,
        requireLowercase = true,
        requireDigit = true,
        requireSpecial = true,
        message = "Password must be at least 8 characters long and contain uppercase, lowercase, digit, and special character"
    )
    String password
) {}
```

### Password Change Request

```java
public record PasswordChangeRequest(
    @NotBlank(message = "Current password is required")
    String currentPassword,

    @NotBlank(message = "New password is required")
    @StrongPassword(
        minLength = 8,
        maxLength = 128,
        requireUppercase = true,
        requireLowercase = true,
        requireDigit = true,
        requireSpecial = true,
        message = "New password must be at least 8 characters long and contain uppercase, lowercase, digit, and special character"
    )
    String newPassword
) {}
```

## 🚨 Enhanced Error Handling

### ValidationErrorResponse

Detailed validation error responses with field-level information:

```json
{
  "success": false,
  "message": "Request validation failed",
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/auth/register",
  "fieldErrors": {
    "email": "Please provide a valid email address",
    "password": "Password must be at least 8 characters long and contain uppercase, lowercase, digit, and special character"
  },
  "globalErrors": null,
  "errorCount": 2
}
```

### ApiResponse Wrapper

Generic response wrapper for consistent API responses:

```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "username": "user123"
  },
  "timestamp": "2024-01-15T10:30:00",
  "path": "/api/auth/register"
}
```

### PagedResponse

Pagination support for list endpoints:

```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8,
  "hasNext": true,
  "hasPrevious": false,
  "isFirst": true,
  "isLast": false
}
```

## 🔍 Validation Features

### Bean Validation Integration

- `@NotBlank` - Required non-empty strings
- `@Size` - String length constraints
- `@Pattern` - Regex pattern validation
- `@Email` - Standard email validation (enhanced with @ValidEmail)

### Custom Validators

- **StrongPasswordValidator** - Password strength validation
- **ValidEmailValidator** - Enhanced email validation

### Global Exception Handling

- **MethodArgumentNotValidException** - Request body validation
- **ConstraintViolationException** - Parameter validation
- **HttpMessageNotReadableException** - Malformed JSON
- **MissingServletRequestParameterException** - Missing parameters

## 📊 Validation Rules

### Password Requirements

- **Minimum Length**: 8 characters
- **Maximum Length**: 128 characters
- **Uppercase Letters**: Required
- **Lowercase Letters**: Required
- **Digits**: Required
- **Special Characters**: Required
- **Special Character Set**: `!@#$%^&*()_+-=[]{}|;:,.<>?`

### Email Requirements

- **Maximum Length**: 254 characters (RFC 5321)
- **Format**: RFC compliant email format
- **Structure**: Valid local and domain parts
- **Domain**: Must contain at least one dot
- **Consecutive Dots**: Not allowed

### General Validation

- **Required Fields**: All mandatory fields validated
- **String Length**: Appropriate length constraints
- **Format Validation**: Proper data format validation
- **Business Rules**: Domain-specific validation rules

## 🛠️ Implementation Details

### Custom Validator Structure

```
src/main/java/com/authbase/validation/
├── annotation/
│   ├── StrongPassword.java
│   └── ValidEmail.java
└── validator/
    ├── StrongPasswordValidator.java
    └── ValidEmailValidator.java
```

### DTO Organization

```
src/main/java/com/authbase/dto/
├── auth/
│   ├── RegisterRequest.java
│   ├── LoginRequest.java
│   ├── RefreshTokenRequest.java
│   ├── LogoutRequest.java
│   ├── ForgotPasswordRequest.java
│   ├── ResetPasswordRequest.java
│   ├── VerifyEmailRequest.java
│   └── AuthenticationResponse.java
├── user/
│   ├── ProfileUpdateRequest.java
│   ├── PasswordChangeRequest.java
│   ├── AccountStatusRequest.java
│   └── UserResponse.java
├── admin/
│   ├── RoleCreateRequest.java
│   ├── RoleUpdateRequest.java
│   └── UserCountResponse.java
├── common/
│   ├── SuccessResponse.java
│   ├── ErrorResponse.java
│   ├── ApiResponse.java
│   ├── PagedResponse.java
│   └── ValidationErrorResponse.java
```

## 🧪 Testing Validation

### Unit Tests

```java
@Test
void testStrongPasswordValidation() {
    StrongPasswordValidator validator = new StrongPasswordValidator();
    StrongPassword annotation = createMockAnnotation();
    validator.initialize(annotation);

    assertTrue(validator.isValid("StrongPass123!", null));
    assertFalse(validator.isValid("weak", null));
    assertFalse(validator.isValid("nouppercase123!", null));
}
```

### Integration Tests

```java
@Test
void testRegistrationValidation() {
    RegisterRequest request = new RegisterRequest("invalid-email", "weak");

    Set<ConstraintViolation<RegisterRequest>> violations =
        validator.validate(request);

    assertEquals(2, violations.size());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    assertTrue(violations.stream()
        .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
}
```

## 📈 Benefits

### Security

- **Strong Password Policy**: Enforces secure password requirements
- **Email Validation**: Prevents invalid email formats
- **Input Sanitization**: Validates all user inputs

### User Experience

- **Clear Error Messages**: Specific validation error messages
- **Field-Level Errors**: Pinpoint exact validation issues
- **Consistent Responses**: Standardized API response format

### Maintainability

- **Reusable Validators**: Custom validation annotations
- **Centralized Validation**: Global exception handling
- **Type Safety**: Java records for type-safe DTOs

### Performance

- **Early Validation**: Request-level validation
- **Efficient Processing**: Optimized validation logic
- **Reduced Database Calls**: Validation before persistence

## 🔄 Migration Guide

### From Basic Validation

1. **Replace @Email with @ValidEmail** for enhanced email validation
2. **Replace @Size with @StrongPassword** for password fields
3. **Update error handling** to use ValidationErrorResponse
4. **Migrate response formats** to use ApiResponse wrapper

### Configuration Updates

```yaml
# application.yml
validation:
  password:
    min-length: 8
    max-length: 128
    require-uppercase: true
    require-lowercase: true
    require-digit: true
    require-special: true
  email:
    max-length: 254
    allow-empty: false
```

## 🎯 Next Steps

### Phase 10: Security Enhancements

- Rate limiting implementation
- Account lockout mechanisms
- Security event logging
- Request logging for audit

### Future Enhancements

- **Custom Validation Groups**: Different validation rules for different contexts
- **Async Validation**: Background validation for complex rules
- **Validation Caching**: Cache validation results for performance
- **Internationalization**: Multi-language validation messages

---

## 📚 Related Documentation

- [REST Controllers (Phase 8)](REST-CONTROLLERS.md)
- [Security Configuration (Phase 4)](../security/SECURITY-CONFIGURATION.md)
- [Service Layer (Phase 6)](../service/SERVICE-LAYER-IMPLEMENTATION.md)
- [Global Exception Handling](../api/GLOBAL-EXCEPTION-HANDLER.md)
