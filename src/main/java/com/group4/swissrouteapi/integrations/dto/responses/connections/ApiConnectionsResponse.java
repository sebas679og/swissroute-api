package com.group4.swissrouteapi.integrations.dto.responses.connections;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.group4.swissrouteapi.integrations.dto.responses.ApiStation;

import java.util.List;

/**
 * ApiConnectionsResponse
 *
 * <p>Record representing a transport API response for station connections. Holds the list of {@link
 * ApiConnection}, origin and destination stations, and related station metadata.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiConnectionsResponse(
    List<ApiConnection> connections,
    ApiStation from,
    ApiStation to,
    ApiStations stations) {}
