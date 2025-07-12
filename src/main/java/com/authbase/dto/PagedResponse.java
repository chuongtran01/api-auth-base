package com.authbase.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Paged response wrapper for paginated API responses.
 * Provides pagination metadata along with the data.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PagedResponse<T>(
    @JsonProperty("content") List<T> content,

    @JsonProperty("page") int page,

    @JsonProperty("size") int size,

    @JsonProperty("totalElements") long totalElements,

    @JsonProperty("totalPages") int totalPages,

    @JsonProperty("hasNext") boolean hasNext,

    @JsonProperty("hasPrevious") boolean hasPrevious,

    @JsonProperty("isFirst") boolean isFirst,

    @JsonProperty("isLast") boolean isLast) {

  /**
   * Create a paged response from Spring Page object.
   * 
   * @param page the Spring Page object
   * @return PagedResponse instance
   */
  public static <T> PagedResponse<T> fromPage(org.springframework.data.domain.Page<T> page) {
    return new PagedResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.hasNext(),
        page.hasPrevious(),
        page.isFirst(),
        page.isLast());
  }

  /**
   * Create a paged response with custom pagination info.
   * 
   * @param content       the list of items
   * @param page          the current page number (0-based)
   * @param size          the page size
   * @param totalElements the total number of elements
   * @return PagedResponse instance
   */
  public static <T> PagedResponse<T> of(List<T> content, int page, int size, long totalElements) {
    int totalPages = (int) Math.ceil((double) totalElements / size);
    boolean hasNext = page < totalPages - 1;
    boolean hasPrevious = page > 0;
    boolean isFirst = page == 0;
    boolean isLast = page == totalPages - 1;

    return new PagedResponse<>(
        content,
        page,
        size,
        totalElements,
        totalPages,
        hasNext,
        hasPrevious,
        isFirst,
        isLast);
  }
}