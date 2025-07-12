package com.authbase.dto;

import com.authbase.entity.User;

public record UserResponse(
    String message,
    User user,
    boolean success) {
}