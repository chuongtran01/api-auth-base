package com.authbase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public record LoginRequest(
    @NotBlank(message = "Username or email is required") String username,

    @NotBlank(message = "Password is required") String password) {
}