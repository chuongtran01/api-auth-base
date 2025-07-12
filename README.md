# Spring Boot JWT Authentication Base

A reusable JWT authentication backend built with Spring Boot 3.x that provides a complete authentication and authorization system.

## 🚀 Features

- **JWT-based Authentication**: Secure token-based authentication
- **Role-based Access Control**: Flexible permission system with **multiple roles per user**
- **User Management**: Complete user lifecycle management
- **Security Features**: Rate limiting, password policies, account lockout
- **API Documentation**: OpenAPI/Swagger integration
- **Database Support**: MySQL (development/production) and H2 (testing)
- **Database Migrations**: Liquibase for version-controlled schema management
- **Migration Format**: SQL files for direct database control and better readability
- **Entity Models**: Complete JPA entities for user management and role-based access control
- **Dependency Injection**: Constructor injection for better testability and immutability
- **Environment Profiles**: Separate configurations for dev, test, and prod
- **Data Persistence**: Development data persists between application restarts

## 🎯 **Multiple Roles Per User**

This system supports **multiple roles per user**, which provides exceptional flexibility for complex business scenarios:

### **Key Benefits:**

- **🔧 Granular Permission Management**: Combine focused roles instead of creating complex monolithic roles
- **🏢 Real-World Applicability**: Perfect for employees with multiple responsibilities
- **⚡ Flexible Role Assignment**: Easy to add/remove capabilities without affecting other permissions
- **🔒 Security**: Follows principle of least privilege with precise permission control

### **Example Use Cases:**

```java
// Senior Sales Representative with multiple responsibilities
User john = new User("john@company.com", "john", "password");
john.addRole(employeeRole);     // Basic employee access
john.addRole(salesRole);        // Sales operations
john.addRole(supportRole);      // Customer support
john.addRole(reportingRole);    // Analytics and reporting

// John now has ALL permissions from ALL roles combined
// Total: 15+ permissions from 4 different roles
```

### **Permission Calculation:**

```java
// Check permissions across all roles
boolean canProcessOrders = PermissionUtil.hasPermission(user, "SALES_PROCESS_ORDER");
boolean canHandleSupport = PermissionUtil.hasPermission(user, "SUPPORT_VIEW_TICKET");
boolean isAdmin = PermissionUtil.hasRole(user, "ADMIN");

// Get all permissions from all roles
Set<Permission> allPermissions = PermissionUtil.getAllUserPermissions(user);
```

📚 **For detailed examples and business scenarios, see:** [`docs/MULTIPLE-ROLES-EXAMPLES.md`](docs/MULTIPLE-ROLES-EXAMPLES.md)

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Security**: Spring Security + JWT
- **Database**: MySQL (dev/prod) + H2 (test)
- **ORM**: Spring Data JPA + Hibernate
- **Migrations**: Liquibase
- **Validation**: Bean Validation
- **Testing**: JUnit 5 + Mockito + TestContainers
- **Documentation**: OpenAPI/Swagger
- **Build Tool**: Maven

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MySQL (for development and production)
- Docker & Docker Compose (optional, for easy database setup)

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd api-auth-base
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run the Application

#### Option A: Development Mode (MySQL Database)

```bash
# Start MySQL database using Docker Compose
docker-compose up -d

# Run the application with development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Option B: Testing Mode (H2 Database)

```bash
# Run tests with H2 in-memory database
mvn test -Dspring-boot.run.profiles=test
```

### 4. Access the Application

- **API Base URL**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **phpMyAdmin**: http://localhost:8081 (when using Docker Compose)

## 🔧 Configuration

### Environment Profiles

The application supports three profiles:

- **dev**: Development environment with MySQL database
- **test**: Testing environment with H2 in-memory database
- **prod**: Production environment with MySQL database

### Environment Variables (Production)

Set these environment variables for production:

```bash
export DATABASE_URL=jdbc:mysql://localhost:3306/authbase?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
export DATABASE_USERNAME=your_username
export DATABASE_PASSWORD=your_password
export JWT_SECRET=your-very-long-and-secure-secret-key
```

### Docker Compose Setup

The project includes a `docker-compose.yml` file for easy MySQL setup:

```bash
# Start MySQL database
docker-compose up -d

# Stop services
docker-compose down

