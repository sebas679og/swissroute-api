package com.group4.swissrouteapi.services.processors;

import com.group4.swissrouteapi.exceptions.NotFoundException;
import com.group4.swissrouteapi.models.FavoriteRouteEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.repositories.FavoriteRouteRepository;
import com.group4.swissrouteapi.repositories.UserRepository;
import com.group4.swissrouteapi.utils.enums.TransportationType;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
  private final UserRepository userRepository;

  /**
   * Persists a new favorite route for a given user.
   *
   * <p>Validates that the user exists before associating the route. Builds a {@link
   * FavoriteRouteEntity} with the provided details and saves it using {@link
   * FavoriteRouteRepository}.
   *
   * @param userId unique identifier of the user who owns the route
   * @param name descriptive name of the favorite route
   * @param origin origin location of the route
   * @param destination destination location of the route
   * @param transportType type of transportation associated with the route
   * @return the persisted {@link FavoriteRouteEntity} instance
   * @throws NotFoundException if the user does not exist
   */
  @Transactional
  public FavoriteRouteEntity saveFavoriteRoute(
      UUID userId,
      String name,
      String origin,
      String destination,
      TransportationType transportType) {
    UserEntity user = searchUser(userId);
    return favoriteRouteRepository.save(
        FavoriteRouteEntity.builder()
            .user(user)
            .name(name)
            .origin(origin)
            .destination(destination)
            .transportType(transportType)
            .build());
  }

  private UserEntity searchUser(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));
  }
}
