package com.group4.swissrouteapi.controllers;

import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.dtos.requests.StationsQueryParams;
import com.group4.swissrouteapi.dtos.responses.StationsResponse;
import com.group4.swissrouteapi.services.StationService;
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
public class StationController {

  private final StationService stationService;

  @GetMapping(ApiPaths.Station.STATIONS)
  public ResponseEntity<StationsResponse> getStations(
      @ParameterObject @Valid @ModelAttribute StationsQueryParams queryParams) {
    return ResponseEntity.status(HttpStatus.OK).body(stationService.getStationsByName(queryParams));
  }
}
