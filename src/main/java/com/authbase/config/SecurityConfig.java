package com.authbase.config;

import com.authbase.security.JwtAccessDeniedHandler;
import com.authbase.security.JwtAuthenticationEntryPoint;
import com.authbase.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security Configuration for JWT-based authentication and authorization.
 * Configures authentication, authorization, CORS, and security filters.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
  private final UserDetailsService userDetailsService;
  private final CorsProperties corsProperties;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
      JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
      JwtAccessDeniedHandler jwtAccessDeniedHandler,
      UserDetailsService userDetailsService,
      CorsProperties corsProperties) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    this.userDetailsService = userDetailsService;
    this.corsProperties = corsProperties;
  }

  /**
   * Configure security filter chain with JWT authentication.
   * 
   * @param http HttpSecurity configuration
   * @return SecurityFilterChain
   * @throws Exception if configuration fails
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // Disable CSRF for JWT-based authentication
        .csrf(AbstractHttpConfigurer::disable)

        // Configure CORS
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))

        // Configure session management (stateless for JWT)
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // Configure security headers
        .headers(headers -> headers
            .addHeaderWriter(securityHeadersWriter())
            .addHeaderWriter(frameOptionsHeaderWriter())
            .addHeaderWriter(xssProtectionHeaderWriter())
            .addHeaderWriter(referrerPolicyHeaderWriter())
            .addHeaderWriter(contentSecurityPolicyHeaderWriter())
            .addHeaderWriter(permissionsPolicyHeaderWriter())
            .addHeaderWriter(hstsHeaderWriter())
            .addHeaderWriter(cacheControlHeaderWriter())
            .addHeaderWriter(pragmaHeaderWriter())
            .addHeaderWriter(expiresHeaderWriter()))

        // Configure authorization rules
        .authorizeHttpRequests(authz -> authz
            // Public endpoints
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/swagger-ui/**", "/api/api-docs/**", "/api/swagger-ui.html").permitAll()
            .requestMatchers("/api/health/**").permitAll()

            // Admin endpoints
            .requestMatchers("/api/admin/**").hasRole("ADMIN")

            // User endpoints (authenticated users)
            .requestMatchers("/api/users/**").authenticated()

            // All other requests require authentication
            .anyRequest().authenticated())

        // Configure authentication provider
        .authenticationProvider(authenticationProvider())

        // Add JWT filter
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

        // Configure exception handling
        .exceptionHandling(exceptions -> exceptions
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .accessDeniedHandler(jwtAccessDeniedHandler));

    return http.build();
  }

  /**
   * Configure authentication provider with user details service and password
   * encoder.
   * 
   * @return AuthenticationProvider
   */
  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  /**
   * Configure authentication manager.
   * 
   * @param config AuthenticationConfiguration
   * @return AuthenticationManager
   * @throws Exception if configuration fails
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }

  /**
   * Configure password encoder (BCrypt).
   * 
   * @return PasswordEncoder
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * Configure CORS settings.
   * 
   * @return CorsConfigurationSource
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Set allowed origins
    configuration.setAllowedOriginPatterns(corsProperties.getAllowedOriginPatterns());

    // Set allowed methods
    configuration.setAllowedMethods(corsProperties.getAllowedMethods());

    // Set allowed headers
    configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());

    // Set exposed headers
    configuration.setExposedHeaders(corsProperties.getExposedHeaders());

    // Set allow credentials
    configuration.setAllowCredentials(corsProperties.isAllowCredentials());

    // Set max age
    configuration.setMaxAge(corsProperties.getMaxAge());

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }

  // Security Headers Configuration
  @Bean
  public HeaderWriter securityHeadersWriter() {
    return new org.springframework.security.web.header.writers.StaticHeadersWriter("X-Content-Type-Options", "nosniff");
  }

  @Bean
  public HeaderWriter frameOptionsHeaderWriter() {
    return new org.springframework.security.web.header.writers.StaticHeadersWriter("X-Frame-Options", "DENY");
  }

  @Bean
  public HeaderWriter xssProtectionHeaderWriter() {
    return new org.springframework.security.web.header.writers.StaticHeadersWriter("X-XSS-Protection", "1; mode=block");
  }

  @Bean
  public HeaderWriter referrerPolicyHeaderWriter() {
    return new org.springframework.security.web.header.writers.StaticHeadersWriter("Referrer-Policy",
        "strict-origin-when-cross-origin");
  }

  @Bean
  public HeaderWriter contentSecurityPolicyHeaderWriter() {
    String csp = "default-src 'self'; " +
        "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
        "style-src 'self' 'unsafe-inline'; " +
        "img-src 'self' data: https:; " +
        "font-src 'self' data:; " +
        "connect-src 'self'; " +
        "frame-ancestors 'none'; " +
        "base-uri 'self'; " +
        "form-action 'self'";

    return new org.springframework.security.web.header.writers.StaticHeadersWriter("Content-Security-Policy", csp);
  }

  @Bean
  public HeaderWriter permissionsPolicyHeaderWriter() {
    String permissionsPolicy = "geolocation=(), " +
        "microphone=(), " +
        "camera=(), " +
        "payment=(), " +
        "usb=(), " +
        "magnetometer=(), " +
        "gyroscope=(), " +
        "accelerometer=()";

    return new org.springframework.security.web.header.writers.StaticHeadersWriter("Permissions-Policy",
        permissionsPolicy);
  }

  @Bean
  public HeaderWriter hstsHeaderWriter() {
    org.springframework.security.web.util.matcher.RequestMatcher httpsMatcher = new org.springframework.security.web.util.matcher.AntPathRequestMatcher(
        "/**");
    return new org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter(httpsMatcher,
        new org.springframework.security.web.header.writers.StaticHeadersWriter("Strict-Transport-Security",
            "max-age=31536000; includeSubDomains"));
  }

  @Bean
  public HeaderWriter cacheControlHeaderWriter() {
    return new org.springframework.security.web.header.writers.StaticHeadersWriter("Cache-Control",
        "no-store, no-cache, must-revalidate, max-age=0");
  }

  @Bean
  public HeaderWriter pragmaHeaderWriter() {
    return new org.springframework.security.web.header.writers.StaticHeadersWriter("Pragma", "no-cache");
  }

  @Bean
  public HeaderWriter expiresHeaderWriter() {
    return new org.springframework.security.web.header.writers.StaticHeadersWriter("Expires", "0");
  }
}