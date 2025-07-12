package com.authbase.controller;

import com.authbase.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
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
  public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException e) {
    log.warn("Authentication failed: {}", e.getMessage());

    ErrorResponse errorResponse = new ErrorResponse(
        "AUTHENTICATION_FAILED",
        "Invalid username or password");
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
  }

  /**
   * Handle disabled account exceptions.
   */
  @ExceptionHandler(DisabledException.class)
  public ResponseEntity<ErrorResponse> handleDisabledAccount(DisabledException e) {
    log.warn("Disabled account access attempt: {}", e.getMessage());

    ErrorResponse errorResponse = new ErrorResponse(
        "ACCOUNT_DISABLED",
        "Account is disabled. Please contact support.");
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
  }

  /**
   * Handle access denied exceptions.
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
    log.warn("Access denied: {}", e.getMessage());

    ErrorResponse errorResponse = new ErrorResponse(
        "ACCESS_DENIED",
        "You don't have permission to access this resource");
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
  }

  // ============================================================================
  // VALIDATION EXCEPTIONS
  // ============================================================================

  /**
   * Handle method argument validation failures.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
    Map<String, String> errors = new HashMap<>();
    e.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    log.warn("Validation failed: {}", errors);

    ErrorResponse errorResponse = new ErrorResponse(
        "VALIDATION_FAILED",
        "Request validation failed: " + errors.toString());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * Handle constraint violation exceptions.
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
    Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
    Map<String, String> errors = new HashMap<>();

    violations.forEach(violation -> {
      String fieldName = violation.getPropertyPath().toString();
      String errorMessage = violation.getMessage();
      errors.put(fieldName, errorMessage);
    });

    log.warn("Constraint violation: {}", errors);

    ErrorResponse errorResponse = new ErrorResponse(
        "CONSTRAINT_VIOLATION",
        "Data validation failed: " + errors.toString());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  // ============================================================================
  // REQUEST PROCESSING EXCEPTIONS
  // ============================================================================

  /**
   * Handle malformed JSON requests.
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
    log.warn("Malformed request body: {}", e.getMessage());

    ErrorResponse errorResponse = new ErrorResponse(
        "INVALID_REQUEST_BODY",
        "Request body is malformed or contains invalid JSON");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * Handle missing request parameters.
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException e) {
    log.warn("Missing required parameter: {}", e.getParameterName());

    ErrorResponse errorResponse = new ErrorResponse(
        "MISSING_PARAMETER",
        "Required parameter '" + e.getParameterName() + "' is missing");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * Handle parameter type mismatches.
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
    log.warn("Parameter type mismatch: {} should be {}", e.getName(), e.getRequiredType());

    ErrorResponse errorResponse = new ErrorResponse(
        "INVALID_PARAMETER_TYPE",
        "Parameter '" + e.getName() + "' should be of type " + e.getRequiredType().getSimpleName());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * Handle 404 errors.
   */
  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoHandlerFound(NoHandlerFoundException e) {
    log.warn("No handler found for {} {}", e.getHttpMethod(), e.getRequestURL());

    ErrorResponse errorResponse = new ErrorResponse(
        "ENDPOINT_NOT_FOUND",
        "The requested endpoint does not exist");
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
  }

  // ============================================================================
  // DATA ACCESS EXCEPTIONS
  // ============================================================================

  /**
   * Handle data integrity violations (e.g., unique constraint violations).
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
    log.error("Data integrity violation: {}", e.getMessage(), e);

    ErrorResponse errorResponse = new ErrorResponse(
        "DATA_INTEGRITY_VIOLATION",
        "The operation would violate data integrity constraints");
    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
  }

  // ============================================================================
  // BUSINESS LOGIC EXCEPTIONS
  // ============================================================================

  /**
   * Handle business logic exceptions.
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
    log.warn("Invalid argument: {}", e.getMessage());

    ErrorResponse errorResponse = new ErrorResponse(
        "INVALID_ARGUMENT",
        e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  /**
   * Handle illegal state exceptions.
   */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e) {
    log.warn("Illegal state: {}", e.getMessage());

    ErrorResponse errorResponse = new ErrorResponse(
        "ILLEGAL_STATE",
        e.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
  }

  // ============================================================================
  // GENERAL EXCEPTIONS
  // ============================================================================

  /**
   * Handle all other runtime exceptions.
   */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
    log.error("Runtime error: {}", e.getMessage(), e);

    ErrorResponse errorResponse = new ErrorResponse(
        "RUNTIME_ERROR",
        "An unexpected error occurred while processing your request");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  /**
   * Handle all other exceptions (fallback).
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
    log.error("Unexpected error: {}", e.getMessage(), e);

    ErrorResponse errorResponse = new ErrorResponse(
        "INTERNAL_SERVER_ERROR",
        "An unexpected error occurred. Please try again later.");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }
}