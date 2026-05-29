package com.group4.swissrouteapi.integrations;

import com.group4.swissrouteapi.integrations.dto.responses.connections.ApiConnectionsResponse;
import com.group4.swissrouteapi.integrations.dto.responses.locations.ApiLocationsResponse;
import com.group4.swissrouteapi.integrations.dto.responses.stationboard.ApiStationBoardResponse;
import com.group4.swissrouteapi.utils.enums.TransportType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * TransportClient.
 *
 * <p>Client interface for interacting with the transport API.
 */
public interface TransportClient {

  ApiLocationsResponse getLocationsByQuery(String query);

  ApiLocationsResponse getLocationsByCoordinates(double latitude, double longitude);

  ApiConnectionsResponse getConnections(
      String from, String to, LocalDate date, LocalTime time, List<TransportType> transportType);

  ApiStationBoardResponse getStationBoard(String id, String name, Integer limit, List<TransportType> transportType);
}
