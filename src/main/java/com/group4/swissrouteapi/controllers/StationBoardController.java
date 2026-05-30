package com.group4.swissrouteapi.controllers;

import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.dtos.requests.StationBoardQueryParams;
import com.group4.swissrouteapi.dtos.responses.ErrorResponse;
import com.group4.swissrouteapi.dtos.responses.board.StationsBoardResponse;
import com.group4.swissrouteapi.services.StationBoardService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * StationBoardController
 *
 * <p>REST controller exposing endpoints for retrieving real-time departure and arrival information
 * from train stations.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(
    name = "Station Board",
    description = "Endpoints for retrieving real-time departure information from train stations.")
public class StationBoardController {

  private final StationBoardService stationBoardService;

  @Operation(
      summary = "Get station departures",
      description =
          """
            Returns the upcoming departures for a specific station, including:

            - Service name
            - Train category
            - Final destination
            - Departure time

            Data is retrieved from the Swiss public transport API.
            """,
      security = {@SecurityRequirement(name = "BearerAuth")})
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Station board retrieved successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = StationsBoardResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid request parameters",
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
        description = "Station not found",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "502",
        description = "Error communicating with external transport provider",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "503",
        description = "External transport service unavailable",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping(ApiPaths.StationBoard.STATION_BOARD)
  public ResponseEntity<StationsBoardResponse> getStationBoard(
      @ParameterObject @Valid @ModelAttribute StationBoardQueryParams queryParams) {

    return ResponseEntity.ok(stationBoardService.getStationBoards(queryParams));
  }
}
