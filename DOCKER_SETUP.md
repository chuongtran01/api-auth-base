# Docker Setup with Redis

This project includes a Docker Compose configuration for running the authentication system with MySQL and Redis.

## Services

### MySQL Database

- **Image**: `mysql:8.0`
- **Port**: `3306`
- **Container**: `authbase-mysql`
- **Database**: `authbase`
- **Username**: `authbase_user`
- **Password**: `authbase_password`

### Redis Cache

- **Image**: `redis:7-alpine`
- **Port**: `6379`
- **Container**: `authbase-redis`
- **Password**: `redis_password`
- **Persistence**: Enabled with AOF (Append Only File)

## Quick Start

### 1. Start Services

```bash
docker-compose up -d
```

### 2. Check Service Status

```bash
docker-compose ps
```

### 3. View Logs

```bash
# All services
docker-compose logs

# Specific service
docker-compose logs redis
docker-compose logs mysql
```

### 4. Stop Services

```bash
docker-compose down
```

## Configuration

### Environment Variables

The Docker Compose setup uses a `docker.env` file to externalize configuration:

```bash
# MySQL Configuration
MYSQL_ROOT_PASSWORD=password
MYSQL_DATABASE=authbase
MYSQL_USER=authbase_user
MYSQL_PASSWORD=authbase_password

# Redis Configuration
REDIS_PASSWORD=redis_password

# Spring Boot Configuration
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_DATABASE=0
```

**Benefits of using `docker.env`:**

- **Environment-specific configuration**: Easy to switch between dev/staging/prod
- **Security**: Keep sensitive data out of version control
- **Flexibility**: Modify configuration without changing compose files
- **Team collaboration**: Each developer can have their own `.env` file

### Redis Configuration

The Redis service is configured with:

- **Persistence**: AOF (Append Only File) enabled for data durability
- **Authentication**: Password protected with `redis_password`
- **Health Check**: Automatic health monitoring
- **Network**: Connected to `authbase-network`

## Spring Boot Configuration

Add the following to your `application.yml` or `application.properties`:

```yaml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: ${REDIS_DATABASE:0}
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
```

## Redis Usage in the Application

The Redis service is used for:

- **Token Blacklisting**: Storing blacklisted JWT tokens
- **Session Management**: Managing user sessions
- **Caching**: Application-level caching

## Troubleshooting

### Redis Connection Issues

1. Check if Redis container is running:

   ```bash
   docker-compose ps redis
   ```

2. Check Redis logs:

   ```bash
   docker-compose logs redis
   ```

3. Test Redis connection:
   ```bash
   docker exec -it authbase-redis redis-cli -a redis_password ping
   ```

### Data Persistence

- Redis data is persisted in the `redis_data` volume
- Data survives container restarts
- To reset Redis data: `docker-compose down -v && docker-compose up -d`

### Network Issues

- All services are connected via `authbase-network`
- Service names can be used as hostnames within the Docker network
- External access is available on localhost for development

## Environment Management

### Development

- Use the provided `docker.env` file
- Default passwords for simplicity
- Exposed ports for direct access

### Production

- Create a production-specific `.env` file
- Use strong passwords and secrets management
- Consider using Docker secrets or external secret management
- Disable port exposure for security

### Custom Environment

To use a different environment file:

```bash
# Use a custom env file
docker-compose --env-file production.env up -d

# Or set environment variables directly
MYSQL_ROOT_PASSWORD=prod_password docker-compose up -d
```

## Security Notes

- The `docker.env` file contains sensitive information
- Consider adding `docker.env` to `.gitignore` for production
- Use Docker secrets or external secret management in production environments
