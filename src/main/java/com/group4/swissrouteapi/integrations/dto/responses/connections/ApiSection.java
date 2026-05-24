package com.group4.swissrouteapi.integrations.dto.responses.connections;

/**
 * ApiSection
 *
 * <p>Record representing a section of a transport journey. Holds journey details, optional walking
 * information, and departure/arrival endpoints.
 */
public record ApiSection(
    ApiJourney journey, Object walk, ApiEndpoint departure, ApiEndpoint arrival) {}
