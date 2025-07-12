# Liquibase Configuration Guide

## 🎯 **Overview**

This project uses **Liquibase** for database schema management and migrations. Liquibase provides version-controlled database changes that are applied consistently across all environments.

## 🔧 **Key Benefits**

✅ **Version Control**: Database changes are tracked in version control  
✅ **Consistency**: Same schema across all environments  
✅ **Data Persistence**: Data survives application restarts in development  
✅ **Rollback Support**: Ability to rollback database changes  
✅ **Team Collaboration**: Multiple developers can work on schema changes

## 📁 **File Structure**

```
src/main/resources/
└── db/
    ├── changelog/
    │   └── db.changelog-master.yaml    # Main changelog file
    └── migration/
        ├── V1__Create_initial_schema.sql    # Initial database schema
        └── V2__Insert_initial_data.sql      # Initial data insertion
```

## 📝 **SQL File Naming Convention**

SQL migration files follow this naming pattern:

```
V{version}__{description}.sql
```

**Examples**:

- `V1__Create_initial_schema.sql` - Version 1, creates initial schema
- `V2__Insert_initial_data.sql` - Version 2, inserts initial data
- `V3__Add_user_profile.sql` - Version 3, adds user profile table
- `V4__Update_user_permissions.sql` - Version 4, updates permissions

**Rules**:

- ✅ Use `V` prefix followed by version number
- ✅ Use double underscore `__` as separator
- ✅ Use descriptive name in lowercase with underscores
- ✅ Always use `.sql` extension
- ✅ Version numbers must be sequential

## 🔄 **How It Works**

### **Before Liquibase (Hibernate DDL Auto)**

```yaml
# Old way - data lost on restart
jpa:
  hibernate:
    ddl-auto: create-drop # Tables dropped on shutdown
```

### **After Liquibase**

```yaml
# New way - data persists
jpa:
  hibernate:
    ddl-auto: validate # Only validates schema, doesn't change it

liquibase:
  enabled: true
  change-log: classpath:db/changelog/db.changelog-master.yaml
```

## 🚀 **Configuration by Environment**

### **Development Environment**

```yaml
# application-dev.yml
spring:
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.yaml
    contexts: dev
    default-schema: authbase
```

**Features**:

- ✅ Data persists between application restarts
- ✅ MySQL database with full features
- ✅ Detailed logging for debugging

### **Production Environment**

```yaml
# application-prod.yml
spring:
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.yaml
    contexts: prod
    default-schema: authbase
```

**Features**:

- ✅ Safe schema validation only
- ✅ Environment variable configuration
- ✅ Performance optimized

### **Test Environment**

```yaml
# application-test.yml
spring:
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.yaml
    contexts: test
```

**Features**:

- ✅ H2 in-memory database
- ✅ Fast test execution
- ✅ Isolated test data

## 📊 **Database Schema**

### **Tables Created**

1. **`users`** - User accounts and authentication
2. **`roles`** - User roles (USER, ADMIN, MODERATOR)
3. **`permissions`** - System permissions
4. **`user_roles`** - Many-to-many relationship between users and roles
5. **`role_permissions`** - Many-to-many relationship between roles and permissions
6. **`refresh_tokens`** - JWT refresh token storage

### **Initial Data**

**Default Roles**:

- `USER` - Standard user role
- `ADMIN` - Full administrative access
- `MODERATOR` - Limited administrative access

**Default Permissions**:

- `USER_READ`, `USER_WRITE`, `USER_DELETE`
- `ROLE_READ`, `ROLE_WRITE`, `ROLE_DELETE`
- `ADMIN_ACCESS`

## 🛠️ **Working with Liquibase**

### **Running the Application**

```bash
# Development (default)
./mvnw spring-boot:run

# Production
./mvnw spring-boot:run -Dspring.profiles.active=prod

# Test
./mvnw spring-boot:run -Dspring.profiles.active=test
```

### **What Happens on Startup**

1. **First Run**: Liquibase creates all tables and inserts initial data
2. **Subsequent Runs**: Liquibase validates schema and skips already-applied changes
3. **Data Persistence**: Your data remains between restarts

### **Adding New Migrations**

1. **Create new SQL migration file**:

