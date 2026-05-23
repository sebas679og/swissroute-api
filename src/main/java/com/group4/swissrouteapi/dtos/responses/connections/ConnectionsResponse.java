package com.group4.swissrouteapi.dtos.responses.connections;

import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * ConnectionsResponse
 *
 * <p>Immutable class representing a transport connections response. Holds a list of {@link
 * Connection} objects.
 *
 * <p>Built with Lombok {@link lombok.Value} and {@link lombok.Builder} for immutability and easy
 * construction.
 */
@Value
@Builder
public class ConnectionsResponse {
  List<Connection> connections;
}
