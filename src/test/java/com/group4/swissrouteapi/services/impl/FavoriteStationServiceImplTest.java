package com.group4.swissrouteapi.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.group4.swissrouteapi.dtos.requests.StationRequest;
import com.group4.swissrouteapi.dtos.responses.favorites.FavStationsResponse;
import com.group4.swissrouteapi.dtos.responses.favorites.StationResponse;
import com.group4.swissrouteapi.exceptions.ConflictException;
import com.group4.swissrouteapi.exceptions.NotFoundException;
import com.group4.swissrouteapi.models.FavoriteStationEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.services.components.UserFinder;
import com.group4.swissrouteapi.services.processors.StationsProcessor;
import com.group4.swissrouteapi.utils.mappers.StationMapper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("FavoriteStationServiceImpl")
class FavoriteStationServiceImplTest {

  @Mock private StationMapper stationMapper;

  @Mock private UserFinder userFinder;

  @Mock private StationsProcessor stationsProcessor;

  @InjectMocks private FavoriteStationServiceImpl favoriteStationService;

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

  private StationRequest buildRequest() {
    return StationRequest.builder()
        .externalStationId(EXTERNAL_ID)
        .stationName(STATION_NAME)
        .build();
  }

  private FavoriteStationEntity buildEntity() {
    return FavoriteStationEntity.builder()
        .id(UUID.randomUUID())
        .externalStationId(EXTERNAL_ID)
        .stationName(STATION_NAME)
        .createdAt(Instant.now().truncatedTo(ChronoUnit.MILLIS))
        .build();
  }

  private StationResponse buildStationResponse(FavoriteStationEntity entity) {
    return StationResponse.builder()
        .externalStationId(entity.getExternalStationId())
        .stationName(entity.getStationName())
        .createdAt(entity.getCreatedAt())
        .build();
  }

  // ─────────────────────────────────────────────────────────────────────────
  // addFavoriteStation
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("addFavoriteStation")
  class AddFavoriteStation {

    @Test
    @DisplayName("returns the mapped StationResponse produced by the mapper")
    void returnsMappedResponse_producedByMapper() {
      UserEntity user = buildUser();
      FavoriteStationEntity entity = buildEntity();
      StationResponse expected = buildStationResponse(entity);

      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(stationsProcessor.addFavoriteStation(user, EXTERNAL_ID, STATION_NAME))
          .thenReturn(entity);
      when(stationMapper.toStationResponse(entity)).thenReturn(expected);

      StationResponse result = favoriteStationService.addFavoriteStation(USER_ID, buildRequest());

      assertThat(result).isSameAs(expected);
    }

    @Test
    @DisplayName("resolves the user via UserFinder before delegating to the processor")
    void resolvesUser_viaUserFinder() {
      UserEntity user = buildUser();
      FavoriteStationEntity entity = buildEntity();

      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(stationsProcessor.addFavoriteStation(any(), any(), any())).thenReturn(entity);
      when(stationMapper.toStationResponse(entity)).thenReturn(buildStationResponse(entity));

      favoriteStationService.addFavoriteStation(USER_ID, buildRequest());

      verify(userFinder).findById(USER_ID);
    }

    @Test
    @DisplayName("delegates to processor with resolved user and all request fields")
    void delegatesToProcessor_withResolvedUserAndAllRequestFields() {
      UserEntity user = buildUser();
      FavoriteStationEntity entity = buildEntity();

      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(stationsProcessor.addFavoriteStation(user, EXTERNAL_ID, STATION_NAME))
          .thenReturn(entity);
      when(stationMapper.toStationResponse(entity)).thenReturn(buildStationResponse(entity));

      favoriteStationService.addFavoriteStation(USER_ID, buildRequest());

      verify(stationsProcessor).addFavoriteStation(user, EXTERNAL_ID, STATION_NAME);
    }

    @Test
    @DisplayName("maps the entity returned by the processor through the mapper")
    void mapsEntityReturnedByProcessor_throughMapper() {
      UserEntity user = buildUser();
      FavoriteStationEntity entity = buildEntity();

      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(stationsProcessor.addFavoriteStation(any(), any(), any())).thenReturn(entity);
      when(stationMapper.toStationResponse(entity)).thenReturn(buildStationResponse(entity));

      favoriteStationService.addFavoriteStation(USER_ID, buildRequest());

      verify(stationMapper).toStationResponse(entity);
    }

    @Test
    @DisplayName("propagates NotFoundException when user is not found")
    void propagatesNotFoundException_whenUserIsNotFound() {
      when(userFinder.findById(USER_ID)).thenThrow(new NotFoundException("User not found"));

      assertThatThrownBy(() -> favoriteStationService.addFavoriteStation(USER_ID, buildRequest()))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("User not found");

      verifyNoInteractions(stationsProcessor, stationMapper);
    }

