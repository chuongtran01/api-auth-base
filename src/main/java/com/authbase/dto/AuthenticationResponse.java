package com.authbase.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response DTO for authentication operations (login, refresh token).
 * Contains access token, refresh token, user information, and message.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthenticationResponse(
        String accessToken,
        String refreshToken,
        UserDto user,
        String message) {
}