package com.authbase.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Data Transfer Object for User entity.
 * Excludes sensitive information like passwords and provides a clean API
 * response.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record UserDto(
    Long id,
    String email,
    String username,
    String firstName,
    String lastName,
    Boolean isEnabled,
    Boolean isEmailVerified,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime lastLoginAt,
    Set<String> roles) {
}