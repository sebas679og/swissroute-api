package com.group4.swissrouteapi.services.processors;

import com.group4.swissrouteapi.exceptions.ConflictException;
import com.group4.swissrouteapi.exceptions.NotFoundException;
import com.group4.swissrouteapi.models.FavoriteStationEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.repositories.FavoriteStationsRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * StationsProcessor
 *
 * <p>Spring service component responsible for managing user favorite stations.
 */
@Service
@RequiredArgsConstructor
public class StationsProcessor {

  private final FavoriteStationsRepository favoriteStationsRepository;

  /**
   * Adds a new favorite station for the given user.
   *
   * <p>Validates that the station is not already registered for the user. If a duplicate is found,
   * a {@link ConflictException} is thrown.
   *
   * @param user the {@link UserEntity} who owns the station
   * @param externalStationId external identifier of the station
   * @param stationName human-readable name of the station
   * @return the persisted {@link FavoriteStationEntity} instance
   * @throws ConflictException if the station is already registered for the user
   */
  @Transactional
  public FavoriteStationEntity addFavoriteStation(
      UserEntity user, String externalStationId, String stationName) {
    if (favoriteStationsRepository.existsByUserIdAndExternalStationId(
        user.getId(), externalStationId)) {
      throw new ConflictException("The station is already registered.");
    }
    return favoriteStationsRepository.save(
        FavoriteStationEntity.builder()
            .user(user)
            .externalStationId(externalStationId)
            .stationName(stationName)
            .build());
  }

  @Transactional(readOnly = true)
  public List<FavoriteStationEntity> getFavoriteStations(UUID userId) {
    return favoriteStationsRepository.findByUserId(userId);
  }

  /**
   * Deletes a favorite station for the given user.
   *
   * <p>Validates that the station exists and belongs to the user before deletion. If the station
   * does not exist, a {@link NotFoundException} is thrown.
   *
   * @param userId unique identifier of the user
   * @param externalStationId external identifier of the station to delete
   * @throws NotFoundException if the station does not exist for the user
   */
  @Transactional
  public void deleteFavoriteStation(UUID userId, String externalStationId) {
    FavoriteStationEntity station =
        favoriteStationsRepository
            .findByUserIdAndExternalStationId(userId, externalStationId)
            .orElseThrow(() -> new NotFoundException("Station not found"));
    favoriteStationsRepository.delete(station);
  }
}
