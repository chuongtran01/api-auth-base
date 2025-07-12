package com.authbase.dto;

public record ErrorResponse(
    String error,
    String message) {
}