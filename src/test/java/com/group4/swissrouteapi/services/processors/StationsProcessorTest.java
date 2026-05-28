package com.group4.swissrouteapi.services.processors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.group4.swissrouteapi.exceptions.ConflictException;
import com.group4.swissrouteapi.exceptions.NotFoundException;
import com.group4.swissrouteapi.models.FavoriteStationEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.repositories.FavoriteStationsRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("StationsProcessor")
class StationsProcessorTest {

  @Mock private FavoriteStationsRepository favoriteStationsRepository;

  @InjectMocks private StationsProcessor stationsProcessor;

  // ─────────────────────────────────────────────────────────────────────────
  // Fixtures
  // ─────────────────────────────────────────────────────────────────────────

  private static final UUID USER_ID = UUID.randomUUID();
  private static final String EXTERNAL_ID = "8503000";
  private static final String STATION_NAME = "Zurich HB";

  private UserEntity buildUser() {
    UserEntity user = UserEntity.builder().build();
    user.setId(USER_ID);
    return user;
  }

  private FavoriteStationEntity buildStation(UserEntity user) {
    return FavoriteStationEntity.builder()
        .id(UUID.randomUUID())
        .user(user)
        .externalStationId(EXTERNAL_ID)
        .stationName(STATION_NAME)
        .createdAt(Instant.now().truncatedTo(ChronoUnit.MILLIS))
        .build();
  }

  // ─────────────────────────────────────────────────────────────────────────
  // addFavoriteStation
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("addFavoriteStation")
  class AddFavoriteStation {

    // ── success ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("returns the persisted entity returned by the repository")
    void returnsPersistedEntity_returnedByRepository() {
      UserEntity user = buildUser();
      FavoriteStationEntity saved = buildStation(user);

      when(favoriteStationsRepository.existsByUserIdAndExternalStationId(USER_ID, EXTERNAL_ID))
          .thenReturn(false);
      when(favoriteStationsRepository.save(any(FavoriteStationEntity.class))).thenReturn(saved);

      FavoriteStationEntity result =
          stationsProcessor.addFavoriteStation(user, EXTERNAL_ID, STATION_NAME);

      assertThat(result).isSameAs(saved);
    }

    @Test
    @DisplayName("saves entity with correct externalStationId, stationName, and user")
    void savesEntity_withCorrectFields() {
      UserEntity user = buildUser();

      when(favoriteStationsRepository.existsByUserIdAndExternalStationId(USER_ID, EXTERNAL_ID))
          .thenReturn(false);
      when(favoriteStationsRepository.save(any(FavoriteStationEntity.class)))
          .thenAnswer(inv -> inv.getArgument(0));

      stationsProcessor.addFavoriteStation(user, EXTERNAL_ID, STATION_NAME);

      ArgumentCaptor<FavoriteStationEntity> captor =
          ArgumentCaptor.forClass(FavoriteStationEntity.class);
      verify(favoriteStationsRepository).save(captor.capture());

      FavoriteStationEntity captured = captor.getValue();
      assertThat(captured.getExternalStationId()).isEqualTo(EXTERNAL_ID);
      assertThat(captured.getStationName()).isEqualTo(STATION_NAME);
      assertThat(captured.getUser()).isSameAs(user);
    }

    @Test
    @DisplayName("checks uniqueness using user's id and externalStationId")
    void checksUniqueness_usingUserIdAndExternalStationId() {
      UserEntity user = buildUser();

      when(favoriteStationsRepository.existsByUserIdAndExternalStationId(USER_ID, EXTERNAL_ID))
          .thenReturn(false);
      when(favoriteStationsRepository.save(any(FavoriteStationEntity.class)))
          .thenAnswer(inv -> inv.getArgument(0));

      stationsProcessor.addFavoriteStation(user, EXTERNAL_ID, STATION_NAME);

      verify(favoriteStationsRepository).existsByUserIdAndExternalStationId(USER_ID, EXTERNAL_ID);
    }

    @Test
    @DisplayName("calls repository save exactly once when no conflict exists")
    void callsRepositorySave_exactlyOnce() {
      UserEntity user = buildUser();

      when(favoriteStationsRepository.existsByUserIdAndExternalStationId(USER_ID, EXTERNAL_ID))
          .thenReturn(false);
      when(favoriteStationsRepository.save(any(FavoriteStationEntity.class)))
          .thenAnswer(inv -> inv.getArgument(0));

      stationsProcessor.addFavoriteStation(user, EXTERNAL_ID, STATION_NAME);

      verify(favoriteStationsRepository, times(1)).save(any());
    }

