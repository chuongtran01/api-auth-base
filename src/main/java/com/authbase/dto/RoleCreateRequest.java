package com.authbase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoleCreateRequest(
    @NotBlank(message = "Role name is required") @Size(max = 50, message = "Role name must be at most 50 characters") String name,

    @Size(max = 200, message = "Role description must be at most 200 characters") String description) {
}