# SQL Migrations Benefits

## 🎯 **Why SQL Files Instead of YAML?**

This project uses **SQL files** for database migrations instead of YAML files. Here's why this approach is better:

## ✅ **Advantages of SQL Files**

### **1. Direct SQL Control**

```sql
-- You write exactly what you want
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

**vs YAML**:

```yaml
- createTable:
    tableName: users
    columns:
      - column:
          name: id
          type: BIGINT
          autoIncrement: true
          constraints:
            primaryKey: true
      # ... more verbose YAML
```

### **2. Better Readability**

- **SQL**: Familiar syntax for database developers
- **YAML**: More verbose and harder to read for complex queries
- **SQL**: Direct representation of what gets executed
- **YAML**: Abstracted representation that gets converted

### **3. Database-Specific Features**

```sql
-- MySQL-specific features work directly
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Complex queries and joins
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ADMIN' AND p.name IN ('USER_READ', 'USER_WRITE');
```

### **4. Easier Debugging**

- **SQL**: Can test directly in database client
- **YAML**: Must understand Liquibase syntax
- **SQL**: Error messages are familiar
- **YAML**: Errors might be in Liquibase conversion

### **5. Version Control Friendly**

- **SQL**: Clear diffs showing exact changes
- **YAML**: Diffs show Liquibase structure changes
- **SQL**: Easy to review database changes
- **YAML**: Harder to understand actual SQL impact

## 📁 **File Structure Comparison**

### **SQL Approach (Current)**

```
src/main/resources/db/
├── changelog/
│   └── db.changelog-master.yaml    # References SQL files
└── migration/
    ├── V1__Create_initial_schema.sql
    └── V2__Insert_initial_data.sql
```

### **YAML Approach (Previous)**

```
src/main/resources/db/changelog/
├── db.changelog-master.yaml
└── changes/
    ├── 001-initial-schema.yaml
    └── 002-insert-initial-data.yaml
```

## 🛠️ **Migration Examples**

### **Adding a New Table (SQL)**

```sql
-- V3__Add_user_profiles.sql
CREATE TABLE user_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    bio TEXT,
    avatar_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
```

### **Adding a New Table (YAML)**

```yaml
databaseChangeLog:
  - changeSet:
      id: 003
      author: developer
      comment: Add user profiles table

      - createTable:
          tableName: user_profiles
          columns:
            - column:
                name: id
                type: BIGINT
                autoIncrement: true
                constraints:
                  primaryKey: true
            - column:
                name: user_id
                type: BIGINT
                constraints:
                  nullable: false
                  unique: true
                  foreignKeyName: fk_user_profiles_user_id
                  references: users(id)
            # ... more columns

      - createIndex:
          tableName: user_profiles
          indexName: idx_user_profiles_user_id
          columns:
            - column:
                name: user_id
```

## 🔧 **Advanced SQL Features**

### **Complex Data Migrations**

```sql
-- V4__Update_user_permissions.sql
-- Add new permission
INSERT INTO permissions (name, description)
VALUES ('PROFILE_EDIT', 'Edit user profiles');

-- Assign to existing roles
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name IN ('ADMIN', 'MODERATOR')
  AND p.name = 'PROFILE_EDIT';

-- Update existing data
UPDATE users
SET is_email_verified = TRUE
WHERE email LIKE '%@example.com';
```

### **Database-Specific Optimizations**

```sql
-- V5__Optimize_performance.sql
-- Add composite indexes
CREATE INDEX idx_users_email_username ON users(email, username);

-- Add covering index for common queries
CREATE INDEX idx_user_roles_user_role ON user_roles(user_id, role_id);

-- Partition large tables (MySQL 8.0+)
ALTER TABLE refresh_tokens
PARTITION BY RANGE (YEAR(created_at)) (
    PARTITION p2023 VALUES LESS THAN (2024),
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);
```

## 🚀 **Best Practices**

### **1. Naming Convention**

```
V{version}__{description}.sql
```

- `V1__Create_initial_schema.sql`
- `V2__Insert_initial_data.sql`
- `V3__Add_user_profiles.sql`

### **2. File Organization**

- Keep migrations in `src/main/resources/db/migration/`
- Use descriptive names
- Include version numbers for ordering

### **3. SQL Best Practices**

```sql
-- Always include comments
-- V3__Add_user_profiles.sql
-- This migration adds user profile functionality

-- Use consistent formatting
CREATE TABLE user_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    bio TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Add indexes for performance
CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
```

### **4. Testing Migrations**

```bash
# Generate SQL without executing
mvn liquibase:updateSQL

# Test in development first
mvn spring-boot:run -Dspring.profiles.active=dev

# Check migration status
mvn liquibase:status
```

## 📊 **Comparison Summary**

| Aspect                | SQL Files       | YAML Files  |
| --------------------- | --------------- | ----------- |
| **Readability**       | ✅ Excellent    | ❌ Verbose  |
| **Debugging**         | ✅ Easy         | ❌ Complex  |
| **Database Features** | ✅ Full Support | ⚠️ Limited  |
| **Version Control**   | ✅ Clear Diffs  | ❌ Abstract |
| **Learning Curve**    | ✅ Low          | ❌ High     |
| **Direct Testing**    | ✅ Yes          | ❌ No       |
| **Performance**       | ✅ Native       | ⚠️ Overhead |

## 🎯 **Conclusion**

Using **SQL files** for database migrations provides:

- **Better developer experience**
- **Easier debugging and testing**
- **Full database feature support**
- **Clearer version control history**
- **Lower learning curve**

This approach makes database migrations more accessible and maintainable for teams working with Spring Boot applications.
