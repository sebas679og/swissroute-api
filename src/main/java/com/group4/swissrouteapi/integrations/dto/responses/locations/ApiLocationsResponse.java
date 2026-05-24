package com.group4.swissrouteapi.integrations.dto.responses.locations;

import java.util.List;

/**
 * LocationsResponse
 *
 * <p>Immutable data record representing the response returned from a transport locations query.
 *
 * <p>Encapsulates a list of {@link ApiStation} objects that match the provided search criteria.
 *
 * <p>Implemented as a Java {@code record}, providing concise syntax, immutability, and built-in
 * methods such as {@code equals}, {@code hashCode}, and {@code toString}.
 */
public record ApiLocationsResponse(List<ApiStation> stations) {}
