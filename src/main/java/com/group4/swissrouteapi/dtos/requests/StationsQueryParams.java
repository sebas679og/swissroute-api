package com.group4.swissrouteapi.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * StationsQueryParams
 *
 * <p>Data transfer object representing query parameters for searching transport stations.
 *
 * <p>Encapsulates the search query string used to filter stations by name or keyword.
 *
 * <p>Annotated with {@link lombok.Data} to generate boilerplate methods, {@link lombok.Builder} to
 * provide a fluent builder API, and Lombok constructors for flexibility in instantiation.
 *
 * <p>Includes validation with {@link jakarta.validation.constraints.NotBlank} to ensure the query
 * string is not empty.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationsQueryParams {

  @NotBlank(message = "Query cannot be blank")
  private String query;
}
