package com.group4.swissrouteapi.services;

import com.group4.swissrouteapi.dtos.requests.StationsQueryParams;
import com.group4.swissrouteapi.dtos.responses.StationsResponse;

/**
 * StationService
 *
 * <p>Service interface defining operations related to transport stations.
 *
 * <p>Provides the contract for retrieving stations by name using query parameters. Implementations
 * are expected to integrate with external transport APIs and return structured responses.
 */
public interface StationService {

  StationsResponse getStationsByName(StationsQueryParams requestParams);
}
