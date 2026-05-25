package com.group4.swissrouteapi.controllers;

import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.dtos.requests.ConnectionsQueryParams;
import com.group4.swissrouteapi.dtos.responses.ErrorResponse;
import com.group4.swissrouteapi.dtos.responses.connections.ConnectionsResponse;
import com.group4.swissrouteapi.services.ConnectionsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ConnectionController
 *
 * <p>REST controller for handling transport connection requests. Exposes an endpoint to search for
 * connections between stations based on query parameters.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(
    name = "Connections",
    description =
        "Endpoints related to transport connections, allowing clients to search for "
            + "connections between stations based on query parameters.")
public class ConnectionController {

  private final ConnectionsService connectionsService;

  /**
   * Handles GET requests for transport connections.
   *
   * @param queryParams validated query parameters including origin, destination, optional
   *     date/time, and transportation filters
   * @param authentication authenticated user performing the request
   * @return {@link ResponseEntity} containing {@link ConnectionsResponse} with HTTP 200 status
   */
  @Operation(
      summary = "Get connections between stations",
      description = "Endpoint responsible for obtaining connections between stations.",
      security = {@SecurityRequirement(name = "BearerAuth")})
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Successful response - List of connections matching the query parameters",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ConnectionsResponse.class))),
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
  @GetMapping(ApiPaths.Connection.CONNECTIONS)
  public ResponseEntity<ConnectionsResponse> getConnections(
      @ParameterObject
          @Parameter(
              name = "transportations",
              description = "List of transportation types to filter results",
              explode = Explode.TRUE,
              style = ParameterStyle.FORM,
              schema =
                  @Schema(
                      type = "array",
                      allowableValues = {"train", "tram", "ship", "bus", "cableway"}))
          @Valid
          @ModelAttribute
          ConnectionsQueryParams queryParams,
      Authentication authentication) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            connectionsService.getConnections(
                queryParams, UUID.fromString(authentication.getName())));
  }
}
