package com.group4.swissrouteapi.dtos.responses.favorites;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class StationResponse {
    UUID id;
    String externalStationId;
    String stationName;
    Instant createdAt;
}
