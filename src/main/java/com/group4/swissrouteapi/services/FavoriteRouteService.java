package com.group4.swissrouteapi.services;

import com.group4.swissrouteapi.dtos.requests.FavoriteRouteRequest;
import com.group4.swissrouteapi.dtos.requests.RouteUpdateRequest;
import com.group4.swissrouteapi.dtos.responses.favorites.RouteResponse;
import com.group4.swissrouteapi.dtos.responses.favorites.RoutesResponse;

import java.util.UUID;

/**
 * FavoriteRouteService
 *
 * <p>Service interface defining operations for managing a user's favorite routes.
 */
public interface FavoriteRouteService {

  RouteResponse addFavoriteRoute(UUID userId, FavoriteRouteRequest request);

  RoutesResponse getFavoriteRoutes(UUID userId);

  RouteResponse updateFavoriteRoute(UUID userId, UUID routeId, RouteUpdateRequest request);

  void deleteFavoriteRoute(UUID userId, UUID routeId);
}
