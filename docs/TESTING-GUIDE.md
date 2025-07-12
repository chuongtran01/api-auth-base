# Testing Guide

This document provides comprehensive guidance on running tests, testing strategies, and best practices for the JWT authentication backend.

## 📋 Overview

The project includes multiple types of tests to ensure code quality, functionality, and reliability:

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test component interactions
- **Controller Tests**: Test REST API endpoints
- **Service Tests**: Test business logic
- **Repository Tests**: Test data access layer

## 🚀 Quick Start

### Run All Tests

```bash
# Run all tests
./mvnw test

# Run tests with detailed output
./mvnw test -X

# Run tests and generate reports
./mvnw test jacoco:report
```

### Run Specific Test Types

```bash
# Run only unit tests
./mvnw test -Dtest="*UnitTest"

# Run only integration tests
./mvnw test -Dtest="*IntegrationTest"

# Run only controller tests
./mvnw test -Dtest="*ControllerTest"

# Run only service tests
./mvnw test -Dtest="*ServiceTest"
```

### Run Specific Test Classes

```bash
# Run specific test class
./mvnw test -Dtest=UserServiceTest

# Run specific test method
./mvnw test -Dtest=UserServiceTest#registerUser_WithValidData_ShouldCreateUser

# Run multiple test classes
./mvnw test -Dtest=UserServiceTest,AuthenticationServiceTest
```

## 🧪 Test Structure

### Directory Organization

```
src/
├── test/
│   ├── java/
│   │   └── com/authbase/
│   │       ├── controller/          # Controller tests
│   │       ├── service/             # Service tests
│   │       ├── repository/          # Repository tests
│   │       ├── security/            # Security tests
│   │       └── integration/         # Integration tests
│   └── resources/
│       ├── application-test.yml     # Test configuration
│       └── data/                    # Test data files
```

### Test Naming Conventions

- **Unit Tests**: `*Test.java`
- **Integration Tests**: `*IntegrationTest.java`
- **Controller Tests**: `*ControllerTest.java`
- **Service Tests**: `*ServiceTest.java`
- **Repository Tests**: `*RepositoryTest.java`

## 🔧 Test Configuration

### Test Properties

The test configuration is defined in `src/test/resources/application-test.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
    properties:
      hibernate:
        format_sql: true

  security:
    jwt:
      secret: test-secret-key-for-testing-purposes-only
      access-token-validity: 3600
      refresh-token-validity: 86400

logging:
  level:
    com.authbase: DEBUG
    org.springframework.security: DEBUG
```

### Test Profiles

```bash
# Run tests with specific profile
./mvnw test -Dspring.profiles.active=test

# Run tests with multiple profiles
./mvnw test -Dspring.profiles.active=test,integration
```

## 🎯 Unit Testing

### Service Layer Testing

Unit tests focus on testing individual service methods in isolation using mocks.

#### Example: UserService Test

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Should register user with valid data")
    void registerUser_WithValidData_ShouldCreateUser() {
        // Given
        String email = "test@example.com";
        String username = "testuser";
        String password = "password";
        String encodedPassword = "encodedPassword";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // When
        User result = userService.registerUser(email, username, password);

        // Then
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getPassword()).isEqualTo(encodedPassword);
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.isEmailVerified()).isFalse();

        verify(userRepository).findByEmail(email);
        verify(userRepository).findByUsername(username);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when user with email already exists")
    void registerUser_WithExistingEmail_ShouldThrowException() {
        // Given
        String email = "existing@example.com";
        String username = "testuser";
        String password = "password";

        User existingUser = new User();
        existingUser.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(email, username, password))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User with email " + email + " already exists");

        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }
}
```

#### Running Service Tests

```bash
# Run all service tests
./mvnw test -Dtest="*ServiceTest"

# Run specific service test
./mvnw test -Dtest=UserServiceTest

# Run service tests with coverage
./mvnw test -Dtest="*ServiceTest" jacoco:report
```

### Repository Layer Testing

Repository tests use `@DataJpaTest` for testing JPA repositories with an in-memory database.

#### Example: UserRepository Test

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should find user by email")
    void findByEmail_WithExistingEmail_ShouldReturnUser() {
        // Given
        User user = new User("test@example.com", "testuser", "password", "John", "Doe");
        entityManager.persistAndFlush(user);

        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should return empty when email not found")
    void findByEmail_WithNonExistingEmail_ShouldReturnEmpty() {
        // When
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(found).isEmpty();
    }
}
```

