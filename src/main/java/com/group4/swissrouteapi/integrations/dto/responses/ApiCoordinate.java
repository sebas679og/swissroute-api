package com.group4.swissrouteapi.integrations.dto.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Coordinate
 *
 * <p>Immutable data record representing a spatial coordinate.
 *
 * <p>Contains a type descriptor along with X and Y values expressed as {@link Double}. Useful for
 * modeling positions, points, or geographic references in transport or mapping contexts.
 *
 * <p>Implemented as a Java {@code record}, providing concise syntax, immutability, and built-in
 * methods such as {@code equals}, {@code hashCode}, and {@code toString}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiCoordinate(String type, Double x, Double y) {}
