package com.group4.swissrouteapi.dtos.responses.favorites;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StationResponse {
  String externalStationId;
  String stationName;
  Instant createdAt;
}