#### Running Repository Tests

```bash
# Run all repository tests
./mvnw test -Dtest="*RepositoryTest"

# Run specific repository test
./mvnw test -Dtest=UserRepositoryTest
```

## 🔗 Integration Testing

### Controller Layer Testing

Controller tests use `@WebMvcTest` to test REST endpoints with mocked services.

#### Example: AuthController Test

```java
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should return tokens on successful login")
    void login_WithValidCredentials_ShouldReturnTokens() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", "password");
        User user = new User("test@example.com", "testuser", "password");
        AuthenticationResult authResult = new AuthenticationResult("accessToken", "refreshToken", user);

        when(authenticationService.authenticate("testuser", "password"))
            .thenReturn(authResult);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("accessToken"))
            .andExpect(jsonPath("$.refreshToken").value("refreshToken"))
            .andExpect(jsonPath("$.user.email").value("test@example.com"));

        verify(authenticationService).authenticate("testuser", "password");
    }

    @Test
    @DisplayName("Should return 401 on invalid credentials")
    void login_WithInvalidCredentials_ShouldReturn401() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");

        when(authenticationService.authenticate("testuser", "wrongpassword"))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid username or password"));

        verify(authenticationService).authenticate("testuser", "wrongpassword");
    }
}
```

#### Running Controller Tests

```bash
# Run all controller tests
./mvnw test -Dtest="*ControllerTest"

# Run specific controller test
./mvnw test -Dtest=AuthControllerTest
```

### Security Testing

Security tests verify authentication and authorization mechanisms.

