package com.group4.swissrouteapi.services.impl;

import com.group4.swissrouteapi.dtos.requests.StationBoardQueryParams;
import com.group4.swissrouteapi.dtos.requests.StationRequest;
import com.group4.swissrouteapi.dtos.responses.board.StationsBoardResponse;
import com.group4.swissrouteapi.dtos.responses.favorites.FavStationsResponse;
import com.group4.swissrouteapi.dtos.responses.favorites.StationResponse;
import com.group4.swissrouteapi.models.FavoriteStationEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.services.FavoriteStationService;
import com.group4.swissrouteapi.services.StationBoardService;
import com.group4.swissrouteapi.services.components.UserFinder;
import com.group4.swissrouteapi.services.processors.StationsProcessor;
import com.group4.swissrouteapi.utils.mappers.StationMapper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * FavoriteStationServiceImpl
 *
 * <p>Concrete implementation of the {@link FavoriteStationService} interface.
 */
@Service
@RequiredArgsConstructor
public class FavoriteStationServiceImpl implements FavoriteStationService {

  private final StationMapper stationMapper;
  private final UserFinder userFinder;
  private final StationsProcessor stationsProcessor;
  private final StationBoardService stationBoardService;

  @Override
  public StationResponse addFavoriteStation(UUID userId, StationRequest request) {
    UserEntity user = getUser(userId);
    return stationMapper.toStationResponse(
        stationsProcessor.addFavoriteStation(
            user, request.getExternalStationId(), request.getStationName()));
  }

  @Override
  public FavStationsResponse getFavoriteStation(UUID userId) {
    UserEntity user = getUser(userId);
    List<FavoriteStationEntity> stations = stationsProcessor.getFavoriteStations(user.getId());
    return FavStationsResponse.builder()
        .favoriteStations(stations.stream().map(stationMapper::toStationResponse).toList())
        .build();
  }

  @Override
  public void removeFavoriteStation(UUID userId, String externalStationId) {
    UserEntity user = getUser(userId);
    stationsProcessor.deleteFavoriteStation(user.getId(), externalStationId);
  }

  @Override
  public StationsBoardResponse getStationBoardByFavoriteStation(
      UUID userid, String externalStationId) {
    UserEntity user = getUser(userid);
    FavoriteStationEntity entity =
        stationsProcessor.getNameAndExternalStationId(user.getId(), externalStationId);

    StationBoardQueryParams queryParams =
        StationBoardQueryParams.builder()
            .station(entity.getStationName())
            .id(entity.getExternalStationId())
            .build();

    return stationBoardService.getStationBoards(queryParams);
  }

  private UserEntity getUser(UUID userId) {
    return userFinder.findById(userId);
  }
}
