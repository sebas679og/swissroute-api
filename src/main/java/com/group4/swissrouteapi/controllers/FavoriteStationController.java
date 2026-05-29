package com.group4.swissrouteapi.controllers;

import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.dtos.requests.StationRequest;
import com.group4.swissrouteapi.dtos.responses.ErrorResponse;
import com.group4.swissrouteapi.dtos.responses.board.StationsBoardResponse;
import com.group4.swissrouteapi.dtos.responses.favorites.FavStationsResponse;
import com.group4.swissrouteapi.dtos.responses.favorites.StationResponse;
import com.group4.swissrouteapi.exceptions.NotFoundException;
import com.group4.swissrouteapi.services.FavoriteStationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * FavoriteStationController
 *
 * <p>Spring REST controller responsible for exposing endpoints related to managing the
 * authenticated user's favorite stations.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(
    name = "Favorite Stations",
    description = "Endpoints for managing the authenticated user's favorite stations.")
public class FavoriteStationController {

  private final FavoriteStationService favoriteStationService;

  /**
   * Handles HTTP POST requests to add a new favorite station for the authenticated user.
   *
   * <p>Validates the incoming {@link StationRequest} payload and delegates the creation logic to
   * {@link FavoriteStationService}. The station is associated with the currently authenticated
   * user, identified by their {@link Authentication} principal.
   *
   * @param authentication the Spring Security authentication object containing the user's identity
   * @param request the validated request payload with station details
   * @return a {@link ResponseEntity} containing the created {@link StationResponse} with HTTP
   *     status {@link org.springframework.http.HttpStatus#CREATED}
   */
  @Operation(
      summary = "Add a favorite station",
      description = "Registers a station as a favorite for the authenticated user.",
      security = {@SecurityRequirement(name = "BearerAuth")})
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "Request body containing the station information to be added as favorite.",
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = StationRequest.class)))
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Favorite station created successfully.",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = StationResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Validation error - Invalid request body or fields.",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - Missing or invalid authentication token.",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "User not found.",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Conflict - The station is already registered as favorite.",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
  })
  @PostMapping(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
  public ResponseEntity<StationResponse> addFavoriteStations(
      Authentication authentication, @RequestBody @Valid StationRequest request) {

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            favoriteStationService.addFavoriteStation(
                UUID.fromString(authentication.getName()), request));
  }

  /**
   * Handles HTTP GET requests to retrieve all favorite stations for the authenticated user.
   *
   * <p>Delegates the retrieval logic to {@link FavoriteStationService}, ensuring that the response
   * is scoped to the currently authenticated user. Always returns a {@link FavStationsResponse}
   * object, which may contain an empty list if the user has no registered favorite stations.
   *
   * @param authentication the Spring Security authentication object containing the user's identity
   * @return a {@link ResponseEntity} containing the {@link FavStationsResponse} with HTTP status
   *     {@link org.springframework.http.HttpStatus#OK}
   */
  @Operation(
      summary = "Get favorite stations",
      description = "Returns all favorite stations associated with the authenticated user.",
      security = {@SecurityRequirement(name = "BearerAuth")})
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Favorite stations retrieved successfully.",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = FavStationsResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - Missing or invalid authentication token.",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "User not found.",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
  public ResponseEntity<FavStationsResponse> getFavoriteStations(Authentication authentication) {

    return ResponseEntity.status(HttpStatus.OK)
        .body(favoriteStationService.getFavoriteStation(UUID.fromString(authentication.getName())));
  }

  /**
   * Handles HTTP DELETE requests to remove a favorite station for the authenticated user.
   *
   * <p>Delegates the deletion logic to {@link FavoriteStationService}, ensuring that the operation
   * is scoped to the currently authenticated user. If the station does not exist, a {@link
   * NotFoundException} is thrown.
   *
   * @param authentication the Spring Security authentication object containing the user's identity
   * @param externalStationId external identifier of the station to remove
   * @return a {@link ResponseEntity} with no content and HTTP status {@link
   *     org.springframework.http.HttpStatus#NO_CONTENT} upon successful deletion
   */
  @Operation(
      summary = "Remove a favorite station",
      description = "Deletes a favorite station associated with the authenticated user.",
      security = {@SecurityRequirement(name = "BearerAuth")})
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Favorite station deleted successfully."),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid station identifier format.",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - Missing or invalid authentication token.",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "User or favorite station not found.",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @DeleteMapping(ApiPaths.FavoriteStations.FAVORITE_STATION)
  public ResponseEntity<Void> deleteFavoriteStations(
      Authentication authentication,
      @PathVariable
          @Parameter(
              description = "External identifier of the user's favorite station",
              required = true,
              example = "8503000")
          String externalStationId) {

    favoriteStationService.removeFavoriteStation(
        UUID.fromString(authentication.getName()), externalStationId);

    return ResponseEntity.noContent().build();
  }

  /**
   * Handles HTTP GET requests to retrieve the station board (departures and arrivals) for a user's
   * favorite station.
   *
   * <p>Delegates the retrieval logic to {@link FavoriteStationService}, ensuring that the response
   * is scoped to the currently authenticated user and the provided external station identifier.
   *
   * @param authentication the Spring Security authentication object containing the user's identity
   * @param externalStationId external identifier of the user's favorite station (e.g., SBB station
   *     code)
   * @return a {@link ResponseEntity} containing the {@link StationsBoardResponse} with HTTP status
   *     {@link org.springframework.http.HttpStatus#OK}
   */
  @Operation(
      summary = "Get station departures from favorite station",
      description =
          """
            Returns the upcoming departures for one of the user's favorite stations.

            The station is retrieved from the authenticated user's saved favorite stations.

            Response includes:

            - Service name
            - Train category
            - Final destination
            - Departure time
        """,
      security = {@SecurityRequirement(name = "BearerAuth")})
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Favorite station board retrieved successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = StationsBoardResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid station identifier",
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
        description = "User or favorite station not found",
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
  @GetMapping(ApiPaths.FavoriteStations.FAVORITE_STATION_BOARD)
  public ResponseEntity<StationsBoardResponse> getStationBoardByFavoriteStationId(
      Authentication authentication,
      @PathVariable
          @Parameter(
              description = "External identifier of the user's favorite station",
              required = true,
              example = "8503000")
          String externalStationId) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            favoriteStationService.getStationBoardByFavoriteStation(
                UUID.fromString(authentication.getName()), externalStationId));
  }
}