#### Example: Security Configuration Test

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SecurityConfigurationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Should allow access to public endpoints")
    void publicEndpoints_ShouldBeAccessible() {
        // When & Then
        ResponseEntity<String> response = restTemplate.getForEntity("/api/health", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Should deny access to protected endpoints without token")
    void protectedEndpoints_WithoutToken_ShouldReturn401() {
        // When & Then
        ResponseEntity<String> response = restTemplate.getForEntity("/api/users/profile", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
```

## 📊 Test Coverage

### Generate Coverage Reports

```bash
# Generate JaCoCo coverage report
./mvnw test jacoco:report

# Generate coverage report with specific goals
./mvnw test jacoco:report -Djacoco.minimum.coverage=80

# Generate coverage report for specific packages
./mvnw test jacoco:report -Djacoco.includes="com.authbase.service.*"
```

### Coverage Configuration

The JaCoCo plugin is configured in `pom.xml`:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <rules>
            <rule>
                <element>BUNDLE</element>
                <limits>
                    <limit>
                        <counter>LINE</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.80</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</plugin>
```

### View Coverage Reports

After running tests with coverage, view the report at:

```
target/site/jacoco/index.html
```

## 🧹 Test Data Management

### Test Data Setup

Use `@BeforeEach` and `@AfterEach` for test data setup and cleanup:

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "testuser", "password", "John", "Doe");
        testUser.setId(1L);
        testUser.setEnabled(true);
    }

    @AfterEach
    void tearDown() {
        // Clean up any test data if needed
    }
}
```

### Test Data Builders

Create test data builders for consistent test data:

```java
public class UserTestBuilder {
    private String email = "test@example.com";
    private String username = "testuser";
    private String password = "password";
    private String firstName = "John";
    private String lastName = "Doe";
    private boolean enabled = true;
    private boolean emailVerified = false;

    public UserTestBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserTestBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public User build() {
        User user = new User(email, username, password, firstName, lastName);
        user.setEnabled(enabled);
        user.setEmailVerified(emailVerified);
        return user;
    }
}
```

## 🔄 Test Execution Strategies

### Parallel Test Execution

Enable parallel test execution for faster test runs:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <parallel>methods</parallel>
        <threadCount>4</threadCount>
        <perCoreThreadCount>true</perCoreThreadCount>
    </configuration>
</plugin>
```

### Test Categories

Organize tests by categories for selective execution:

```java
@Tag("unit")
@Tag("service")
class UserServiceTest {
    // Unit tests for UserService
}

@Tag("integration")
@Tag("controller")
class AuthControllerTest {
    // Integration tests for AuthController
}
```

Run tests by category:

```bash
# Run unit tests only
./mvnw test -Dgroups="unit"

# Run integration tests only
./mvnw test -Dgroups="integration"

# Run service tests only
./mvnw test -Dgroups="service"
```

## 🚨 Common Testing Issues

### Database Connection Issues

If you encounter database connection issues:

```bash
# Clean and rebuild
./mvnw clean test

# Use specific database profile
./mvnw test -Dspring.profiles.active=test

# Check database configuration
./mvnw test -Dspring.datasource.url=jdbc:h2:mem:testdb
```

### Memory Issues

For memory-intensive tests:

```bash
# Increase JVM memory
./mvnw test -Xmx2g

# Run tests with garbage collection
./mvnw test -XX:+UseG1GC
```

### Test Timeout Issues

If tests are timing out:

```bash
# Increase test timeout
./mvnw test -Dsurefire.timeout=300

# Run tests with debug output
./mvnw test -X
```

## 📈 Performance Testing

### Load Testing

For performance testing, use tools like Apache JMeter or Gatling:

```bash
# Run performance tests
./mvnw gatling:test

# Run specific performance test
./mvnw gatling:test -Dgatling.simulationClass=AuthLoadTest
```

### Benchmark Testing

Create benchmark tests to measure performance:

```java
@Benchmark
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public void benchmarkUserRegistration() {
    userService.registerUser("benchmark@test.com", "benchmarkuser", "password");
}
```

## 🔍 Debugging Tests

### Debug Mode

Run tests in debug mode:

```bash
# Run tests with debug output
./mvnw test -X

# Run specific test with debug
./mvnw test -Dtest=UserServiceTest -X

# Run tests with remote debugging
./mvnw test -Dmaven.surefire.debug
```

### Test Logging

Configure test logging in `src/test/resources/logback-test.xml`:

```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <logger name="com.authbase" level="DEBUG"/>
    <logger name="org.springframework.security" level="DEBUG"/>

    <root level="INFO">
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
```

## 📚 Best Practices

### Test Organization

1. **Arrange-Act-Assert**: Structure tests with clear sections
2. **Descriptive Names**: Use descriptive test method names
3. **Single Responsibility**: Each test should test one thing
4. **Independent Tests**: Tests should not depend on each other

### Test Data

1. **Use Builders**: Create test data builders for consistency
2. **Minimal Data**: Use only necessary data for each test
3. **Clean State**: Ensure clean state between tests
4. **Realistic Data**: Use realistic test data

### Assertions

1. **Specific Assertions**: Use specific assertions over generic ones
2. **Meaningful Messages**: Provide meaningful assertion messages
3. **Multiple Assertions**: Group related assertions together
4. **Custom Matchers**: Create custom matchers for complex assertions

### Mocking

1. **Mock Dependencies**: Mock external dependencies
2. **Verify Interactions**: Verify mock interactions
3. **Stub Behavior**: Stub mock behavior appropriately
4. **Avoid Over-Mocking**: Don't mock everything

## 📋 Test Checklist

Before committing code, ensure:

- [ ] All tests pass
- [ ] Test coverage meets minimum requirements (80%)
- [ ] New functionality has corresponding tests
- [ ] Tests are properly organized and named
- [ ] Test data is realistic and minimal
- [ ] Assertions are specific and meaningful
- [ ] Mocks are used appropriately
- [ ] Integration tests cover critical paths
- [ ] Performance tests for critical operations
- [ ] Security tests for authentication/authorization

## 🔗 Related Documentation

- [Service Layer Implementation](SERVICE-LAYER-IMPLEMENTATION.md) - Service layer testing strategies
- [Interface-Based Service Layer](INTERFACE-BASED-SERVICE-LAYER.md) - Interface testing patterns
- [Security Configuration](SECURITY-CONFIGURATION.md) - Security testing approaches
- [Controller Layer](CONTROLLER-LAYER.md) - Controller testing examples
- [Repository Layer](REPOSITORY-LAYER.md) - Repository testing patterns
