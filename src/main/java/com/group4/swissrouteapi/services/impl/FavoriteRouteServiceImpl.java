package com.group4.swissrouteapi.services.impl;

import com.group4.swissrouteapi.dtos.requests.FavoriteRouteRequest;
import com.group4.swissrouteapi.dtos.requests.RouteUpdateRequest;
import com.group4.swissrouteapi.dtos.responses.favorites.RouteResponse;
import com.group4.swissrouteapi.dtos.responses.favorites.RoutesResponse;
import com.group4.swissrouteapi.exceptions.BadRequestException;
import com.group4.swissrouteapi.models.FavoriteRouteEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.services.FavoriteRouteService;
import com.group4.swissrouteapi.services.components.UserFinder;
import com.group4.swissrouteapi.services.processors.FavoriteRouteProcessor;
import com.group4.swissrouteapi.utils.mappers.FavoriteRouteMapper;
import java.util.List;
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
  public RouteResponse addFavoriteRoute(UUID userId, FavoriteRouteRequest request) {
    UserEntity user = getUser(userId);
    return favoriteRouteMapper.toFavoriteRouteResponse(
        favoriteRouteProcessor.saveFavoriteRoute(
            user,
            request.getName(),
            request.getOrigin(),
            request.getDestination(),
            request.getTransportationType()));
  }

  @Override
  public RoutesResponse getFavoriteRoutes(UUID userId) {
    UserEntity user = getUser(userId);
    List<FavoriteRouteEntity> routes = favoriteRouteProcessor.getAllFavoriteRoutes(user.getId());
    return RoutesResponse.builder()
        .favoriteRoutes(routes.stream().map(favoriteRouteMapper::toFavoriteRouteResponse).toList())
        .build();
  }

  @Override
  public RouteResponse updateFavoriteRoute(UUID userId, UUID routeId, RouteUpdateRequest request) {
    if (request == null || request.isEmpty()) {
      throw new BadRequestException("The request cannot be completely empty or null");
    }
    UserEntity user = getUser(userId);
    return favoriteRouteMapper.toFavoriteRouteResponse(
        favoriteRouteProcessor.updateFavoriteRoute(
            user.getId(),
            routeId,
            request.getName(),
            request.getOrigin(),
            request.getDestination(),
            request.getTransportationType()));
  }

  @Override
  public void deleteFavoriteRoute(UUID userId, UUID routeId) {
    UserEntity user = getUser(userId);
    favoriteRouteProcessor.deleteFavoriteRoute(user.getId(), routeId);
  }

  private UserEntity getUser(UUID userId) {
    return userFinder.findById(userId);
  }
}