    @Test
    @DisplayName("propagates ConflictException thrown by the processor")
    void propagatesConflictException_thrownByProcessor() {
      UserEntity user = buildUser();

      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(stationsProcessor.addFavoriteStation(any(), any(), any()))
          .thenThrow(new ConflictException("The station is already registered."));

      assertThatThrownBy(() -> favoriteStationService.addFavoriteStation(USER_ID, buildRequest()))
          .isInstanceOf(ConflictException.class)
          .hasMessage("The station is already registered.");

      verifyNoInteractions(stationMapper);
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // getFavoriteStation
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("getFavoriteStation")
  class GetFavoriteStation {

    @Test
    @DisplayName("returns FavStationsResponse containing all mapped stations")
    void returnsFavStationsResponse_containingAllMappedStations() {
      UserEntity user = buildUser();
      FavoriteStationEntity e1 = buildEntity();
      FavoriteStationEntity e2 = buildEntity();
      StationResponse r1 = buildStationResponse(e1);
      StationResponse r2 = buildStationResponse(e2);

      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(stationsProcessor.getFavoriteStations(USER_ID)).thenReturn(List.of(e1, e2));
      when(stationMapper.toStationResponse(e1)).thenReturn(r1);
      when(stationMapper.toStationResponse(e2)).thenReturn(r2);

      FavStationsResponse result = favoriteStationService.getFavoriteStation(USER_ID);

      assertThat(result.getFavoriteStations()).containsExactly(r1, r2);
    }

    @Test
    @DisplayName("returns FavStationsResponse with empty list when user has no stations")
    void returnsFavStationsResponse_withEmptyList_whenUserHasNoStations() {
      UserEntity user = buildUser();

      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(stationsProcessor.getFavoriteStations(USER_ID)).thenReturn(List.of());

      FavStationsResponse result = favoriteStationService.getFavoriteStation(USER_ID);

      assertThat(result.getFavoriteStations()).isEmpty();
      verifyNoInteractions(stationMapper);
    }

    @Test
    @DisplayName("delegates to processor with the resolved user's id")
    void delegatesToProcessor_withResolvedUserId() {
      UserEntity user = buildUser();

      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(stationsProcessor.getFavoriteStations(USER_ID)).thenReturn(List.of());

      favoriteStationService.getFavoriteStation(USER_ID);

      verify(stationsProcessor).getFavoriteStations(USER_ID);
    }

    @Test
    @DisplayName("maps every entity in the list through the mapper")
    void mapsEveryEntity_throughMapper() {
      UserEntity user = buildUser();
      FavoriteStationEntity e1 = buildEntity();
      FavoriteStationEntity e2 = buildEntity();

      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(stationsProcessor.getFavoriteStations(USER_ID)).thenReturn(List.of(e1, e2));
      when(stationMapper.toStationResponse(e1)).thenReturn(buildStationResponse(e1));
      when(stationMapper.toStationResponse(e2)).thenReturn(buildStationResponse(e2));

      favoriteStationService.getFavoriteStation(USER_ID);

      verify(stationMapper).toStationResponse(e1);
      verify(stationMapper).toStationResponse(e2);
    }

    @Test
    @DisplayName("propagates NotFoundException when user is not found")
    void propagatesNotFoundException_whenUserIsNotFound() {
      when(userFinder.findById(USER_ID)).thenThrow(new NotFoundException("User not found"));

      assertThatThrownBy(() -> favoriteStationService.getFavoriteStation(USER_ID))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("User not found");

      verifyNoInteractions(stationsProcessor, stationMapper);
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // removeFavoriteStation
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("removeFavoriteStation")
  class RemoveFavoriteStation {

    @Test
    @DisplayName("delegates deletion to processor with resolved user's id and externalStationId")
    void delegatesDeletionToProcessor_withResolvedUserIdAndExternalStationId() {
      UserEntity user = buildUser();
      when(userFinder.findById(USER_ID)).thenReturn(user);

      favoriteStationService.removeFavoriteStation(USER_ID, EXTERNAL_ID);

      verify(stationsProcessor).deleteFavoriteStation(USER_ID, EXTERNAL_ID);
    }

    @Test
    @DisplayName("does not interact with mapper during removal")
    void doesNotInteractWithMapper_duringRemoval() {
      UserEntity user = buildUser();
      when(userFinder.findById(USER_ID)).thenReturn(user);

      favoriteStationService.removeFavoriteStation(USER_ID, EXTERNAL_ID);

      verifyNoInteractions(stationMapper);
    }

    @Test
    @DisplayName("propagates NotFoundException when user is not found")
    void propagatesNotFoundException_whenUserIsNotFound() {
      when(userFinder.findById(USER_ID)).thenThrow(new NotFoundException("User not found"));

      assertThatThrownBy(() -> favoriteStationService.removeFavoriteStation(USER_ID, EXTERNAL_ID))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("User not found");

      verifyNoInteractions(stationsProcessor, stationMapper);
    }

    @Test
    @DisplayName("propagates NotFoundException thrown by processor when station is not found")
    void propagatesNotFoundException_thrownByProcessor_whenStationNotFound() {
      UserEntity user = buildUser();
      when(userFinder.findById(USER_ID)).thenReturn(user);
      doThrow(new NotFoundException("Station not found"))
          .when(stationsProcessor)
          .deleteFavoriteStation(USER_ID, EXTERNAL_ID);

      assertThatThrownBy(() -> favoriteStationService.removeFavoriteStation(USER_ID, EXTERNAL_ID))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("Station not found");
    }
  }
}
