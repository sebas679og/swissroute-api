package com.group4.swissrouteapi.services.impl;

import com.group4.swissrouteapi.dtos.requests.StationsQueryParams;
import com.group4.swissrouteapi.dtos.responses.stations.StationsResponse;
import com.group4.swissrouteapi.exceptions.NotFoundException;
import com.group4.swissrouteapi.integrations.TransportClient;
import com.group4.swissrouteapi.integrations.dto.responses.ApiStation;
import com.group4.swissrouteapi.integrations.dto.responses.locations.ApiLocationsResponse;
import com.group4.swissrouteapi.services.StationService;
import com.group4.swissrouteapi.utils.mappers.StationMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * StationServiceImpl
 *
 * <p>Concrete implementation of the {@link StationService} interface.
 *
 * <p>Provides business logic for retrieving and mapping transport stations based on query
 * parameters. Integrates with the external transport API through {@link TransportClient} and
 * transforms results into domain-specific responses using {@link StationMapper}.
 *
 * <p>Annotated with {@link org.springframework.stereotype.Service} for Spring component scanning
 * and {@link lombok.RequiredArgsConstructor} to enable constructor-based dependency injection.
 *
 * <p>Responsible for handling station search requests, validating results, and throwing {@link
 * NotFoundException} when no stations match the given query.
 */
@Service
@RequiredArgsConstructor
public class StationServiceImpl implements StationService {

  private final TransportClient transportClient;
  private final StationMapper stationMapper;

  @Override
  public StationsResponse getStations(StationsQueryParams requestParams) {
    if (requestParams.getLatitude() != null && requestParams.getLongitude() != null) {
      return getStationsByCoordinates(requestParams.getLatitude(), requestParams.getLongitude());
    }
    return getStationsByName(requestParams.getQuery());
  }

  private StationsResponse getStationsByName(String name) {
    ApiLocationsResponse api = transportClient.getLocationsByQuery(name);
    List<ApiStation> apiStations = api.stations();

    if (apiStations == null || apiStations.isEmpty()) {
      throw new NotFoundException("No stations found with the name: " + name);
    }

    return StationsResponse.builder()
        .stations(apiStations.stream().map(stationMapper::toStations).toList())
        .build();
  }

  private StationsResponse getStationsByCoordinates(double latitude, double longitude) {
    ApiLocationsResponse api = transportClient.getLocationsByCoordinates(latitude, longitude);
    List<ApiStation> apiStations = api.stations();

    if (apiStations == null || apiStations.isEmpty()) {
      throw new NotFoundException(
          "No stations found at the coordinates: (" + latitude + ", " + longitude + ")");
    }

    return StationsResponse.builder()
        .stations(apiStations.stream().map(stationMapper::toStations).toList())
        .build();
  }
}
