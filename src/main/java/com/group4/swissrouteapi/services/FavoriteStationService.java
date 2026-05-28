package com.group4.swissrouteapi.services;

import com.group4.swissrouteapi.dtos.requests.StationRequest;
import com.group4.swissrouteapi.dtos.responses.favorites.StationResponse;
import com.group4.swissrouteapi.dtos.responses.favorites.StationsResponse;

import java.util.UUID;

public interface FavoriteStationService {

    StationResponse addFavoriteStation(UUID userId, StationRequest request);

    StationsResponse getFavoriteStation(UUID userId);

    void removeFavoriteStation(UUID userId, UUID favoriteStationId);
}
