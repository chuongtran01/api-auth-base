package com.authbase.dto;

/**
 * Response DTO for user-related operations.
 * Contains user information, message, and success status.
 */
public record UserResponse(
        String message,
        UserDto user,
        boolean success) {
}