```sql
-- src/main/resources/db/migration/V3__Add_user_profile.sql
-- Add user profile table
CREATE TABLE user_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    bio TEXT,
    avatar_url VARCHAR(500),
    date_of_birth DATE,
    phone_number VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Add index for better performance
CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);

-- Insert some default profile data (optional)
INSERT INTO user_profiles (user_id, bio)
SELECT id, 'Welcome to our platform!'
FROM users
WHERE id IN (SELECT user_id FROM user_roles WHERE role_id = (SELECT id FROM roles WHERE name = 'ADMIN'));
```

2. **Include in master changelog**:

```yaml
# src/main/resources/db/changelog/db.changelog-master.yaml
databaseChangeLog:
  - include:
      file: db/migration/V1__Create_initial_schema.sql
  - include:
      file: db/migration/V2__Insert_initial_data.sql
  - include:
      file: db/migration/V3__Add_user_profile.sql # New migration
```

3. **Test the migration**:

```bash
# Generate SQL without executing (dry run)
mvn liquibase:updateSQL

# Apply the migration
mvn spring-boot:run
```

### **Liquibase Commands**

```bash
# Check migration status
./mvnw liquibase:status

# Generate SQL without executing
./mvnw liquibase:updateSQL

# Rollback last change
./mvnw liquibase:rollback -Dliquibase.rollbackCount=1

# Generate changelog from existing database
./mvnw liquibase:diff
```

## 🔍 **Monitoring and Debugging**

### **Liquibase Logs**

When you start the application, you'll see:

```
INFO  - Liquibase: Successfully acquired change log lock
INFO  - Liquibase: Creating database history table with name: authbase.DATABASECHANGELOG
INFO  - Liquibase: Reading from authbase.DATABASECHANGELOG
INFO  - Liquibase: Running Changeset: db/changelog/changes/001-initial-schema.yaml::001::system
INFO  - Liquibase: Successfully released change log lock
```

### **Database History Table**

Liquibase creates a `DATABASECHANGELOG` table that tracks:

- Which changes have been applied
- When they were applied
- Who applied them
- Checksums for validation

### **Checking Applied Changes**

```sql
-- View applied changes
SELECT * FROM DATABASECHANGELOG ORDER BY DATEEXECUTED;

-- View change details
SELECT ID, AUTHOR, COMMENTS, DATEEXECUTED FROM DATABASECHANGELOG;
```

## 🚨 **Important Notes**

### **Development Workflow**

1. **Never modify existing changelog files** that have been applied to production
2. **Always create new changelog files** for schema changes
3. **Test migrations** in development before applying to production
4. **Use meaningful changeSet IDs** and comments

### **Data Persistence**

- ✅ **Development**: Data persists between restarts
- ✅ **Production**: Data is always preserved
- ⚠️ **Test**: Data is lost (H2 in-memory)

### **Schema Validation**

- Hibernate `ddl-auto: validate` ensures your entity classes match the database schema
- If they don't match, the application will fail to start
- This prevents runtime errors due to schema mismatches

## 🔧 **Troubleshooting**

### **Common Issues**

1. **"Schema validation failed"**

   - Check that entity classes match the database schema
   - Run `./mvnw liquibase:update` to apply pending migrations

2. **"ChangeSet already applied"**

   - This is normal - Liquibase skips already-applied changes
   - Check `DATABASECHANGELOG` table for applied changes

3. **"Cannot acquire change log lock"**
   - Another instance might be running
   - Check for running applications
   - Wait a few minutes for lock timeout

### **Reset Development Database**

If you need to start fresh in development:

```bash
# Drop and recreate database
mysql -u root -p
DROP DATABASE authbase;
CREATE DATABASE authbase;
exit

# Restart application - Liquibase will recreate everything
./mvnw spring-boot:run
```

## 📚 **Best Practices**

1. **Version Control**: Always commit changelog files
2. **Descriptive Comments**: Use meaningful changeSet comments
3. **Atomic Changes**: Keep each changeSet focused on one change
4. **Testing**: Test migrations in development first
5. **Backup**: Always backup production database before migrations
6. **Rollback**: Plan for rollback scenarios
7. **Documentation**: Document complex migrations

## 🎯 **Migration Strategy**

### **Development**

- Use `validate` mode for data persistence
- Create new changelog files for changes
- Test thoroughly before committing

### **Production**

- Always backup before migrations
- Use `validate` mode for safety
- Monitor migration execution
- Have rollback plan ready

### **Testing**

- Use H2 in-memory for fast tests
- Reset data between test runs
- Test migration scenarios
