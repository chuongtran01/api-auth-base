package com.authbase.dto;

import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
    @Size(max = 50, message = "Username must be at most 50 characters") String username,

    @Size(max = 100, message = "First name must be at most 100 characters") String firstName,

    @Size(max = 100, message = "Last name must be at most 100 characters") String lastName) {
}