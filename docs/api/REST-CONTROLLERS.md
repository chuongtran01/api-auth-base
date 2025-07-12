# REST Controllers Implementation

## Overview

Phase 8 implements comprehensive REST controllers for the JWT authentication system, providing a complete API surface for authentication, user management, and administrative operations.

## Controllers Overview

### 1. AuthController (`/auth`)

Handles all authentication-related operations.

**Base Path:** `/auth`

#### Endpoints:

| Method | Endpoint           | Description               | Request Body            | Response                 |
| ------ | ------------------ | ------------------------- | ----------------------- | ------------------------ |
| POST   | `/login`           | User login                | `LoginRequest`          | `AuthenticationResponse` |
| POST   | `/register`        | User registration         | `RegisterRequest`       | `UserResponse`           |
| POST   | `/refresh`         | Refresh access token      | `RefreshTokenRequest`   | `AuthenticationResponse` |
| POST   | `/logout`          | User logout               | `LogoutRequest`         | `SuccessResponse`        |
| POST   | `/forgot-password` | Request password reset    | `ForgotPasswordRequest` | `SuccessResponse`        |
| POST   | `/reset-password`  | Reset password with token | `ResetPasswordRequest`  | `SuccessResponse`        |
| POST   | `/verify-email`    | Verify email with token   | `VerifyEmailRequest`    | `SuccessResponse`        |

### 2. UserController (`/users`)

Handles user profile management operations.

**Base Path:** `/users`  
**Authentication Required:** Yes

#### Endpoints:

| Method | Endpoint    | Description         | Request Body            | Response          |
| ------ | ----------- | ------------------- | ----------------------- | ----------------- |
| GET    | `/profile`  | Get user profile    | None                    | `UserResponse`    |
| PUT    | `/profile`  | Update user profile | `ProfileUpdateRequest`  | `UserResponse`    |
| PUT    | `/password` | Change password     | `PasswordChangeRequest` | `SuccessResponse` |
| DELETE | `/account`  | Delete account      | None                    | `SuccessResponse` |

### 3. AdminController (`/admin`)

Handles administrative operations for user and role management.

**Base Path:** `/admin`  
**Authentication Required:** Yes  
**Role Required:** ADMIN

#### User Management Endpoints:

| Method | Endpoint                 | Description        | Request Body           | Response            |
| ------ | ------------------------ | ------------------ | ---------------------- | ------------------- |
| GET    | `/users`                 | Get all users      | None                   | `List<User>`        |
| GET    | `/users/{userId}`        | Get user by ID     | None                   | `UserResponse`      |
| PUT    | `/users/{userId}/status` | Update user status | `AccountStatusRequest` | `UserResponse`      |
| DELETE | `/users/{userId}`        | Delete user        | None                   | `SuccessResponse`   |
| GET    | `/users/count`           | Get user count     | None                   | `UserCountResponse` |

#### Role Management Endpoints:

| Method | Endpoint                         | Description           | Request Body        | Response          |
| ------ | -------------------------------- | --------------------- | ------------------- | ----------------- |
| GET    | `/roles`                         | Get all roles         | None                | `List<Role>`      |
| GET    | `/roles/{roleId}`                | Get role by ID        | None                | `Role`            |
| POST   | `/roles`                         | Create new role       | `RoleCreateRequest` | `Role`            |
| PUT    | `/roles/{roleId}`                | Update role           | `RoleUpdateRequest` | `Role`            |
| DELETE | `/roles/{roleId}`                | Delete role           | None                | `SuccessResponse` |
| POST   | `/users/{userId}/roles/{roleId}` | Assign role to user   | None                | `SuccessResponse` |
| DELETE | `/users/{userId}/roles/{roleId}` | Remove role from user | None                | `SuccessResponse` |

## Request/Response DTOs

### Authentication DTOs

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

#### RefreshTokenRequest

```java
public record RefreshTokenRequest(
    @NotBlank(message = "Refresh token is required")
    String refreshToken
) {}
```

#### LogoutRequest

```java
public record LogoutRequest(
    @NotBlank(message = "Refresh token is required")
    String refreshToken
) {}
```

### User Management DTOs

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

#### PasswordChangeRequest

```java
public record PasswordChangeRequest(
    @NotBlank(message = "Current password is required")
    String currentPassword,

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    String newPassword
) {}
```

### Admin DTOs

#### AccountStatusRequest

```java
public record AccountStatusRequest(boolean enabled) {}
```

#### RoleCreateRequest

```java
public record RoleCreateRequest(
    @NotBlank(message = "Role name is required")
    @Size(max = 50, message = "Role name must be at most 50 characters")
    String name,

    @Size(max = 200, message = "Role description must be at most 200 characters")
    String description
) {}
```

#### RoleUpdateRequest

