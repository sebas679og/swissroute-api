package com.group4.swissrouteapi.dtos.responses.connections;

import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * ConnectionResponse
 *
 * <p>Immutable class representing a transport connection response. Holds origin, destination,
 * duration, products, and journey sections.
 *
 * <p>Built with Lombok {@link lombok.Value} and {@link lombok.Builder} for immutability and easy
 * construction.
 */
@Value
@Builder
public class ConnectionResponse {
  String origin;
  String destination;
  String duration;
  List<String> products;
  List<Section> sections;
}
