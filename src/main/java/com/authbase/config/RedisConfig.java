package com.authbase.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Redis configuration for token blacklisting and session management.
 * Provides Redis template configuration and session management setup.
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600) // 1 hour session timeout
public class RedisConfig {

  @Value("${auth.redis.enabled:false}")
  private boolean redisEnabled;

  /**
   * Configure Redis template for token and session operations.
   * Uses String keys and JSON values for better performance and readability.
   * 
   * @param connectionFactory Redis connection factory
   * @return configured Redis template
   */
  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    // Use String serializer for keys (better performance)
    StringRedisSerializer stringSerializer = new StringRedisSerializer();
    template.setKeySerializer(stringSerializer);
    template.setHashKeySerializer(stringSerializer);

    // Use JSON serializer for values (better readability and type safety)
    GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
    template.setValueSerializer(jsonSerializer);
    template.setHashValueSerializer(jsonSerializer);

    // Enable default serializers for other operations
    template.setDefaultSerializer(jsonSerializer);
    template.setEnableDefaultSerializer(true);
    template.setEnableTransactionSupport(true);

    template.afterPropertiesSet();
    return template;
  }

  /**
   * Check if Redis is enabled for this application.
   * 
   * @return true if Redis is enabled, false otherwise
   */
  public boolean isRedisEnabled() {
    return redisEnabled;
  }
}