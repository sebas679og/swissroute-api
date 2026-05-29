package com.group4.swissrouteapi.integrations.dto.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * ApiPrognosis
 *
 * <p>Record representing prognosis information from the transport API. Holds platform details,
 * arrival and departure times, and seating capacity.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiPrognosis(
    String platform, String arrival, String departure, Integer capacity1st, Integer capacity2nd) {}