    // ── conflict ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("throws ConflictException when station is already registered for the user")
    void throwsConflictException_whenStationAlreadyRegistered() {
      UserEntity user = buildUser();

      when(favoriteStationsRepository.existsByUserIdAndExternalStationId(USER_ID, EXTERNAL_ID))
          .thenReturn(true);

      assertThatThrownBy(
              () -> stationsProcessor.addFavoriteStation(user, EXTERNAL_ID, STATION_NAME))
          .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("exception message is 'The station is already registered.'")
    void exceptionMessage_isStationAlreadyRegistered() {
      UserEntity user = buildUser();

      when(favoriteStationsRepository.existsByUserIdAndExternalStationId(USER_ID, EXTERNAL_ID))
          .thenReturn(true);

      assertThatThrownBy(
              () -> stationsProcessor.addFavoriteStation(user, EXTERNAL_ID, STATION_NAME))
          .isInstanceOf(ConflictException.class)
          .hasMessage("The station is already registered.");
    }

    @Test
    @DisplayName("does not call save when a conflict is detected")
    void doesNotCallSave_whenConflictIsDetected() {
      UserEntity user = buildUser();

      when(favoriteStationsRepository.existsByUserIdAndExternalStationId(USER_ID, EXTERNAL_ID))
          .thenReturn(true);

      assertThatThrownBy(
              () -> stationsProcessor.addFavoriteStation(user, EXTERNAL_ID, STATION_NAME))
          .isInstanceOf(ConflictException.class);

      verify(favoriteStationsRepository, never()).save(any());
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // getFavoriteStations
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("getFavoriteStations")
  class GetFavoriteStations {

    @Test
    @DisplayName("returns the list provided by the repository")
    void returnsListFromRepository() {
      UserEntity user = buildUser();
      FavoriteStationEntity s1 = buildStation(user);
      FavoriteStationEntity s2 = buildStation(user);

      when(favoriteStationsRepository.findByUserId(USER_ID)).thenReturn(List.of(s1, s2));

      List<FavoriteStationEntity> result = stationsProcessor.getFavoriteStations(USER_ID);

      assertThat(result).containsExactly(s1, s2);
    }

    @Test
    @DisplayName("returns an empty list when the user has no favorite stations")
    void returnsEmptyList_whenUserHasNoStations() {
      when(favoriteStationsRepository.findByUserId(USER_ID)).thenReturn(List.of());

      List<FavoriteStationEntity> result = stationsProcessor.getFavoriteStations(USER_ID);

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("delegates to repository with the exact userId")
    void delegatesToRepository_withExactUserId() {
      when(favoriteStationsRepository.findByUserId(USER_ID)).thenReturn(List.of());

      stationsProcessor.getFavoriteStations(USER_ID);

      verify(favoriteStationsRepository).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("calls findByUserId exactly once")
    void callsFindByUserId_exactlyOnce() {
      when(favoriteStationsRepository.findByUserId(USER_ID)).thenReturn(List.of());

      stationsProcessor.getFavoriteStations(USER_ID);

      verify(favoriteStationsRepository, times(1)).findByUserId(any());
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // deleteFavoriteStation
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("deleteFavoriteStation")
  class DeleteFavoriteStation {

    // ── success ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("deletes the station when it exists and belongs to the user")
    void deletesStation_whenItExistsAndBelongsToUser() {
      UserEntity user = buildUser();
      FavoriteStationEntity station = buildStation(user);

      when(favoriteStationsRepository.findByUserIdAndExternalStationId(USER_ID, EXTERNAL_ID))
          .thenReturn(Optional.of(station));

      stationsProcessor.deleteFavoriteStation(USER_ID, EXTERNAL_ID);

      verify(favoriteStationsRepository).delete(station);
    }

    @Test
    @DisplayName("calls delete exactly once with the found station entity")
    void callsDelete_exactlyOnce_withFoundEntity() {
      UserEntity user = buildUser();
      FavoriteStationEntity station = buildStation(user);

      when(favoriteStationsRepository.findByUserIdAndExternalStationId(USER_ID, EXTERNAL_ID))
          .thenReturn(Optional.of(station));

      stationsProcessor.deleteFavoriteStation(USER_ID, EXTERNAL_ID);

      verify(favoriteStationsRepository, times(1)).delete(station);
    }

    @Test
    @DisplayName("looks up the station using userId and externalStationId before deleting")
    void looksUpStation_usingUserIdAndExternalStationId() {
      UserEntity user = buildUser();
      FavoriteStationEntity station = buildStation(user);

      when(favoriteStationsRepository.findByUserIdAndExternalStationId(USER_ID, EXTERNAL_ID))
          .thenReturn(Optional.of(station));

      stationsProcessor.deleteFavoriteStation(USER_ID, EXTERNAL_ID);

      verify(favoriteStationsRepository).findByUserIdAndExternalStationId(USER_ID, EXTERNAL_ID);
    }

    // ── not found ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("throws NotFoundException when station does not exist for the user")
    void throwsNotFoundException_whenStationDoesNotExist() {
      when(favoriteStationsRepository.findByUserIdAndExternalStationId(USER_ID, EXTERNAL_ID))
          .thenReturn(Optional.empty());

      assertThatThrownBy(() -> stationsProcessor.deleteFavoriteStation(USER_ID, EXTERNAL_ID))
          .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("exception message is 'Station not found'")
    void exceptionMessage_isStationNotFound() {
      when(favoriteStationsRepository.findByUserIdAndExternalStationId(USER_ID, EXTERNAL_ID))
          .thenReturn(Optional.empty());

      assertThatThrownBy(() -> stationsProcessor.deleteFavoriteStation(USER_ID, EXTERNAL_ID))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("Station not found");
    }

    @Test
    @DisplayName("does not call delete when station is not found")
    void doesNotCallDelete_whenStationIsNotFound() {
      when(favoriteStationsRepository.findByUserIdAndExternalStationId(USER_ID, EXTERNAL_ID))
          .thenReturn(Optional.empty());

      assertThatThrownBy(() -> stationsProcessor.deleteFavoriteStation(USER_ID, EXTERNAL_ID))
          .isInstanceOf(NotFoundException.class);

      verify(favoriteStationsRepository, never()).delete(any());
    }
  }
}
