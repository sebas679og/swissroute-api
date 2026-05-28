package com.group4.swissrouteapi.dtos.requests;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StationRequest {
    private String externalStationId;
    private String stationName;
}
