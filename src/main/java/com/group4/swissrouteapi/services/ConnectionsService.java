package com.group4.swissrouteapi.services;

import com.group4.swissrouteapi.dtos.requests.ConnectionsQueryParams;
import com.group4.swissrouteapi.dtos.responses.connections.ConnectionsResponse;
import java.util.UUID;

/**
 * ConnectionsService
 *
 * <p>Service interface for retrieving transport connections. Defines the contract for searching
 * connections between stations based on provided query parameters.
 *
 * <p>Implementations are expected to handle validation, integration with external transport APIs,
 * and mapping to response DTOs.
 */
public interface ConnectionsService {

  ConnectionsResponse getConnections(ConnectionsQueryParams requestParams, UUID userId);
}
