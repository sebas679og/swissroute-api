package com.group4.swissrouteapi.controllers;

import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.dtos.requests.ConnectionsQueryParams;
import com.group4.swissrouteapi.dtos.responses.connections.ConnectionsResponse;
import com.group4.swissrouteapi.services.ConnectionsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class ConnectionController {

  private final ConnectionsService connectionsService;

  @GetMapping(ApiPaths.Connection.CONNECTIONS)
  public ResponseEntity<ConnectionsResponse> getConnections(
      @ParameterObject @Valid @ModelAttribute ConnectionsQueryParams queryParams) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(connectionsService.getConnections(queryParams));
  }
}
