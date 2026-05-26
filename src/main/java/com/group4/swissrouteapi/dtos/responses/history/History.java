package com.group4.swissrouteapi.dtos.responses.history;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

/**
 * History
 *
 * <p>Immutable DTO representing a single search history record. Stores details about a user's
 * search including origin, destination, result count, and timestamp.
 */
@Value
@Builder
public class History {
  UUID id;
  String origin;
  String destination;
  Integer resultCount;
  Instant searchedAt;
}
