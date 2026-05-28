package com.group4.swissrouteapi.dtos.responses.favorites;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

/**
 * StationResponse
 *
 * <p>Immutable DTO representing the response payload for a user's favorite station.
 */
@Value
@Builder
public class StationResponse {
  String externalStationId;
  String stationName;
  Instant createdAt;
}
