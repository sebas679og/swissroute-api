package com.group4.swissrouteapi.integrations.dto.responses.connections;

/**
 * ApiPrognosis
 *
 * <p>Record representing prognosis information from the transport API. Holds platform details,
 * arrival and departure times, and seating capacity.
 */
public record ApiPrognosis(
    String platform, String arrival, String departure, Integer capacity1st, Integer capacity2nd) {}
