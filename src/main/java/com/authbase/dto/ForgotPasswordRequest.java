package com.authbase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public record ForgotPasswordRequest(
    @NotBlank(message = "Email is required") @Email(message = "Email must be a valid email address") String email) {
}