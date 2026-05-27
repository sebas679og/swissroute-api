package com.group4.swissrouteapi.services.processors;

import com.group4.swissrouteapi.exceptions.ConflictException;
import com.group4.swissrouteapi.exceptions.NotFoundException;
import com.group4.swissrouteapi.models.FavoriteRouteEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.repositories.FavoriteRouteRepository;
import com.group4.swissrouteapi.utils.enums.TransportationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * FavoriteRouteProcessor
 *
 * <p>Spring service component responsible for handling persistence operations related to a user's
 * favorite routes.
 */
@Service
@RequiredArgsConstructor
public class FavoriteRouteProcessor {

  private final FavoriteRouteRepository favoriteRouteRepository;

  /**
   * Persists a new favorite route for a given user.
   *
   * <p>Validates that the user exists before associating the route. Builds a {@link
   * FavoriteRouteEntity} with the provided details and saves it using {@link
   * FavoriteRouteRepository}.
   *
   * @param user the user entity to which the favorite route will be associated
   * @param name descriptive name of the favorite route
   * @param origin origin location of the route
   * @param destination destination location of the route
   * @param transportType type of transportation associated with the route
   * @return the persisted {@link FavoriteRouteEntity} instance
   */
  @Transactional
  public FavoriteRouteEntity saveFavoriteRoute(
      UserEntity user,
      String name,
      String origin,
      String destination,
      TransportationType transportType) {
    if (favoriteRouteRepository.existsByUserIdAndName(user.getId(), name)) {
      throw new ConflictException("Favorite route name already exists");
    }
    return favoriteRouteRepository.save(
        FavoriteRouteEntity.builder()
            .user(user)
            .name(name)
            .origin(origin)
            .destination(destination)
            .transportType(transportType)
            .build());
  }

  @Transactional(readOnly = true)
  public List<FavoriteRouteEntity> getAllFavoriteRoutes(UUID userId){
    return favoriteRouteRepository.findByUserId(userId);
  }

  @Transactional
  public FavoriteRouteEntity updateFavoriteRoute(
          UUID userId,
          UUID routeId,
          String name,
          String origin,
          String destination,
          TransportationType transportType
  ) {

    FavoriteRouteEntity route = favoriteRouteRepository
            .findByUserIdAndId(userId, routeId)
            .orElseThrow(() -> new NotFoundException("Route not found"));

    if (name != null && favoriteRouteRepository.existsByUserIdAndNameAndIdNot(userId, name, routeId)) {
      throw new ConflictException("Favorite route name already exists");
    }

    if (name != null) {
      route.setName(name);
    }

    if (origin != null) {
      route.setOrigin(origin);
    }

    if (destination != null) {
      route.setDestination(destination);
    }

    if (transportType != null) {
      route.setTransportType(transportType);
    }

    return favoriteRouteRepository.save(route);
  }

  @Transactional
  public void deleteFavoriteRoute(UUID userId, UUID routeId) {
    FavoriteRouteEntity route =
            favoriteRouteRepository.findByUserIdAndId(userId, routeId)
                    .orElseThrow(()-> new NotFoundException("Route not found"));

    favoriteRouteRepository.delete(route);
  }
}
