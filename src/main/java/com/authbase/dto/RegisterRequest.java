package com.authbase.dto;

import jakarta.validation.constraints.NotBlank;
import com.authbase.validation.annotation.ValidEmail;
import com.authbase.validation.annotation.StrongPassword;

public record RegisterRequest(
    @NotBlank(message = "Email is required") @ValidEmail(message = "Please provide a valid email address") String email,

    @NotBlank(message = "Password is required") @StrongPassword(minLength = 8, maxLength = 128, requireUppercase = true, requireLowercase = true, requireDigit = true, requireSpecial = true, message = "Password must be at least 8 characters long and contain uppercase, lowercase, digit, and special character") String password) {
}