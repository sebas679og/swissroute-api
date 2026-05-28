package com.group4.swissrouteapi.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StationRequest {

    @NotBlank(message = "The station ID is required")
    private String externalStationId;

    @NotBlank(message = "The name of the station is required")
    private String stationName;
}
