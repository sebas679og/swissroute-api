package com.group4.swissrouteapi.dtos.responses.stations;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

/**
 * Station
 *
 * <p>Immutable data class representing a transport station.
 *
 * <p>Contains identifying information such as ID and name, along with geographic coordinates
 * (latitude and longitude).
 *
 * <p>Annotated with {@link lombok.Value} to enforce immutability and {@link lombok.Builder} to
 * provide a fluent builder API for object creation.
 */
@Value
@Builder
public class Station {
  String id;
  String name;
  Double latitude;
  Double longitude;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  Integer distance;
}
