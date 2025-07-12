package com.authbase.dto;

import com.authbase.entity.User;

public record AuthenticationResponse(
    String accessToken,
    String refreshToken,
    User user,
    String message) {
}