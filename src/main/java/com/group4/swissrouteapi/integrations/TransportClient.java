package com.group4.swissrouteapi.integrations;

import com.group4.swissrouteapi.integrations.dto.responses.locations.ApiLocationsResponse;

/**
 * TransportClient.
 *
 * <p>Client interface for interacting with the transport API.
 */
public interface TransportClient {

  ApiLocationsResponse getLocations(String query);
}
