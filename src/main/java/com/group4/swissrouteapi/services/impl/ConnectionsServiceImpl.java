package com.group4.swissrouteapi.services.impl;

import com.group4.swissrouteapi.dtos.requests.ConnectionsQueryParams;
import com.group4.swissrouteapi.dtos.responses.connections.ConnectionsResponse;
import com.group4.swissrouteapi.exceptions.NotFoundException;
import com.group4.swissrouteapi.integrations.TransportClient;
import com.group4.swissrouteapi.integrations.dto.responses.connections.ApiConnectionsResponse;
import com.group4.swissrouteapi.services.ConnectionsService;
import com.group4.swissrouteapi.utils.mappers.ConnectionsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * ConnectionsServiceImpl
 *
 * <p>Spring service implementation of {@link ConnectionsService}. Handles retrieval of transport
 * connections by delegating requests to the {@link TransportClient} and mapping API responses into
 * application DTOs using {@link ConnectionsMapper}.
 */
@Service
@RequiredArgsConstructor
public class ConnectionsServiceImpl implements ConnectionsService {

  private final TransportClient transportClient;
  private final ConnectionsMapper connectionsMapper;

  @Override
  public ConnectionsResponse getConnections(ConnectionsQueryParams requestParams) {
    ApiConnectionsResponse api =
        transportClient.getConnections(
            requestParams.getFrom(),
            requestParams.getTo(),
            requestParams.getDate(),
            requestParams.getTime(),
            requestParams.getTransportations());

    if (api == null || api.connections() == null || api.connections().isEmpty()) {
      throw new NotFoundException("No connections found for the given parameters");
    }

    return ConnectionsResponse.builder()
        .connections(
            api.connections().stream().map(connectionsMapper::toConnectionResponse).toList())
        .build();
  }
}
