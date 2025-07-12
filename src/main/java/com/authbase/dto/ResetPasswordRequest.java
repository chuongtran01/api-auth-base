package com.authbase.dto;

import jakarta.validation.constraints.NotBlank;
import com.authbase.validation.annotation.StrongPassword;

public record ResetPasswordRequest(
    @NotBlank(message = "Token is required") String token,

    @NotBlank(message = "New password is required") @StrongPassword(minLength = 8, maxLength = 128, requireUppercase = true, requireLowercase = true, requireDigit = true, requireSpecial = true, message = "New password must be at least 8 characters long and contain uppercase, lowercase, digit, and special character") String newPassword) {
}