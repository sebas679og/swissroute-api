package com.group4.swissrouteapi.dtos.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * HistoryQueryParams
 *
 * <p>DTO representing query parameters for retrieving paginated search history. Provides default
 * values and validation constraints for pagination.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryQueryParams {

  public static final Integer DEFAULT_PAGE = 1;
  public static final Integer DEFAULT_SIZE = 20;

  @Min(value = 1, message = "Page must be greater than or equal to 1")
  @Builder.Default
  private Integer page = DEFAULT_PAGE;

  @Min(value = 1, message = "Size must be greater than or equal to 1")
  @Max(value = 50, message = "Size must be less than or equal to 50")
  @Builder.Default
  private Integer size = DEFAULT_SIZE;

  public Integer getPage() {
    return page != null ? page : DEFAULT_PAGE;
  }

  public Integer getSize() {
    return size != null ? size : DEFAULT_SIZE;
  }
}
