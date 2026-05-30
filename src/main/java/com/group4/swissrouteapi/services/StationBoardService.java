package com.group4.swissrouteapi.services;

import com.group4.swissrouteapi.dtos.requests.StationBoardQueryParams;
import com.group4.swissrouteapi.dtos.responses.board.StationsBoardResponse;

/**
 * StationBoardService
 *
 * <p>Service interface defining the contract for retrieving station board information.
 */
public interface StationBoardService {

  StationsBoardResponse getStationBoards(StationBoardQueryParams requestParams);
}
