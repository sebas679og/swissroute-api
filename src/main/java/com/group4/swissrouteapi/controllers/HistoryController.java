package com.group4.swissrouteapi.controllers;

import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.dtos.requests.HistoryQueryParams;
import com.group4.swissrouteapi.dtos.responses.ErrorResponse;
import com.group4.swissrouteapi.dtos.responses.history.HistoryResponse;
import com.group4.swissrouteapi.services.HistoryService;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HistoryController
 *
 * <p>Spring REST controller responsible for exposing endpoints related to user search history.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(
    name = "History",
    description =
        "Endpoints related to user search history, allowing clients to retrieve, delete, and clear "
            + "their search history records.")
public class HistoryController {

  private final HistoryService historyService;

  @Operation(
      summary = "Get search history of connections",
      description =
          "Endpoint responsible for retrieving the search history of "
              + "connections based on query parameters.",
      security = {@SecurityRequirement(name = "BearerAuth")})
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description =
            "Successful response - Paginated list of search history "
                + "records matching the query parameters",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = HistoryResponse.class))),
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
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @GetMapping(ApiPaths.History.HISTORY)
  public ResponseEntity<HistoryResponse> getHistory(
      Authentication authentication,
      @ParameterObject @Valid @ModelAttribute HistoryQueryParams queryParams) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(historyService.getAllHistory(queryParams, UUID.fromString(authentication.getName())));
  }

  @Operation(
      summary = "Delete a specific search history item",
      description =
          "Endpoint responsible for deleting a specific search history item based on its ID.",
      security = {@SecurityRequirement(name = "BearerAuth")})
  @ApiResponses({
    @ApiResponse(
        responseCode = "204",
        description = "Successful response - History item deleted successfully"),
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
        description = "Not found - History item not found or does not belong to the user",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @DeleteMapping(ApiPaths.History.HISTORY_ITEM)
  public ResponseEntity<Void> deleteHistoryItem(
      Authentication authentication,
      @Parameter(
              description = "Unique identifier of the history item to delete",
              required = true,
              example = "240e8d5c-2532-4006-8d0a-6d33052ba25b")
          @PathVariable
          UUID itemId) {
    historyService.deleteHistoryItem(itemId, UUID.fromString(authentication.getName()));
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Clear all search history",
      description =
          "Endpoint responsible for clearing all search history items for the authenticated user.",
      security = {@SecurityRequirement(name = "BearerAuth")})
  @ApiResponses({
    @ApiResponse(
        responseCode = "204",
        description = "Successful response - All history items cleared successfully"),
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
                schema = @Schema(implementation = ErrorResponse.class)))
  })
  @DeleteMapping(ApiPaths.History.HISTORY)
  public ResponseEntity<Void> clearHistory(Authentication authentication) {
    historyService.clearHistory(UUID.fromString(authentication.getName()));
    return ResponseEntity.noContent().build();
  }
}
