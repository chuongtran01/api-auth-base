package com.authbase.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI configuration for Swagger UI with Bearer token authentication.
 * Provides comprehensive API documentation with security scheme support.
 */
@Configuration
public class OpenApiConfig {

  /**
   * Configure OpenAPI with Bearer token authentication and comprehensive
   * documentation.
   * 
   * @return OpenAPI configuration
   */
  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(apiInfo())
        .servers(servers())
        .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
        .components(new Components()
            .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
  }

  /**
   * Create API information with comprehensive details.
   * 
   * @return API info configuration
   */
  private Info apiInfo() {
    return new Info()
        .title("API Auth Base")
        .description("""
            A comprehensive JWT authentication system built with Spring Boot 3.x.

            ## Features
            - **JWT Authentication**: Secure token-based authentication
            - **Role-based Access Control**: Multiple roles per user
            - **User Management**: Complete user lifecycle management
            - **Security Features**: Rate limiting, password policies, account lockout
            - **Redis Integration**: Token blacklisting and session management

            ## Authentication
            Most endpoints require Bearer token authentication. Include your JWT token in the Authorization header:
            ```
            Authorization: Bearer your-jwt-token-here
            ```

            ## Getting Started
            1. Register a new user using `/api/auth/register`
            2. Login using `/api/auth/login` to get your JWT tokens
            3. Use the access token in the Authorization header for protected endpoints
            4. Refresh your token using `/api/auth/refresh` when it expires

            ## Error Codes
            - `401 Unauthorized`: Invalid or missing authentication token
            - `403 Forbidden`: Insufficient permissions for the requested resource
            - `400 Bad Request`: Invalid request data or validation errors
            - `404 Not Found`: Requested resource not found
            - `500 Internal Server Error`: Server-side error
            """)
        .version("0.0.1-SNAPSHOT")
        .contact(new Contact()
            .name("API Auth Base Team")
            .email("support@authbase.com")
            .url("https://github.com/chuongtran01/api-auth-base"))
        .license(new License()
            .name("MIT License")
            .url("https://opensource.org/licenses/MIT"));
  }

  /**
   * Configure server information for different environments.
   * 
   * @return List of server configurations
   */
  private List<Server> servers() {
    return List.of(
        new Server()
            .url("http://localhost:8080/api")
            .description("Development Server"),
        new Server()
            .url("https://api.authbase.com/api")
            .description("Production Server"));
  }

  /**
   * Create Bearer token security scheme for JWT authentication.
   * 
   * @return Security scheme configuration
   */
  private SecurityScheme createAPIKeyScheme() {
    return new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .bearerFormat("JWT")
        .scheme("bearer")
        .description("""
            JWT Bearer token for authentication.

            **How to use:**
            1. Login using `/api/auth/login` to get your access token
            2. Include the token in the Authorization header:
               ```
               Authorization: Bearer your-jwt-token-here
               ```
            3. The token will be automatically validated for each request

            **Token Expiration:**
            - Access tokens expire after 15 minutes
            - Use `/api/auth/refresh` to get a new access token
            - Refresh tokens expire after 7 days
            """);
  }
}