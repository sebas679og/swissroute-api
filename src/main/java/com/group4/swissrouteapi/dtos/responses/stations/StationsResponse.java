package com.group4.swissrouteapi.dtos.responses.stations;

import java.util.List;

import lombok.Builder;
import lombok.Value;

/**
 * StationsResponse
 *
 * <p>Immutable data class representing the response for a station query.
 *
 * <p>Encapsulates a list of {@link Station} objects returned from a search operation or external
 * API call.
 *
 * <p>Annotated with {@link lombok.Value} to enforce immutability and {@link lombok.Builder} to
 * provide a fluent builder API for object creation.
 */
@Value
@Builder
public class StationsResponse {
  List<Station> stations;
}
