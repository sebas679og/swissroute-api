package com.group4.swissrouteapi.controllers;

import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.dtos.requests.StationsQueryParams;
import com.group4.swissrouteapi.dtos.responses.ErrorResponse;
import com.group4.swissrouteapi.dtos.responses.StationsResponse;
import com.group4.swissrouteapi.services.StationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * StationController
 *
 * <p>REST controller responsible for handling HTTP requests related to transport stations.
 *
 * <p>Exposes endpoints for retrieving station data based on query parameters. Delegates business
 * logic to the {@link StationService}.
 *
 * <p>Annotated with {@link org.springframework.web.bind.annotation.RestController} to mark it as a
 * Spring MVC controller, {@link org.springframework.web.bind.annotation.RequestMapping} for request
 * mapping configuration, and {@link lombok.RequiredArgsConstructor} to enable constructor-based
 * dependency injection.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(
    name = "Stations",
    description =
        "Endpoints related to transport stations, allowing clients to retrieve "
            + "information about stations based on query parameters.")
public class StationController {

  private final StationService stationService;

  @Operation(
      summary = "Get stations by name",
      description = "Endpoint responsible for obtaining stations by their name.",
      security = {@SecurityRequirement(name = "BearerAuth")})
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Successful response - List of stations matching the query parameters",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = StationsResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Validation error - Invalid input fields",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - Missing or invalid authentication token",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Not found - No stations found matching the query parameters",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "502",
        description = "Bad Gateway - Error communicating with external station data provider",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "503",
        description =
            "Service Unavailable - External station data provider is currently unavailable",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
  })
  @GetMapping(ApiPaths.Station.STATIONS)
  public ResponseEntity<StationsResponse> getStations(
      @ParameterObject @Valid @ModelAttribute StationsQueryParams queryParams) {
    return ResponseEntity.status(HttpStatus.OK).body(stationService.getStationsByName(queryParams));
  }
}
