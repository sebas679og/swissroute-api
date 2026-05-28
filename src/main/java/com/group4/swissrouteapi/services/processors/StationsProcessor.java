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

@Service
@RequiredArgsConstructor
public class StationsProcessor {

  private final FavoriteStationsRepository favoriteStationsRepository;

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

  @Transactional
  public void deleteFavoriteStation(UUID userId, String externalStationId) {
    FavoriteStationEntity station =
        favoriteStationsRepository
            .findByUserIdAndExternalStationId(userId, externalStationId)
            .orElseThrow(() -> new NotFoundException("Station not found"));
    favoriteStationsRepository.delete(station);
  }
}
