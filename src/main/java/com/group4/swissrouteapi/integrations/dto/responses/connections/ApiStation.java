package com.group4.swissrouteapi.integrations.dto.responses.connections;

/**
 * ApiStation
 *
 * <p>Record representing a station from the transport API. Holds station ID, name, score,
 * coordinates, and distance.
 */
public record ApiStation(
    String id, String name, Double score, ApiCoordinate coordinate, Integer distance) {}
