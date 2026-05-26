package com.group4.swissrouteapi.dtos.requests;

import com.group4.swissrouteapi.utils.enums.TransportationType;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * FavoriteRouteRequest
 *
 * <p>DTO representing a request to create a user's favorite route.
 *
 * <p>Contains mandatory fields for route details (name, origin, destination) and an optional {@link
 * TransportationType} to specify the transport mode.
 */
@Getter
@Setter
@Builder
public class FavoriteRouteRequest {

  @NotBlank(message = "Name is required")
  private String name;

  @NotBlank(message = "Origin is required")
  private String origin;

  @NotBlank(message = "Destination is required")
  private String destination;

  private TransportationType transportationType;
}
