package com.group4.swissrouteapi.services.impl;

import com.group4.swissrouteapi.dtos.requests.FavoriteRouteRequest;
import com.group4.swissrouteapi.dtos.responses.favorites.RegisterRouteResponse;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.services.FavoriteRouteService;
import com.group4.swissrouteapi.services.components.UserFinder;
import com.group4.swissrouteapi.services.processors.FavoriteRouteProcessor;
import com.group4.swissrouteapi.utils.mappers.FavoriteRouteMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * FavoriteRouteServiceImpl
 *
 * <p>Concrete implementation of the {@link FavoriteRouteService} interface.
 */
@Service
@RequiredArgsConstructor
public class FavoriteRouteServiceImpl implements FavoriteRouteService {

  private final FavoriteRouteProcessor favoriteRouteProcessor;
  private final UserFinder userFinder;
  private final FavoriteRouteMapper favoriteRouteMapper;

  @Override
  public RegisterRouteResponse addFavoriteRoute(UUID userId, FavoriteRouteRequest request) {
    UserEntity user = userFinder.findById(userId);
    return favoriteRouteMapper.toFavoriteRouteResponse(
        favoriteRouteProcessor.saveFavoriteRoute(
            user,
            request.getName(),
            request.getOrigin(),
            request.getDestination(),
            request.getTransportationType()));
  }
}
