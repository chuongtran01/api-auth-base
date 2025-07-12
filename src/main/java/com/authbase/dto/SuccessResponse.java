package com.authbase.dto;

public record SuccessResponse(
    String message,
    boolean success) {
}