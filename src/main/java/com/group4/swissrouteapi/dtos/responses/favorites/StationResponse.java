package com.group4.swissrouteapi.dtos.responses.favorites;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class StationResponse {
    String externalStationId;
    String stationName;
    Instant createdAt;
}
