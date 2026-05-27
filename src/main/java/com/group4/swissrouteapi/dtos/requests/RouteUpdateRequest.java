package com.group4.swissrouteapi.dtos.requests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.group4.swissrouteapi.utils.deserializer.TransportationTypeDeserializer;
import com.group4.swissrouteapi.utils.enums.TransportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * RouteUpdateRequest
 *
 * <p>DTO representing a request to update an existing favorite route.
 *
 * <p>Contains optional fields for route details (name, origin, destination, and transport type).
 * Fields may be partially provided to update only specific attributes of a route.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteUpdateRequest {

  private String name;
  private String origin;
  private String destination;

  @JsonDeserialize(using = TransportationTypeDeserializer.class)
  private TransportType transportType;

  /**
   * Checks whether the request contains no meaningful update data.
   *
   * <p>Returns {@code true} if all fields are either {@code null} or blank, and {@code false}
   * otherwise.
   *
   * @return {@code true} if the request has no update values, {@code false} otherwise
   */
  @JsonIgnore
  public boolean isEmpty() {
    return (name == null || name.isBlank())
        && (origin == null || origin.isBlank())
        && (destination == null || destination.isBlank())
        && transportType == null;
  }
}
