package com.group4.swissrouteapi.controllers;

import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.dtos.requests.FavoriteRouteRequest;
import com.group4.swissrouteapi.dtos.responses.ErrorResponse;
import com.group4.swissrouteapi.dtos.responses.favorites.RouteResponse;
import com.group4.swissrouteapi.dtos.responses.favorites.RoutesResponse;
import com.group4.swissrouteapi.services.FavoriteRouteService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * FavoriteRouteController
 *
 * <p>Spring REST controller responsible for exposing endpoints related to user favorite routes.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(
    name = "Favorite Routes",
    description =
        "Controller in charge of receiving HTTP requests related to the favorite routes service.")
public class FavoriteRouteController {

  private final FavoriteRouteService favoriteRouteService;

  /**
   * Handles HTTP POST requests to add a new favorite route for the authenticated user.
   *
   * <p>Validates the incoming {@link FavoriteRouteRequest} payload and delegates the creation logic
   * to {@link FavoriteRouteService}. The route is associated with the currently authenticated user,
   * identified by their {@link Authentication} principal.
   *
   * @param authentication the Spring Security authentication object containing the user's identity
   * @param request the validated request payload with route details
   * @return a {@link ResponseEntity} containing the created {@link RouteResponse} with HTTP
   *     status {@link org.springframework.http.HttpStatus#OK}
   */
  @Operation(
      summary = "Add route to favorites",
      description = "Endpoint in charge of registering favorite routes",
      security = {@SecurityRequirement(name = "BearerAuth")})
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "Data body for favorite route aggregation",
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = FavoriteRouteRequest.class)))
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Successful Response - Favorite route response body when registered",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = RouteResponse.class))),
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
        description = "Not found - User not found",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Conflict - name already registered",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
  })
  @PostMapping(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
  public ResponseEntity<RouteResponse> addFavoriteRoute(
      Authentication authentication, @RequestBody @Valid FavoriteRouteRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            favoriteRouteService.addFavoriteRoute(
                UUID.fromString(authentication.getName()), request));
  }
  
  @GetMapping(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
    public ResponseEntity<RoutesResponse> getAllFavoriteRoutes(Authentication authentication){
      return ResponseEntity.status(HttpStatus.OK).body(favoriteRouteService.getFavoriteRoutes(UUID.fromString(authentication.getName())));
  }
}
