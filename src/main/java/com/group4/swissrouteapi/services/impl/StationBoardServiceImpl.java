package com.group4.swissrouteapi.services.impl;

import com.group4.swissrouteapi.dtos.requests.StationBoardQueryParams;
import com.group4.swissrouteapi.dtos.responses.board.StationsBoardResponse;
import com.group4.swissrouteapi.exceptions.NotFoundException;
import com.group4.swissrouteapi.integrations.TransportClient;
import com.group4.swissrouteapi.integrations.dto.responses.stationboard.ApiStationBoardResponse;
import com.group4.swissrouteapi.services.StationBoardService;
import com.group4.swissrouteapi.utils.mappers.StationBoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * StationBoardServiceImpl
 *
 * <p>Concrete implementation of the {@link StationBoardService} interface.
 */
@Service
@RequiredArgsConstructor
public class StationBoardServiceImpl implements StationBoardService {

  private final TransportClient transportClient;
  private final StationBoardMapper stationBoardMapper;

  @Override
  public StationsBoardResponse getStationBoards(StationBoardQueryParams requestParams) {
    ApiStationBoardResponse api =
        transportClient.getStationBoard(
            requestParams.getStation(),
            requestParams.getId(),
            requestParams.getLimit(),
            requestParams.getTransportType());

    if (api == null || api.stationBoard() == null || api.stationBoard().isEmpty()) {
      throw new NotFoundException("Station Board not found.");
    }
    return StationsBoardResponse.builder()
        .stationBoards(api.stationBoard().stream().map(stationBoardMapper::toStationBoard).toList())
        .build();
  }
}
