package com.group4.swissrouteapi.integrations;

import com.group4.swissrouteapi.integrations.dto.responses.locations.ApiLocationsResponse;

/**
 * TransportClient.
 *
 * <p>Client interface for interacting with the transport API.
 */
public interface TransportClient {

  ApiLocationsResponse getLocationsByQuery(String query);

  ApiLocationsResponse getLocationsByCoordinates(double latitude, double longitude);
}
