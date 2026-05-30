package com.group4.swissrouteapi.repositories;

import com.group4.swissrouteapi.models.FavoriteRouteEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * FavoriteRouteRepository
 *
 * <p>Spring Data JPA repository interface for managing persistence operations related to {@link
 * FavoriteRouteEntity}.
 */
public interface FavoriteRouteRepository extends JpaRepository<FavoriteRouteEntity, UUID> {

  boolean existsByUserIdAndName(UUID userId, String name);

  boolean existsByUserIdAndNameAndIdNot(UUID userId, String name, UUID routeId);

  Optional<FavoriteRouteEntity> findByUserIdAndId(UUID userId, UUID routeId);

  List<FavoriteRouteEntity> findByUserId(UUID userId);
}
