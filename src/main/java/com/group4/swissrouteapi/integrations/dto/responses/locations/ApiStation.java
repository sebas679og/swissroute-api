package com.group4.swissrouteapi.integrations.dto.responses.locations;

/**
 * Station
 *
 * <p>Immutable data record representing a transport station.
 *
 * <p>Contains identifying information such as ID and name, along with metadata including score,
 * geographic {@link ApiCoordinate}, distance, and an associated icon.
 *
 * <p>Implemented as a Java {@code record}, providing concise syntax, immutability, and built-in
 * methods such as {@code equals}, {@code hashCode}, and {@code toString}.
 */
public record ApiStation(
    String id,
    String name,
    Double score,
    ApiCoordinate coordinate,
    Integer distance,
    String icon) {}
