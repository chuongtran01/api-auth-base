# H2 Database Data Management Guide (Testing Only)

> **Note**: H2 is now only used for **testing**. For development, use MySQL with Docker Compose.

## 🧪 **H2 for Testing**

H2 database is configured for the **test profile** to provide fast, isolated testing without external dependencies.

### Test Configuration

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: password
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: false
  h2:
    console:
      enabled: false # Disabled for tests
```

## 🚀 **Running Tests with H2**

### Run All Tests

```bash
mvn test -Dspring-boot.run.profiles=test
```

### Run Specific Test

```bash
mvn test -Dtest=UserServiceTest -Dspring-boot.run.profiles=test
```

### Run Integration Tests

```bash
mvn test -Dtest=*IntegrationTest -Dspring-boot.run.profiles=test
```

## 🖥️ **H2 Console (For Debugging Tests)**

### Enable H2 Console for Testing

If you need to debug test data, temporarily enable H2 console in `application-test.yml`:

```yaml
spring:
  h2:
    console:
      enabled: true
      path: /h2-console
```

### Access H2 Console During Tests

1. Start application with test profile: `mvn spring-boot:run -Dspring-boot.run.profiles=test`
2. Open browser: `http://localhost:8080/api/h2-console`
3. Use connection settings:
   - **JDBC URL**: `jdbc:h2:mem:testdb`
   - **Username**: `sa`
   - **Password**: `password`

## 📝 **Test Data Management**

### Test Data Initialization

Create test data in `src/test/resources/`:

```sql
-- src/test/resources/test-data.sql
INSERT INTO users (email, username, password, is_enabled)
VALUES
('test@example.com', 'testuser', '$2a$10$...', true);
```

### Test Configuration

```java
@TestConfiguration
public class TestDataConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("classpath:test-data.sql")
            .build();
    }
}
```

### Using @Transactional for Tests

```java
@SpringBootTest
@Transactional
class UserServiceTest {

    @Test
    void testCreateUser() {
        // Test data is automatically rolled back after test
    }
}
```

## 🔍 **Useful H2 Queries for Testing**

### View Test Data

```sql
-- Show all tables
SHOW TABLES;

-- Show table structure
DESCRIBE users;

-- Show table data
SELECT * FROM users;

-- Count records
SELECT COUNT(*) FROM users;
```

### Clean Test Data

```sql
-- Clear all data
TRUNCATE TABLE users;
TRUNCATE TABLE roles;

-- Or delete specific data
DELETE FROM users WHERE email = 'test@example.com';
```

## 🛠️ **Test Data Utilities**

### Generate BCrypt Passwords for Tests

```java
// In test classes
@Autowired
private PasswordEncoder passwordEncoder;

String encodedPassword = passwordEncoder.encode("testpassword");
```

### Test Data Builders

```java
public class TestDataBuilder {

    public static User createTestUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword("$2a$10$..."); // Use PasswordGenerator utility
        user.setEnabled(true);
        return user;
    }
}
```

## 🎯 **Best Practices for H2 Testing**

### Test Isolation

- ✅ Use `@Transactional` for automatic rollback
- ✅ Use `@DirtiesContext` when needed
- ✅ Clean up data between tests
- ✅ Use unique test data

### Performance

- ✅ Use in-memory H2 for fast tests
- ✅ Disable SQL logging in tests
- ✅ Use `create-drop` for clean state

### Data Management

- ✅ Use test data builders
- ✅ Generate BCrypt passwords properly
- ✅ Use meaningful test data
- ✅ Avoid hardcoded IDs

## 🔧 **Troubleshooting H2 Tests**

### Common Issues

1. **Test data not persisting**: Use `@Transactional` or manual cleanup
2. **Password encoding issues**: Use `PasswordEncoder` bean
3. **Database connection errors**: Check test profile configuration
4. **Data conflicts**: Use unique test data

### Debug Commands

```bash
# Run tests with debug logging
mvn test -Dspring-boot.run.profiles=test -Dlogging.level.com.authbase=DEBUG

# Run specific test with verbose output
mvn test -Dtest=UserServiceTest -Dspring-boot.run.profiles=test -X
```

## 📊 **H2 vs MySQL for Testing**

| Feature          | H2 (Testing)         | MySQL (Development)             |
| ---------------- | -------------------- | ------------------------------- |
| **Speed**        | ✅ Very fast         | ❌ Slower                       |
| **Setup**        | ✅ No setup required | ❌ Requires Docker/installation |
| **Isolation**    | ✅ Perfect isolation | ❌ Shared database              |
| **Realism**      | ❌ Different dialect | ✅ Matches production           |
| **Dependencies** | ✅ No external deps  | ❌ External database required   |

## 🚀 **Development vs Testing**

### Development (MySQL)

- **Database**: MySQL via Docker Compose
- **Data Persistence**: ✅ Data persists between restarts
- **Realism**: ✅ Matches production environment
- **Setup**: Requires `docker-compose up -d`

### Testing (H2)

- **Database**: H2 in-memory
- **Data Persistence**: ❌ Data lost after tests
- **Realism**: ❌ Different database dialect
- **Setup**: ✅ No setup required

This setup gives you the best of both worlds: realistic development environment with MySQL and fast, isolated testing with H2!
