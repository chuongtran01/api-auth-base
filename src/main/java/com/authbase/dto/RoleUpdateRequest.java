package com.authbase.dto;

import jakarta.validation.constraints.Size;

public record RoleUpdateRequest(
    @Size(max = 200, message = "Role description must be at most 200 characters") String description) {
}