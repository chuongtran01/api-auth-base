package com.authbase.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Specialized response for validation errors.
 * Provides detailed field-level validation error information.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ValidationErrorResponse(
    @JsonProperty("success") boolean success,

    @JsonProperty("message") String message,

    @JsonProperty("timestamp") LocalDateTime timestamp,

    @JsonProperty("path") String path,

    @JsonProperty("fieldErrors") Map<String, String> fieldErrors,

    @JsonProperty("globalErrors") List<String> globalErrors,

    @JsonProperty("errorCount") int errorCount) {

  /**
   * Create a validation error response with field errors.
   * 
   * @param message     the error message
   * @param path        the request path
   * @param fieldErrors map of field names to error messages
   * @return ValidationErrorResponse instance
   */
  public static ValidationErrorResponse of(String message, String path, Map<String, String> fieldErrors) {
    return new ValidationErrorResponse(
        false,
        message,
        LocalDateTime.now(),
        path,
        fieldErrors,
        null,
        fieldErrors.size());
  }

  /**
   * Create a validation error response with global errors.
   * 
   * @param message      the error message
   * @param path         the request path
   * @param globalErrors list of global error messages
   * @return ValidationErrorResponse instance
   */
  public static ValidationErrorResponse of(String message, String path, List<String> globalErrors) {
    return new ValidationErrorResponse(
        false,
        message,
        LocalDateTime.now(),
        path,
        null,
        globalErrors,
        globalErrors.size());
  }

  /**
   * Create a validation error response with both field and global errors.
   * 
   * @param message      the error message
   * @param path         the request path
   * @param fieldErrors  map of field names to error messages
   * @param globalErrors list of global error messages
   * @return ValidationErrorResponse instance
   */
  public static ValidationErrorResponse of(String message, String path, Map<String, String> fieldErrors,
      List<String> globalErrors) {
    int totalErrors = (fieldErrors != null ? fieldErrors.size() : 0) + (globalErrors != null ? globalErrors.size() : 0);
    return new ValidationErrorResponse(
        false,
        message,
        LocalDateTime.now(),
        path,
        fieldErrors,
        globalErrors,
        totalErrors);
  }
}