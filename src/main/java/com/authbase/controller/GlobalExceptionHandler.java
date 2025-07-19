package com.authbase.controller;

import com.authbase.dto.ValidationErrorResponse;
import com.authbase.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Global exception handler for REST controllers.
 * Provides consistent error responses across all endpoints.
 * 
 * This handler is designed to be scalable and maintainable:
 * - Specific exception handlers for different error types
 * - Consistent error response format
 * - Proper HTTP status codes
 * - Detailed logging for debugging
 * - Validation error handling
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  // ============================================================================
  // AUTHENTICATION & AUTHORIZATION EXCEPTIONS
  // ============================================================================

  /**
   * Handle authentication failures.
   */
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
      BadCredentialsException e, WebRequest request) {
    log.warn("Authentication failed: {}", e.getMessage());

    ApiResponse<Void> errorResponse = ApiResponse.error(
        "Invalid username or password",
        request.getDescription(false));
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
  }

  /**
   * Handle disabled account exceptions.
   */
  @ExceptionHandler(DisabledException.class)
  public ResponseEntity<ApiResponse<Void>> handleDisabledAccount(
      DisabledException e, WebRequest request) {
    log.warn("Disabled account access attempt: {}", e.getMessage());

    ApiResponse<Void> errorResponse = ApiResponse.error(
        "Account is disabled. Please contact support.",
        request.getDescription(false));
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
  }

  /**
   * Handle access denied exceptions.
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
      AccessDeniedException e, WebRequest request) {
    log.warn("Access denied: {}", e.getMessage());

    ApiResponse<Void> errorResponse = ApiResponse.error(
        "You don't have permission to access this resource",
        request.getDescription(false));
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
  }

  // ============================================================================
  // VALIDATION EXCEPTIONS
  // ============================================================================

  /**
   * Handle method argument validation failures.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException e, WebRequest request) {
    Map<String, String> errors = new HashMap<>();
    e.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    log.warn("Validation failed: {}", errors);

    ValidationErrorResponse errorResponse = ValidationErrorResponse.of(
        "Request validation failed",
        request.getDescription(false),
        errors);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * Handle constraint violation exceptions.
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(
      ConstraintViolationException e, WebRequest request) {
    Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
    Map<String, String> errors = new HashMap<>();

    violations.forEach(violation -> {
      String fieldName = violation.getPropertyPath().toString();
      String errorMessage = violation.getMessage();
      errors.put(fieldName, errorMessage);
    });

    log.warn("Constraint violation: {}", errors);

    ValidationErrorResponse errorResponse = ValidationErrorResponse.of(
        "Data validation failed",
        request.getDescription(false),
        errors);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  // ============================================================================
  // REQUEST PROCESSING EXCEPTIONS
  // ============================================================================

  /**
   * Handle malformed JSON requests.
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
      HttpMessageNotReadableException e, WebRequest request) {
    log.warn("Malformed request body: {}", e.getMessage());

    ApiResponse<Void> errorResponse = ApiResponse.error(
        "Request body is malformed or contains invalid JSON",
        request.getDescription(false));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * Handle missing request parameters.
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiResponse<Void>> handleMissingParameter(
      MissingServletRequestParameterException e, WebRequest request) {
    log.warn("Missing required parameter: {}", e.getParameterName());

    ApiResponse<Void> errorResponse = ApiResponse.error(
        "Required parameter '" + e.getParameterName() + "' is missing",
        request.getDescription(false));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * Handle parameter type mismatches.
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
      MethodArgumentTypeMismatchException e, WebRequest request) {
    log.warn("Parameter type mismatch: {} should be {}", e.getName(), e.getRequiredType());

    ApiResponse<Void> errorResponse = ApiResponse.error(
        "Parameter '" + e.getName() + "' should be of type " + e.getRequiredType().getSimpleName(),
        request.getDescription(false));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * Handle 404 errors.
   */
  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleNoHandlerFound(
      NoHandlerFoundException e, WebRequest request) {
    log.warn("No handler found for {} {}", e.getHttpMethod(), e.getRequestURL());

    ApiResponse<Void> errorResponse = ApiResponse.error(
        "The requested endpoint does not exist",
        request.getDescription(false));
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
  }

  // ============================================================================
  // DATA ACCESS EXCEPTIONS
  // ============================================================================

  /**
   * Handle data integrity violations (e.g., unique constraint violations).
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
      DataIntegrityViolationException e, WebRequest request) {
    log.error("Data integrity violation: {}", e.getMessage(), e);

    ApiResponse<Void> errorResponse = ApiResponse.error(
        "The operation would violate data integrity constraints",
        request.getDescription(false));
    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
  }

  // ============================================================================
  // BUSINESS LOGIC EXCEPTIONS
  // ============================================================================

  /**
   * Handle business logic exceptions.
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
      IllegalArgumentException e, WebRequest request) {
    log.warn("Invalid argument: {}", e.getMessage());

    ApiResponse<Void> errorResponse = ApiResponse.error(
        e.getMessage(),
        request.getDescription(false));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * Handle illegal state exceptions.
   */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ApiResponse<Void>> handleIllegalState(
      IllegalStateException e, WebRequest request) {
    log.warn("Illegal state: {}", e.getMessage());

    ApiResponse<Void> errorResponse = ApiResponse.error(
        e.getMessage(),
        request.getDescription(false));
    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
  }

  // ============================================================================
  // GENERAL EXCEPTIONS
  // ============================================================================

  /**
   * Handle all other runtime exceptions.
   */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ApiResponse<Void>> handleRuntimeException(
      RuntimeException e, WebRequest request) {
    log.error("Runtime error: {}", e.getMessage(), e);

    ApiResponse<Void> errorResponse = ApiResponse.error(
        "An unexpected error occurred while processing your request",
        request.getDescription(false));
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  /**
   * Handle all other exceptions (fallback).
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleGenericException(
      Exception e, WebRequest request) {
    log.error("Unexpected error: {}", e.getMessage(), e);

    ApiResponse<Void> errorResponse = ApiResponse.error(
        "An unexpected error occurred. Please try again later.",
        request.getDescription(false));
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }
}