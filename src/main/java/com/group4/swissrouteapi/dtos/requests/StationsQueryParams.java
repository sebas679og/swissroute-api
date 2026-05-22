package com.group4.swissrouteapi.dtos.requests;

import com.group4.swissrouteapi.utils.validators.query.ValidStationQuery;
import io.swagger.v3.oas.annotations.media.Schema;
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
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidStationQuery
@Schema(description = "Query parameters for searching transport stations")
public class StationsQueryParams {

  @Schema(description = "query", example = "Basel")
  private String query;

  @Schema(description = "Coordinate Latitude", example = "47.5596")
  private Double latitude;

  @Schema(description = "Coordinate Longitude", example = "7.5886")
  private Double longitude;
}
