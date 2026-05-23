package com.group4.swissrouteapi.integrations.dto.responses.connections;

/**
 * ApiCoordinate
 *
 * <p>Record representing a coordinate from the transport API. Holds the type of coordinate and its
 * X and Y values.
 */
public record ApiCoordinate(String type, Double x, Double y) {}
