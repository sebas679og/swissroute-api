package com.group4.swissrouteapi.dtos.responses.board;

import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * StationsBoardResponse
 *
 * <p>Immutable DTO representing the response payload containing a collection of station board
 * entries.
 */
@Value
@Builder
public class StationsBoardResponse {
  List<StationBoard> stationBoards;
}
