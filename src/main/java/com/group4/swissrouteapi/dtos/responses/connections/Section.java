package com.group4.swissrouteapi.dtos.responses.connections;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Value;

/**
 * Section
 *
 * <p>Immutable class representing a section of a transport journey. Holds category, number,
 * operator, destination, departure and arrival station details, times, and platform information.
 *
 * <p>Built with Lombok {@link lombok.Value} and {@link lombok.Builder} for immutability and easy
 * construction.
 */
@Value
@Builder
public class Section {
  String category;
  String number;
  String operator;
  String destination;
  String departureStation;
  OffsetDateTime departureTime;
  String arrivalStation;
  OffsetDateTime arrivalTime;
  String platform;
}
