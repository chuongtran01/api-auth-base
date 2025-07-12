package com.authbase.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Generic API response wrapper for consistent response formatting.
 * Provides a standardized structure for all API responses.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    @JsonProperty("success") boolean success,

    @JsonProperty("message") String message,

    @JsonProperty("data") T data,

    @JsonProperty("timestamp") LocalDateTime timestamp,

    @JsonProperty("path") String path) {

  /**
   * Create a successful response with data.
   * 
   * @param message the response message
   * @param data    the response data
   * @param path    the request path
   * @return ApiResponse instance
   */
  public static <T> ApiResponse<T> success(String message, T data, String path) {
    return new ApiResponse<>(true, message, data, LocalDateTime.now(), path);
  }

  /**
   * Create a successful response without data.
   * 
   * @param message the response message
   * @param path    the request path
   * @return ApiResponse instance
   */
  public static <T> ApiResponse<T> success(String message, String path) {
    return new ApiResponse<>(true, message, null, LocalDateTime.now(), path);
  }

  /**
   * Create an error response.
   * 
   * @param message the error message
   * @param path    the request path
   * @return ApiResponse instance
   */
  public static <T> ApiResponse<T> error(String message, String path) {
    return new ApiResponse<>(false, message, null, LocalDateTime.now(), path);
  }

  /**
   * Create an error response with data.
   * 
   * @param message the error message
   * @param data    the error data
   * @param path    the request path
   * @return ApiResponse instance
   */
  public static <T> ApiResponse<T> error(String message, T data, String path) {
    return new ApiResponse<>(false, message, data, LocalDateTime.now(), path);
  }
}