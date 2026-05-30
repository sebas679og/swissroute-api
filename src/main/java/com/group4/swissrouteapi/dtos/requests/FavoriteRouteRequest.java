package com.group4.swissrouteapi.dtos.requests;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.group4.swissrouteapi.utils.deserializer.TransportationTypeDeserializer;
import com.group4.swissrouteapi.utils.enums.TransportType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * FavoriteRouteRequest
 *
 * <p>DTO representing a request to create a user's favorite route.
 *
 * <p>Contains mandatory fields for route details (name, origin, destination) and an optional {@link
 * TransportType} to specify the transport mode.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteRouteRequest {

  @NotBlank(message = "Name is required")
  private String name;

  @NotBlank(message = "Origin is required")
  private String origin;

  @NotBlank(message = "Destination is required")
  private String destination;

  @JsonDeserialize(using = TransportationTypeDeserializer.class)
  private TransportType transportType;
}
