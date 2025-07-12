package com.authbase.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * CORS (Cross-Origin Resource Sharing) configuration for the authentication
 * API.
 * This allows frontend applications to make requests to the API from different
 * origins.
 */
@Configuration
public class CorsConfig {

  private final CorsProperties corsProperties;

  /**
   * Constructor injection for CORS properties.
   * 
   * @param corsProperties the CORS configuration properties
   */
  public CorsConfig(CorsProperties corsProperties) {
    this.corsProperties = corsProperties;
  }

  /**
   * Configure CORS settings for the application.
   * 
   * @return CorsConfigurationSource with CORS settings
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Set allowed origin patterns
    configuration.setAllowedOriginPatterns(corsProperties.getAllowedOriginPatterns());

    // Set allowed HTTP methods
    configuration.setAllowedMethods(corsProperties.getAllowedMethods());

    // Set allowed headers
    configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());

    // Set exposed headers
    configuration.setExposedHeaders(corsProperties.getExposedHeaders());

    // Set credentials policy
    configuration.setAllowCredentials(corsProperties.isAllowCredentials());

    // Set max age for preflight requests
    configuration.setMaxAge(corsProperties.getMaxAge());

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }
}