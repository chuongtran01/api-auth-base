# Profile Update Functionality

## Overview

The profile update functionality allows users to modify their profile information including username, first name, and last name. This feature provides a comprehensive way for users to maintain and update their account information.

## Key Features

### 1. **Username Updates**

- Users can change their username
- Username uniqueness validation
- Optional field - users can keep existing username

### 2. **Profile Information**

- Update first name and last name
- All fields are optional
- Maintains existing data if not provided

### 3. **Validation**

- Username uniqueness check
- User existence validation
- Proper error handling

## Service Layer Implementation

### UserService Interface

```java
public interface UserService {
    /**
     * Update user profile information.
     *
     * @param userId    user ID
     * @param username  new username (optional)
     * @param firstName new first name
     * @param lastName  new last name
     * @return updated user
     * @throws IllegalArgumentException if user not found or username already taken
     */
    User updateProfile(Long userId, String username, String firstName, String lastName);
}
```

### UserServiceImpl Implementation

```java
@Override
public User updateProfile(Long userId, String username, String firstName, String lastName) {
    log.info("Updating profile for user ID: {}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

    // If username is being updated, check uniqueness
    if (username != null && !username.isBlank() && !username.equals(user.getUsername())) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username " + username + " is already taken");
        }
        user.setUsername(username);
    }

    user.setFirstName(firstName);
    user.setLastName(lastName);

    User updatedUser = userRepository.save(user);
    log.info("Profile updated successfully for user: {}", updatedUser.getEmail());

    return updatedUser;
}
```

## Usage Examples

### Basic Profile Update

```java
@Service
public class UserController {
    private final UserService userService;

    public ResponseEntity<User> updateProfile(
            @PathVariable Long userId,
            @RequestBody UpdateProfileRequest request) {

        User updatedUser = userService.updateProfile(
            userId,
            request.getUsername(),
            request.getFirstName(),
            request.getLastName()
        );

        return ResponseEntity.ok(updatedUser);
    }
}
```

### Request DTO

```java
public class UpdateProfileRequest {
    private String username;
    private String firstName;
    private String lastName;

    // Constructors, getters, setters
}
```

### Controller Endpoint

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @PutMapping("/{userId}/profile")
    public ResponseEntity<User> updateProfile(
            @PathVariable Long userId,
            @RequestBody @Valid UpdateProfileRequest request) {

        User updatedUser = userService.updateProfile(
            userId,
            request.getUsername(),
            request.getFirstName(),
            request.getLastName()
        );

        return ResponseEntity.ok(updatedUser);
    }
}
```

## Business Logic

### Username Update Logic

1. **Check if username is provided**: Only validate if username is not null and not blank
2. **Check if username is changing**: Compare with current username to avoid unnecessary validation
3. **Validate uniqueness**: Ensure the new username is not already taken by another user
4. **Update username**: Set the new username if validation passes

### Profile Update Logic

1. **Find user**: Retrieve user by ID
2. **Validate user exists**: Throw exception if user not found
3. **Update username**: Apply username validation and update if needed
4. **Update profile fields**: Set first name and last name
5. **Save changes**: Persist updates to database

## Error Handling

### Common Exceptions

```java
// User not found
throw new IllegalArgumentException("User not found with ID: " + userId);

// Username already taken
throw new IllegalArgumentException("Username " + username + " is already taken");
```

### Exception Handling in Controller

```java
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
    ErrorResponse error = new ErrorResponse("Validation Error", ex.getMessage());
    return ResponseEntity.badRequest().body(error);
}
```

## Testing

### Unit Tests

```java
@Test
@DisplayName("Should update user profile with username")
void updateProfile_WithUsername_ShouldUpdateUser() {
    // Given
    Long userId = 1L;
    String newUsername = "newusername";
    String firstName = "John";
    String lastName = "Doe";

    User existingUser = new User("test@example.com", "password");
    existingUser.setId(userId);
    existingUser.setUsername("oldusername");

    when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
    when(userRepository.findByUsername(newUsername)).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

    // When
    User result = userService.updateProfile(userId, newUsername, firstName, lastName);

    // Then
    assertThat(result.getUsername()).isEqualTo(newUsername);
    assertThat(result.getFirstName()).isEqualTo(firstName);
    assertThat(result.getLastName()).isEqualTo(lastName);
}
```

### Test Scenarios

1. **Successful username update**: User changes to available username
2. **Profile update without username change**: User keeps existing username
3. **Username already taken**: Attempt to use existing username
4. **User not found**: Attempt to update non-existent user
5. **Null/blank username**: Handle optional username field

## API Examples

### Request Examples

```json
// Update username and profile
{
    "username": "newusername",
    "firstName": "John",
    "lastName": "Doe"
}

// Update profile only (keep existing username)
{
    "firstName": "Jane",
    "lastName": "Smith"
}

// Update username only
{
    "username": "newusername"
}
```

### Response Examples

```json
// Successful update
{
    "id": 1,
    "email": "user@example.com",
    "username": "newusername",
    "firstName": "John",
    "lastName": "Doe",
    "isEnabled": true,
    "isEmailVerified": false,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T11:00:00"
}

// Error response
{
    "error": "Validation Error",
    "message": "Username newusername is already taken"
}
```

## Security Considerations

### Authorization

- Users should only be able to update their own profile
- Implement proper authentication and authorization checks
- Validate user ownership before allowing updates

### Input Validation

```java
@Valid
public class UpdateProfileRequest {
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username can only contain letters, numbers, underscores, and hyphens")
    private String username;

    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;
}
```

### Rate Limiting

- Implement rate limiting to prevent abuse
- Consider cooldown periods for username changes
- Monitor for suspicious update patterns

## Best Practices

### 1. **Validation**

- Always validate username uniqueness
- Check for reserved usernames
- Implement proper input sanitization

### 2. **User Experience**

- Provide clear error messages
- Allow users to check username availability
- Support partial updates

### 3. **Performance**

- Use database indexes for username lookups
- Implement caching for frequently accessed profiles
- Optimize database queries

### 4. **Audit Trail**

- Log profile update events
- Track username change history
- Maintain audit records for compliance

### 5. **Notifications**

- Notify users of successful updates
- Alert users of username changes
- Send confirmation emails for critical changes

## Future Enhancements

### 1. **Username History**

- Track username change history
- Allow reverting to previous usernames
- Implement username cooldown periods

### 2. **Profile Pictures**

- Add profile picture upload functionality
- Support multiple image formats
- Implement image resizing and optimization

### 3. **Additional Fields**

- Add bio/description field
- Support for social media links
- Custom profile themes

### 4. **Bulk Updates**

- Support for updating multiple users
- Batch profile updates for administrators
- Bulk username validation

This enhanced profile update functionality provides a comprehensive solution for user profile management while maintaining security and performance standards.
