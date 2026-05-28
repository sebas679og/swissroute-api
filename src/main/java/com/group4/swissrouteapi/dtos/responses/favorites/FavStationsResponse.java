package com.group4.swissrouteapi.dtos.responses.favorites;

import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * FavStationsResponse
 *
 * <p>Immutable DTO representing the response payload containing a collection of the authenticated
 * user's favorite stations.
 */
@Value
@Builder
public class FavStationsResponse {
  List<StationResponse> favoriteStations;
}
