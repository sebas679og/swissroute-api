package com.group4.swissrouteapi.dtos.responses.favorites;

import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * RoutesResponse
 *
 * <p>Immutable DTO representing the response payload containing a collection of the user's favorite
 * routes.
 */
@Value
@Builder
public class RoutesResponse {
  List<RouteResponse> favoriteRoutes;
}
