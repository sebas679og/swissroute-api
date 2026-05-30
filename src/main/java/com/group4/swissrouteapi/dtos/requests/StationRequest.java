package com.group4.swissrouteapi.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * StationRequest
 *
 * <p>DTO representing the request payload for creating or registering a user's favorite station.
 */
@Getter
@Setter
@Builder
public class StationRequest {

  @NotBlank(message = "The station ID is required")
  private String externalStationId;

  @NotBlank(message = "The name of the station is required")
  private String stationName;
}