# Stop and remove volumes (data will be lost)
docker-compose down -v
```

**Docker Compose Services:**

- **MySQL 8.0**: Database server on port 3306

**Default Credentials:**

- MySQL Root: `root` / `password`
- Database: `authbase`

**Database Management:**
You can connect to the MySQL database using:

- **Command Line**: `mysql -h localhost -P 3306 -u root -p`
- **MySQL Workbench**: Connect to `localhost:3306`
- **DBeaver**: Connect to `localhost:3306`
- **IntelliJ IDEA**: Database tools

### JWT Configuration

Configure JWT settings in `application.yml`:

```yaml
jwt:
  secret: your-secret-key-here
  access-token-expiration: 900000 # 15 minutes
  refresh-token-expiration: 604800000 # 7 days
```

## 📚 API Endpoints

### Authentication

- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Refresh token
- `POST /api/auth/logout` - User logout
- `POST /api/auth/forgot-password` - Password reset request
- `POST /api/auth/reset-password` - Password reset
- `POST /api/auth/verify-email` - Email verification

### User Management

- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update user profile
- `PUT /api/users/password` - Change password
- `DELETE /api/users/account` - Delete account

## 🧪 Testing

### Run Tests

```bash
# Run all tests with H2 database
mvn test -Dspring-boot.run.profiles=test

# Run specific test
mvn test -Dtest=UserServiceTest -Dspring-boot.run.profiles=test
```

### Test Coverage

```bash
mvn jacoco:report
```

## 🐳 Docker Support

### Build Docker Image

```bash
docker build -t api-auth-base .
```

### Run with Docker

```bash
docker run -p 8080:8080 api-auth-base
```

### Docker Compose (with MySQL)

```bash
docker-compose up -d
```

## 📦 Integration

### As a Dependency

Add this project as a dependency to your Spring Boot application:

```xml
<dependency>
    <groupId>com.authbase</groupId>
    <artifactId>api-auth-base</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Configuration Properties

Customize the authentication base using configuration properties:

```yaml
auth:
  jwt:
    secret: your-secret
    access-token-expiration: 900000
    refresh-token-expiration: 604800000
  password:
    min-length: 8
    require-uppercase: true
    require-lowercase: true
    require-numbers: true
    require-special-chars: true
  rate-limiting:
    login-attempts: 5
    lockout-duration: 300000
```

## 🔒 Security Features

- **JWT Token Management**: Secure token generation and validation
- **Password Hashing**: BCrypt password encryption
- **Rate Limiting**: Protection against brute force attacks
- **Account Lockout**: Automatic lockout after failed attempts
- **CORS Configuration**: Cross-origin resource sharing setup
- **Security Headers**: Protection against common vulnerabilities
- **Input Validation**: Comprehensive request validation

## 📝 Development

### Project Structure

```
src/main/java/com/authbase/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── dto/            # Data Transfer Objects
├── entity/         # JPA entities
├── repository/     # Data access layer
├── service/        # Business logic
├── security/       # Security configuration
└── util/           # Utility classes
```

### Adding New Features

1. Create entity classes in `entity/` package
2. Add repositories in `repository/` package
3. Implement business logic in `service/` package
4. Create controllers in `controller/` package
5. Add DTOs in `dto/` package
6. Write tests in `src/test/` directory

### Coding Standards

This project follows Spring Boot best practices:

- **Constructor Injection**: Use constructor injection instead of `@Autowired` for better testability
- **Immutable Dependencies**: Mark dependency fields as `final`
- **SQL Migrations**: Use SQL files for database migrations (see `docs/SQL-MIGRATIONS-BENEFITS.md`)
- **Configuration Properties**: Use `@ConfigurationProperties` for externalized configuration
- **Comprehensive Testing**: Write unit and integration tests for all new features

For detailed guidelines, see:

- `docs/CONSTRUCTOR-INJECTION.md` - Constructor injection patterns
- `docs/SQL-MIGRATIONS-BENEFITS.md` - SQL migration benefits
- `docs/LIQUIBASE-CONFIGURATION.md` - Database migration guide

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:

- Create an issue in the repository
- Check the [Wiki](wiki-url) for detailed documentation
- Review the API documentation at `/api/swagger-ui.html`

## 🔄 Changelog

### Version 0.0.1-SNAPSHOT

- Initial release
- Basic JWT authentication
- User management
- Role-based access control
- API documentation
