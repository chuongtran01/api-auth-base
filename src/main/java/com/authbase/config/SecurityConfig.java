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
import org.springframework.web.cors.CorsConfigurationSource;

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
  private final CorsConfigurationSource corsConfigurationSource;
  private final HeaderWriter securityHeadersWriter;
  private final HeaderWriter frameOptionsHeaderWriter;
  private final HeaderWriter xssProtectionHeaderWriter;
  private final HeaderWriter referrerPolicyHeaderWriter;
  private final HeaderWriter contentSecurityPolicyHeaderWriter;
  private final HeaderWriter permissionsPolicyHeaderWriter;
  private final HeaderWriter hstsHeaderWriter;
  private final HeaderWriter cacheControlHeaderWriter;
  private final HeaderWriter pragmaHeaderWriter;
  private final HeaderWriter expiresHeaderWriter;
  private final WhitelistProperties whitelistProperties;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
      JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
      JwtAccessDeniedHandler jwtAccessDeniedHandler,
      UserDetailsService userDetailsService,
      CorsConfigurationSource corsConfigurationSource,
      HeaderWriter securityHeadersWriter,
      HeaderWriter frameOptionsHeaderWriter,
      HeaderWriter xssProtectionHeaderWriter,
      HeaderWriter referrerPolicyHeaderWriter,
      HeaderWriter contentSecurityPolicyHeaderWriter,
      HeaderWriter permissionsPolicyHeaderWriter,
      HeaderWriter hstsHeaderWriter,
      HeaderWriter cacheControlHeaderWriter,
      HeaderWriter pragmaHeaderWriter,
      HeaderWriter expiresHeaderWriter,
      WhitelistProperties whitelistProperties) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    this.userDetailsService = userDetailsService;
    this.corsConfigurationSource = corsConfigurationSource;
    this.securityHeadersWriter = securityHeadersWriter;
    this.frameOptionsHeaderWriter = frameOptionsHeaderWriter;
    this.xssProtectionHeaderWriter = xssProtectionHeaderWriter;
    this.referrerPolicyHeaderWriter = referrerPolicyHeaderWriter;
    this.contentSecurityPolicyHeaderWriter = contentSecurityPolicyHeaderWriter;
    this.permissionsPolicyHeaderWriter = permissionsPolicyHeaderWriter;
    this.hstsHeaderWriter = hstsHeaderWriter;
    this.cacheControlHeaderWriter = cacheControlHeaderWriter;
    this.pragmaHeaderWriter = pragmaHeaderWriter;
    this.expiresHeaderWriter = expiresHeaderWriter;
    this.whitelistProperties = whitelistProperties;
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

        // Configure CORS (uses the bean from CorsConfig)
        .cors(cors -> cors.configurationSource(corsConfigurationSource))

        // Configure session management (stateless for JWT)
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // Configure security headers (uses beans from SecurityHeadersConfig)
        .headers(headers -> headers
            .addHeaderWriter(securityHeadersWriter)
            .addHeaderWriter(frameOptionsHeaderWriter)
            .addHeaderWriter(xssProtectionHeaderWriter)
            .addHeaderWriter(referrerPolicyHeaderWriter)
            .addHeaderWriter(contentSecurityPolicyHeaderWriter)
            .addHeaderWriter(permissionsPolicyHeaderWriter)
            .addHeaderWriter(hstsHeaderWriter)
            .addHeaderWriter(cacheControlHeaderWriter)
            .addHeaderWriter(pragmaHeaderWriter)
            .addHeaderWriter(expiresHeaderWriter))

        // Configure authorization rules
        .authorizeHttpRequests(authz -> {
          // Configure whitelisted endpoints from WhitelistProperties
          whitelistProperties.getEndpoints().forEach(pattern -> {
            authz.requestMatchers(pattern).permitAll();
          });

          // Admin endpoints
          authz.requestMatchers("/api/admin/**").hasRole("ADMIN")

              // User endpoints (authenticated users)
              .requestMatchers("/api/users/**").authenticated()

              // All other requests require authentication
              .anyRequest().authenticated();
        })

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
}