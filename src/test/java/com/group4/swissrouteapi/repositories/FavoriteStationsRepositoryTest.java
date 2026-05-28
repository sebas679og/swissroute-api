package com.group4.swissrouteapi.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.group4.swissrouteapi.AbstractIntegrationTest;
import com.group4.swissrouteapi.models.FavoriteStationEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.providers.FavoriteStationDataProvider;
import com.group4.swissrouteapi.providers.UserDataProvider;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class FavoriteStationsRepositoryTest extends AbstractIntegrationTest {

  @Autowired FavoriteStationsRepository favoriteStationsRepository;
  @Autowired UserRepository userRepository;

  private UserEntity userA;
  private UserEntity userB;

  @BeforeEach
  void setUp() {
    favoriteStationsRepository.deleteAll();
    userRepository.deleteAll();
    userA = userRepository.save(UserDataProvider.createMockUser());
    userB = userRepository.save(UserDataProvider.createAnotherMockUser());
  }

  // ─── existsByUserIdAndExternalStationId ──────────────────────────────────────

  @Test
  void shouldReturnTrueWhenUserHasThatStationFavorited() {
    favoriteStationsRepository.save(FavoriteStationDataProvider.createMockStation(userA));

    assertTrue(
        favoriteStationsRepository.existsByUserIdAndExternalStationId(
            userA.getId(), FavoriteStationDataProvider.EXTERNAL_STATION_ID_A));
  }

  @Test
  void shouldReturnFalseWhenStationIsNotFavoritedByUser() {
    favoriteStationsRepository.save(FavoriteStationDataProvider.createMockStation(userA));

    assertFalse(
        favoriteStationsRepository.existsByUserIdAndExternalStationId(
            userA.getId(), "station-not-saved"));
  }

  @Test
  void shouldReturnFalseWhenRepositoryIsEmpty() {
    assertFalse(
        favoriteStationsRepository.existsByUserIdAndExternalStationId(
            userA.getId(), FavoriteStationDataProvider.EXTERNAL_STATION_ID_A));
  }

  @Test
  void shouldReturnFalseWhenStationIsFavoritedByAnotherUser() {
    favoriteStationsRepository.save(FavoriteStationDataProvider.createMockStation(userA));

    assertFalse(
        favoriteStationsRepository.existsByUserIdAndExternalStationId(
            userB.getId(), FavoriteStationDataProvider.EXTERNAL_STATION_ID_A));
  }

  @Test
  void shouldAllowSameStationToBeFavoritedByDifferentUsers() {
    // Unique constraint is (user_id, external_station_id), so both saves must succeed
    favoriteStationsRepository.save(FavoriteStationDataProvider.createMockStation(userA));
    favoriteStationsRepository.save(FavoriteStationDataProvider.createMockStation(userB));

    assertTrue(
        favoriteStationsRepository.existsByUserIdAndExternalStationId(
            userA.getId(), FavoriteStationDataProvider.EXTERNAL_STATION_ID_A));
    assertTrue(
        favoriteStationsRepository.existsByUserIdAndExternalStationId(
            userB.getId(), FavoriteStationDataProvider.EXTERNAL_STATION_ID_A));
  }

  // ─── findByUserId ────────────────────────────────────────────────────────────

  @Test
  void shouldReturnAllFavoriteStationsForUser() {
    favoriteStationsRepository.save(FavoriteStationDataProvider.createMockStation(userA));
    favoriteStationsRepository.save(FavoriteStationDataProvider.createAnotherMockStation(userA));

    List<FavoriteStationEntity> result = favoriteStationsRepository.findByUserId(userA.getId());

    assertEquals(2, result.size());
  }

  @Test
  void shouldReturnEmptyListWhenUserHasNoFavoriteStations() {
    favoriteStationsRepository.save(FavoriteStationDataProvider.createMockStation(userB));

    List<FavoriteStationEntity> result = favoriteStationsRepository.findByUserId(userA.getId());

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnEmptyListWhenRepositoryIsEmpty() {
    List<FavoriteStationEntity> result = favoriteStationsRepository.findByUserId(userA.getId());

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnOnlyStationsBelongingToRequestedUser() {
    FavoriteStationEntity stationA =
        favoriteStationsRepository.save(FavoriteStationDataProvider.createMockStation(userA));
    favoriteStationsRepository.save(FavoriteStationDataProvider.createMockStation(userB));

    List<FavoriteStationEntity> result = favoriteStationsRepository.findByUserId(userA.getId());

    assertEquals(1, result.size());
    assertEquals(stationA.getId(), result.getFirst().getId());
  }

  // ─── findByUserIdAndExternalStationId ────────────────────────────────────────

  @Test
  void shouldReturnStationWhenUserIdAndExternalStationIdMatch() {
    FavoriteStationEntity saved =
        favoriteStationsRepository.save(FavoriteStationDataProvider.createMockStation(userA));

    Optional<FavoriteStationEntity> result =
        favoriteStationsRepository.findByUserIdAndExternalStationId(
            userA.getId(), saved.getExternalStationId());

    assertTrue(result.isPresent());
    assertEquals(saved.getId(), result.get().getId());
    assertEquals(saved.getExternalStationId(), result.get().getExternalStationId());
    assertEquals(saved.getStationName(), result.get().getStationName());
  }

  @Test
  void shouldReturnEmptyWhenExternalStationIdDoesNotExist() {
    favoriteStationsRepository.save(FavoriteStationDataProvider.createMockStation(userA));

    Optional<FavoriteStationEntity> result =
        favoriteStationsRepository.findByUserIdAndExternalStationId(
            userA.getId(), "station-not-saved");

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnEmptyWhenStationBelongsToAnotherUser() {
    FavoriteStationEntity saved =
        favoriteStationsRepository.save(FavoriteStationDataProvider.createMockStation(userA));

    Optional<FavoriteStationEntity> result =
        favoriteStationsRepository.findByUserIdAndExternalStationId(
            userB.getId(), saved.getExternalStationId());

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnCorrectStationAmongMultipleForSameUser() {
    FavoriteStationEntity stationA1 =
        favoriteStationsRepository.save(FavoriteStationDataProvider.createMockStation(userA));
    FavoriteStationEntity stationA2 =
        favoriteStationsRepository.save(
            FavoriteStationDataProvider.createAnotherMockStation(userA));

    Optional<FavoriteStationEntity> result =
        favoriteStationsRepository.findByUserIdAndExternalStationId(
            userA.getId(), stationA1.getExternalStationId());

    assertTrue(result.isPresent());
    assertEquals(stationA1.getId(), result.get().getId());
    assertNotEquals(stationA2.getId(), result.get().getId());
  }
}
