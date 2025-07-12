# H2 Database Quick Reference

## 🚀 **Quick Start**

### 1. Start Application

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 2. Access H2 Console

- **URL**: http://localhost:8080/api/h2-console
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: `password`

## 🔍 **Common H2 Queries**

### View Database Info

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

### Data Management

```sql
-- Insert data
INSERT INTO users (email, username, password) VALUES ('test@example.com', 'testuser', 'password');

-- Update data
UPDATE users SET email = 'new@example.com' WHERE username = 'testuser';

-- Delete data
DELETE FROM users WHERE username = 'testuser';

-- Clear all data
TRUNCATE TABLE users;
```

### Export/Import

```sql
-- Export to SQL file
SCRIPT TO 'export.sql';

-- Export to CSV
CALL CSVWRITE('users.csv', 'SELECT * FROM users');

-- Import from CSV
CALL CSVREAD('users.csv', 'ID,EMAIL,USERNAME', 'fieldSeparator=,');
```

## 🛠️ **Generate BCrypt Passwords**

### Using Java Utility

```bash
# Compile and run
javac -cp "target/classes:$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q)" src/main/java/com/authbase/util/PasswordGenerator.java
java -cp "target/classes:$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q)" com.authbase.util.PasswordGenerator admin123
```

### Using Online Tools

- BCrypt Generator: https://bcrypt-generator.com/
- Spring Security BCrypt: https://www.devglan.com/online-tools/bcrypt-hash-generator

## 📝 **Sample Data Setup**

### Enable Data Initialization

Add to `application-dev.yml`:

```yaml
spring:
  jpa:
    defer-datasource-initialization: true
  sql:
    init:
      mode: always
      data-locations: classpath:data.sql
```

### Sample Users

```sql
-- Generate password first using PasswordGenerator utility
INSERT INTO users (email, username, password, first_name, last_name, is_enabled)
VALUES
('admin@example.com', 'admin', '$2a$10$...', 'Admin', 'User', true),
('user@example.com', 'user', '$2a$10$...', 'Regular', 'User', true);
```

## 🔧 **Troubleshooting**

### Common Issues

1. **H2 Console not accessible**

   - Check if application is running
   - Verify path: `/api/h2-console`
   - Check if H2 console is enabled

2. **Data not persisting**

   - In-memory H2 loses data on restart
   - Use file-based H2 for persistence: `jdbc:h2:file:./data/authbase`

3. **Connection refused**
   - Check if application is running on port 8080
   - Verify JDBC URL: `jdbc:h2:mem:testdb`

### Debug Commands

```bash
# Check application health
curl http://localhost:8080/api/actuator/health

# Check H2 console
curl http://localhost:8080/api/h2-console

# View logs
tail -f logs/application.log
```

## 📊 **H2 Console Features**

### Main Features

- **Execute SQL**: Run queries directly
- **Browse Tables**: View table structure and data
- **Import/Export**: CSV data import/export
- **Schema Browser**: Explore database schema
- **Backup/Restore**: Database backup and restore

### Keyboard Shortcuts

- **Ctrl+Enter**: Execute query
- **Ctrl+Up/Down**: Navigate query history
- **Ctrl+Shift+Up/Down**: Navigate result sets

## 🎯 **Best Practices**

### Development

- Use in-memory H2 for fast development
- Enable H2 console for debugging
- Use data.sql for initial data setup

### Testing

- Use separate H2 instance for tests
- Clean data between tests
- Use @Transactional for test isolation

### Security

- Disable H2 console in production
- Use strong passwords
- Limit network access to H2 console
