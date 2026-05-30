package com.group4.swissrouteapi.dtos.responses.favorites;

import com.group4.swissrouteapi.utils.enums.TransportType;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

/**
 * FavoriteRouteResponse
 *
 * <p>Immutable DTO representing the response payload for a user's favorite route.
 *
 * <p>Contains route details such as identifier, name, destination, transport type, and creation
 * timestamp. Designed for API responses to provide clients with consistent and structured route
 * information.
 */
@Value
@Builder
public class RouteResponse {
  UUID id;
  String name;
  String origin;
  String destination;
  TransportType transportType;
  Instant createdAt;
}
