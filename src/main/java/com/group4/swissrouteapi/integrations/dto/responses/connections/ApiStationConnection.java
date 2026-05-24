package com.group4.swissrouteapi.integrations.dto.responses.connections;

/**
 * ApiStation
 *
 * <p>Record representing a station from the transport API. Holds station ID, name, score,
 * coordinates, and distance.
 */
public record ApiStationConnection(
    String id, String name, Double score, ApiCoordinateConnection coordinate, Integer distance) {}
