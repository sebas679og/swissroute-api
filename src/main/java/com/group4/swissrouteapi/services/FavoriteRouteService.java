package com.group4.swissrouteapi.services;

import com.group4.swissrouteapi.dtos.requests.FavoriteRouteRequest;
import com.group4.swissrouteapi.dtos.responses.favorites.RegisterRouteResponse;
import java.util.UUID;

/**
 * FavoriteRouteService
 *
 * <p>Service interface defining operations for managing a user's favorite routes.
 */
public interface FavoriteRouteService {

  RegisterRouteResponse addFavoriteRoute(UUID userId, FavoriteRouteRequest request);
}
