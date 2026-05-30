package com.group4.swissrouteapi.services.impl;

import com.group4.swissrouteapi.dtos.requests.ConnectionsQueryParams;
import com.group4.swissrouteapi.dtos.responses.connections.Connection;
import com.group4.swissrouteapi.dtos.responses.connections.ConnectionsResponse;
import com.group4.swissrouteapi.exceptions.NotFoundException;
import com.group4.swissrouteapi.integrations.TransportClient;
import com.group4.swissrouteapi.integrations.dto.responses.connections.ApiConnectionsResponse;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.services.ConnectionsService;
import com.group4.swissrouteapi.services.components.UserFinder;
import com.group4.swissrouteapi.services.processors.HistoryProcessor;
import com.group4.swissrouteapi.utils.mappers.ConnectionsMapper;
import java.util.List;
import java.util.UUID;
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
  private final HistoryProcessor historyProcessor;
  private final UserFinder userFinder;

  @Override
  public ConnectionsResponse getConnections(ConnectionsQueryParams requestParams, UUID userId) {
    ApiConnectionsResponse api =
        transportClient.getConnections(
            requestParams.getFrom(),
            requestParams.getTo(),
            requestParams.getDate(),
            requestParams.getTime(),
            requestParams.getTransportations(),
            requestParams.getVia());

    if (api == null || api.connections() == null || api.connections().isEmpty()) {
      throw new NotFoundException("No connections found for the given parameters");
    }

    List<Connection> connections =
        api.connections().stream().map(connectionsMapper::toConnectionResponse).toList();

    UserEntity user = userFinder.findById(userId);

    historyProcessor.saveHistory(
        requestParams.getFrom(), requestParams.getTo(), connections.size(), user);

    return ConnectionsResponse.builder().connections(connections).build();
  }
}
