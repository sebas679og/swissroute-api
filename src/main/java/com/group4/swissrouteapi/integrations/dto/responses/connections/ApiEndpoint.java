package com.group4.swissrouteapi.integrations.dto.responses.connections;

import com.group4.swissrouteapi.integrations.dto.responses.ApiStation;

import java.time.OffsetDateTime;

/**
 * ApiEndpoint
 *
 * <p>Record representing a transport API endpoint detail. Holds station information, arrival and
 * departure times, delay, platform, prognosis, real-time availability, and location data.
 */
public record ApiEndpoint(
    ApiStation station,
    OffsetDateTime arrival,
    Long arrivalTimestamp,
    OffsetDateTime departure,
    Long departureTimestamp,
    Integer delay,
    String platform,
    ApiPrognosis prognosis,
    Object realtimeAvailability,
    ApiStation location) {}
