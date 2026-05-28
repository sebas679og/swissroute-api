package com.group4.swissrouteapi.services;

import com.group4.swissrouteapi.dtos.requests.StationRequest;
import com.group4.swissrouteapi.dtos.responses.favorites.FavStationsResponse;
import com.group4.swissrouteapi.dtos.responses.favorites.StationResponse;
import java.util.UUID;

public interface FavoriteStationService {

  StationResponse addFavoriteStation(UUID userId, StationRequest request);

  FavStationsResponse getFavoriteStation(UUID userId);

  void removeFavoriteStation(UUID userId, String externalStationId);
}
