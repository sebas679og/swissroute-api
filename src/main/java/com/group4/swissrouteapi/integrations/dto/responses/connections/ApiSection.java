package com.group4.swissrouteapi.integrations.dto.responses.connections;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.group4.swissrouteapi.integrations.dto.responses.ApiEndpoint;
import com.group4.swissrouteapi.integrations.dto.responses.ApiJourney;

/**
 * ApiSection
 *
 * <p>Record representing a section of a transport journey. Holds journey details, optional walking
 * information, and departure/arrival endpoints.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiSection(
        ApiJourney journey, Object walk, ApiEndpoint departure, ApiEndpoint arrival) {}
