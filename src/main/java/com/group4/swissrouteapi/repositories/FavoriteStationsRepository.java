package com.group4.swissrouteapi.repositories;

import com.group4.swissrouteapi.models.FavoriteStationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteStationsRepository extends JpaRepository<FavoriteStationEntity, UUID> {

    boolean existsByUserIdAndExternalStationId(UUID userId, String externalStationId);

    List<FavoriteStationEntity> findByUserId(UUID userId);

    Optional<FavoriteStationEntity> findByIdAndUserId(UUID userId, UUID favoriteStationId);
}