```java
public record RoleUpdateRequest(
    @Size(max = 200, message = "Role description must be at most 200 characters")
    String description
) {}
```

## Input Validation

All controller endpoints use Bean Validation annotations for input validation:

### Validation Annotations Used:

- `@NotBlank`: Ensures string is not null, empty, or whitespace
- `@Email`: Validates email format
- `@Size`: Validates string length (min/max)
- `@Valid`: Triggers validation on request body

### Validation Features:

- **Automatic Validation**: All `@RequestBody` parameters are validated automatically
- **Custom Error Messages**: Each validation annotation includes descriptive error messages
- **Global Exception Handling**: Validation errors are handled by `GlobalExceptionHandler`
- **Consistent Error Responses**: All validation errors return standardized error responses

## Security Features

### Authentication

- **JWT Token Authentication**: All protected endpoints require valid JWT tokens
- **Role-Based Access Control**: Admin endpoints require ADMIN role
- `@PreAuthorize("hasRole('ADMIN')")` annotation on AdminController

### Authorization

- **User Context**: User endpoints access current user from `Authentication` principal
- **Admin Privileges**: Admin endpoints can manage any user/role
- **Token Blacklisting**: Logout endpoint blacklists access tokens

## Error Handling

### Global Exception Handler

All controllers use the centralized `GlobalExceptionHandler` for consistent error responses:

- **Validation Errors**: 400 Bad Request with field-specific messages
- **Authentication Errors**: 401 Unauthorized
- **Authorization Errors**: 403 Forbidden
- **Not Found Errors**: 404 Not Found
- **Server Errors**: 500 Internal Server Error

### Error Response Format

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "details": {
    "email": "Email must be a valid email address",
    "password": "Password must be at least 8 characters long"
  }
}
```

## API Usage Examples

### Authentication Flow

#### 1. User Registration

```bash
POST /auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

#### 2. User Login

```bash
POST /auth/login
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "securePassword123"
}
```

#### 3. Token Refresh

```bash
POST /auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### 4. User Logout

```bash
POST /auth/logout
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### User Profile Management

#### 1. Get Profile

```bash
GET /users/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### 2. Update Profile

```bash
PUT /users/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "username": "newusername",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### 3. Change Password

```bash
PUT /users/password
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "currentPassword": "oldPassword123",
  "newPassword": "newPassword456"
}
```

### Admin Operations

#### 1. Get All Users

```bash
GET /admin/users
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### 2. Create Role

```bash
POST /admin/roles
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "name": "MODERATOR",
  "description": "Content moderation role"
}
```

#### 3. Assign Role to User

```bash
POST /admin/users/123/roles/456
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## Implementation Notes

### Service Layer Integration

- All controllers delegate business logic to service layer
- Proper separation of concerns maintained
- Service methods handle all data validation and business rules

### Logging

- Comprehensive logging at INFO level for all operations
- DEBUG level for data retrieval operations
- WARN level for failed operations with error details

### Transaction Management

- Service layer handles transaction boundaries
- Read-only operations marked with `@Transactional(readOnly = true)`
- Write operations use default transaction behavior

### Future Enhancements

- **Email Service Integration**: Password reset and email verification endpoints need email service integration
- **Token Management**: Implement proper token storage and validation for password reset and email verification
- **Rate Limiting**: Add rate limiting for authentication endpoints
- **Audit Logging**: Add comprehensive audit logging for admin operations

## Testing Strategy

### Unit Tests

- Controller method testing with MockMvc
- Service layer integration testing
- Validation testing for all DTOs

### Integration Tests

- End-to-end API testing
- Authentication flow testing
- Authorization testing for admin endpoints

### Test Data

- Test users with different roles
- Mock JWT tokens for testing
- Test database setup

## Performance Considerations

### Caching

- User profile data caching (future enhancement)
- Role and permission caching (future enhancement)

### Database Optimization

- Proper indexing on frequently queried fields
- Efficient query patterns in service layer

### Response Optimization

- Minimal response payloads
- Efficient JSON serialization
- Proper HTTP status codes

## Security Best Practices

### Input Validation

- All user inputs validated using Bean Validation
- SQL injection prevention through JPA/Hibernate
- XSS prevention through proper output encoding

### Authentication Security

- JWT token expiration and refresh mechanism
- Token blacklisting on logout
- Secure password hashing with BCrypt

### Authorization Security

- Role-based access control
- Method-level security annotations
- Principle of least privilege

## Monitoring and Observability

### Health Checks

- Application health endpoint available
- Database connectivity monitoring
- Redis connectivity monitoring (if enabled)

### Metrics

- Request/response metrics (future enhancement)
- Error rate monitoring
- Performance metrics collection

### Logging

- Structured logging with SLF4J
- Request/response logging
- Error logging with stack traces
