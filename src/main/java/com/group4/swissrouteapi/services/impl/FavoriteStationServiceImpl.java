package com.group4.swissrouteapi.services.impl;

import com.group4.swissrouteapi.dtos.requests.StationRequest;
import com.group4.swissrouteapi.dtos.responses.favorites.StationResponse;
import com.group4.swissrouteapi.dtos.responses.favorites.FavStationsResponse;
import com.group4.swissrouteapi.models.FavoriteStationEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.services.FavoriteStationService;
import com.group4.swissrouteapi.services.components.UserFinder;
import com.group4.swissrouteapi.services.processors.StationsProcessor;
import com.group4.swissrouteapi.utils.mappers.StationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavoriteStationServiceImpl implements FavoriteStationService {

    private final StationMapper stationMapper;
    private final UserFinder userFinder;
    private final StationsProcessor stationsProcessor;

    @Override
    public StationResponse addFavoriteStation(UUID userId, StationRequest request) {
        UserEntity user =
                getUser(userId);
        return stationMapper.toStationResponse(
                stationsProcessor.addFavoriteStation(
                        user,
                        request.getExternalStationId(),
                        request.getStationName()
                )
        );
    }

    @Override
    public FavStationsResponse getFavoriteStation(UUID userId) {
        UserEntity user = getUser(userId);
        List<FavoriteStationEntity> stations =
                stationsProcessor.getFavoriteStations(user.getId());
        return FavStationsResponse.builder()
                .favoriteStations(stations.stream()
                        .map(stationMapper::toStationResponse)
                        .toList())
                .build();
    }

    @Override
    public void removeFavoriteStation(UUID userId, String externalStationId) {
        UserEntity user = getUser(userId);
        stationsProcessor.deleteFavoriteStation(user.getId(), externalStationId);
    }

    private UserEntity getUser(UUID userId) {
        return userFinder.findById(userId);
    }
}
