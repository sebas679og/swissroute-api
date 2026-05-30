package com.group4.swissrouteapi.dtos.responses.board;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Value;

/**
 * StationBoard
 *
 * <p>Immutable DTO representing station board information for a specific service.
 */
@Value
@Builder
public class StationBoard {
  String serviceName;
  String category;
  String destinationName;
  OffsetDateTime departureTime;
}
