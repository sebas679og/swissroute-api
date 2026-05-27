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
import com.group4.swissrouteapi.models.FavoriteRouteEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.repositories.FavoriteRouteRepository;
import com.group4.swissrouteapi.utils.enums.TransportationType;
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
@DisplayName("FavoriteRouteProcessor")
class FavoriteRouteProcessorTest {

  @Mock private FavoriteRouteRepository favoriteRouteRepository;

  @InjectMocks private FavoriteRouteProcessor favoriteRouteProcessor;

  // ─────────────────────────────────────────────────────────────────────────
  // Fixtures
  // ─────────────────────────────────────────────────────────────────────────

  private static final UUID USER_ID = UUID.randomUUID();
  private static final String ROUTE_NAME = "Zurich–Bern Express";
  private static final String ORIGIN = "Zurich HB";
  private static final String DESTINATION = "Bern";
  private static final TransportationType TRANSPORT_TYPE = TransportationType.TRAIN;

  private UserEntity buildUser() {
    UserEntity user = new UserEntity();
    user.setId(USER_ID);
    return user;
  }

  private FavoriteRouteEntity buildSavedEntity(UserEntity user) {
    return FavoriteRouteEntity.builder()
        .id(UUID.randomUUID())
        .user(user)
        .name(ROUTE_NAME)
        .origin(ORIGIN)
        .destination(DESTINATION)
        .transportType(TRANSPORT_TYPE)
        .build();
  }

  // ─────────────────────────────────────────────────────────────────────────
  // saveFavoriteRoute — happy path
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("saveFavoriteRoute — success")
  class SaveFavoriteRouteSuccess {

    @Test
    @DisplayName("returns the persisted entity returned by the repository")
    void returnsPersistedEntity_returnedByRepository() {
      UserEntity user = buildUser();
      FavoriteRouteEntity saved = buildSavedEntity(user);

      when(favoriteRouteRepository.existsByUserIdAndName(USER_ID, ROUTE_NAME)).thenReturn(false);
      when(favoriteRouteRepository.save(any(FavoriteRouteEntity.class))).thenReturn(saved);

      FavoriteRouteEntity result =
          favoriteRouteProcessor.saveFavoriteRoute(
              user, ROUTE_NAME, ORIGIN, DESTINATION, TRANSPORT_TYPE);

      assertThat(result).isSameAs(saved);
    }

    @Test
    @DisplayName("saves entity with the correct name, origin, destination, and transportType")
    void savesEntity_withCorrectFields() {
      UserEntity user = buildUser();
      when(favoriteRouteRepository.existsByUserIdAndName(USER_ID, ROUTE_NAME)).thenReturn(false);
      when(favoriteRouteRepository.save(any(FavoriteRouteEntity.class)))
          .thenAnswer(inv -> inv.getArgument(0));

      favoriteRouteProcessor.saveFavoriteRoute(
          user, ROUTE_NAME, ORIGIN, DESTINATION, TRANSPORT_TYPE);

      ArgumentCaptor<FavoriteRouteEntity> captor =
          ArgumentCaptor.forClass(FavoriteRouteEntity.class);
      verify(favoriteRouteRepository).save(captor.capture());

      FavoriteRouteEntity captured = captor.getValue();
      assertThat(captured.getName()).isEqualTo(ROUTE_NAME);
      assertThat(captured.getOrigin()).isEqualTo(ORIGIN);
      assertThat(captured.getDestination()).isEqualTo(DESTINATION);
      assertThat(captured.getTransportType()).isEqualTo(TRANSPORT_TYPE);
    }

    @Test
    @DisplayName("saves entity associated with the provided user entity")
    void savesEntity_associatedWithProvidedUser() {
      UserEntity user = buildUser();
      when(favoriteRouteRepository.existsByUserIdAndName(USER_ID, ROUTE_NAME)).thenReturn(false);
      when(favoriteRouteRepository.save(any(FavoriteRouteEntity.class)))
          .thenAnswer(inv -> inv.getArgument(0));

      favoriteRouteProcessor.saveFavoriteRoute(
          user, ROUTE_NAME, ORIGIN, DESTINATION, TRANSPORT_TYPE);

      ArgumentCaptor<FavoriteRouteEntity> captor =
          ArgumentCaptor.forClass(FavoriteRouteEntity.class);
      verify(favoriteRouteRepository).save(captor.capture());

      assertThat(captor.getValue().getUser()).isSameAs(user);
    }

    @Test
    @DisplayName("checks uniqueness using the user's id and the provided route name")
    void checksUniqueness_usingUserIdAndRouteName() {
      UserEntity user = buildUser();
      when(favoriteRouteRepository.existsByUserIdAndName(USER_ID, ROUTE_NAME)).thenReturn(false);
      when(favoriteRouteRepository.save(any(FavoriteRouteEntity.class)))
          .thenAnswer(inv -> inv.getArgument(0));

      favoriteRouteProcessor.saveFavoriteRoute(
          user, ROUTE_NAME, ORIGIN, DESTINATION, TRANSPORT_TYPE);

      verify(favoriteRouteRepository).existsByUserIdAndName(USER_ID, ROUTE_NAME);
    }

    @Test
    @DisplayName("calls repository save exactly once when no conflict exists")
    void callsRepositorySave_exactlyOnce_whenNoConflictExists() {
      UserEntity user = buildUser();
      when(favoriteRouteRepository.existsByUserIdAndName(USER_ID, ROUTE_NAME)).thenReturn(false);
      when(favoriteRouteRepository.save(any(FavoriteRouteEntity.class)))
          .thenAnswer(inv -> inv.getArgument(0));

      favoriteRouteProcessor.saveFavoriteRoute(
          user, ROUTE_NAME, ORIGIN, DESTINATION, TRANSPORT_TYPE);

      verify(favoriteRouteRepository, times(1)).save(any(FavoriteRouteEntity.class));
    }

    @Test
    @DisplayName("saves entity with null transportType when no transport type is provided")
    void savesEntity_withNullTransportType() {
      UserEntity user = buildUser();
      when(favoriteRouteRepository.existsByUserIdAndName(USER_ID, ROUTE_NAME)).thenReturn(false);
      when(favoriteRouteRepository.save(any(FavoriteRouteEntity.class)))
          .thenAnswer(inv -> inv.getArgument(0));

      favoriteRouteProcessor.saveFavoriteRoute(user, ROUTE_NAME, ORIGIN, DESTINATION, null);

      ArgumentCaptor<FavoriteRouteEntity> captor =
          ArgumentCaptor.forClass(FavoriteRouteEntity.class);
      verify(favoriteRouteRepository).save(captor.capture());

      assertThat(captor.getValue().getTransportType()).isNull();
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // saveFavoriteRoute — conflict
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("saveFavoriteRoute — conflict")
  class SaveFavoriteRouteConflict {

    @Test
    @DisplayName(
        "throws ConflictException when a route with the same name already exists for the user")
    void throwsConflictException_whenRouteNameAlreadyExistsForUser() {
      UserEntity user = buildUser();
      when(favoriteRouteRepository.existsByUserIdAndName(USER_ID, ROUTE_NAME)).thenReturn(true);

      assertThatThrownBy(
              () ->
                  favoriteRouteProcessor.saveFavoriteRoute(
                      user, ROUTE_NAME, ORIGIN, DESTINATION, TRANSPORT_TYPE))
          .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("exception message is 'Favorite route name already exists'")
    void exceptionMessage_isFavoriteRouteNameAlreadyExists() {
      UserEntity user = buildUser();
      when(favoriteRouteRepository.existsByUserIdAndName(USER_ID, ROUTE_NAME)).thenReturn(true);

      assertThatThrownBy(
              () ->
                  favoriteRouteProcessor.saveFavoriteRoute(
                      user, ROUTE_NAME, ORIGIN, DESTINATION, TRANSPORT_TYPE))
          .isInstanceOf(ConflictException.class)
          .hasMessage("Favorite route name already exists");
    }

    @Test
    @DisplayName("does not call repository save when a conflict is detected")
    void doesNotCallRepositorySave_whenConflictIsDetected() {
      UserEntity user = buildUser();
      when(favoriteRouteRepository.existsByUserIdAndName(USER_ID, ROUTE_NAME)).thenReturn(true);

      assertThatThrownBy(
              () ->
                  favoriteRouteProcessor.saveFavoriteRoute(
                      user, ROUTE_NAME, ORIGIN, DESTINATION, TRANSPORT_TYPE))
          .isInstanceOf(ConflictException.class);

      verify(favoriteRouteRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAllFavoriteRoutes
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllFavoriteRoutes")
    class GetAllFavoriteRoutes {

      @Test
      @DisplayName("returns the list provided by the repository")
      void returnsListFromRepository() {
        UserEntity user = buildUser();
        FavoriteRouteEntity e1 = buildSavedEntity(user);
        FavoriteRouteEntity e2 = buildSavedEntity(user);

        when(favoriteRouteRepository.findByUserId(USER_ID)).thenReturn(List.of(e1, e2));

        List<FavoriteRouteEntity> result = favoriteRouteProcessor.getAllFavoriteRoutes(USER_ID);

        assertThat(result).containsExactly(e1, e2);
      }

      @Test
      @DisplayName("returns an empty list when the user has no favorite routes")
      void returnsEmptyList_whenUserHasNoRoutes() {
        when(favoriteRouteRepository.findByUserId(USER_ID)).thenReturn(List.of());

        List<FavoriteRouteEntity> result = favoriteRouteProcessor.getAllFavoriteRoutes(USER_ID);

        assertThat(result).isEmpty();
      }

      @Test
      @DisplayName("delegates to repository with the exact userId")
      void delegatesToRepository_withExactUserId() {
        when(favoriteRouteRepository.findByUserId(USER_ID)).thenReturn(List.of());

        favoriteRouteProcessor.getAllFavoriteRoutes(USER_ID);

        verify(favoriteRouteRepository).findByUserId(USER_ID);
      }

      @Test
      @DisplayName("calls findByUserId exactly once")
      void callsFindByUserId_exactlyOnce() {
        when(favoriteRouteRepository.findByUserId(USER_ID)).thenReturn(List.of());

        favoriteRouteProcessor.getAllFavoriteRoutes(USER_ID);

        verify(favoriteRouteRepository, times(1)).findByUserId(any());
      }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // updateFavoriteRoute
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateFavoriteRoute")
    class UpdateFavoriteRoute {

      private static final UUID ROUTE_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

      private FavoriteRouteEntity existingRoute() {
        UserEntity user = buildUser();
        return FavoriteRouteEntity.builder()
            .id(ROUTE_ID)
            .user(user)
            .name(ROUTE_NAME)
            .origin(ORIGIN)
            .destination(DESTINATION)
            .transportType(TRANSPORT_TYPE)
            .build();
      }

      // ── success ───────────────────────────────────────────────────────────

      @Test
      @DisplayName("returns the saved entity after a successful update")
      void returnsUpdatedEntity_afterSuccessfulUpdate() {
        FavoriteRouteEntity route = existingRoute();
        when(favoriteRouteRepository.findByUserIdAndId(USER_ID, ROUTE_ID))
            .thenReturn(Optional.of(route));
        when(favoriteRouteRepository.existsByUserIdAndNameAndIdNot(USER_ID, "New Name", ROUTE_ID))
            .thenReturn(false);
        when(favoriteRouteRepository.save(route)).thenReturn(route);

        FavoriteRouteEntity result =
            favoriteRouteProcessor.updateFavoriteRoute(
                USER_ID, ROUTE_ID, "New Name", null, null, null);

        assertThat(result).isSameAs(route);
      }

      @Test
      @DisplayName("updates only the name when only name is provided")
      void updatesOnlyName_whenOnlyNameIsProvided() {
        FavoriteRouteEntity route = existingRoute();
        when(favoriteRouteRepository.findByUserIdAndId(USER_ID, ROUTE_ID))
            .thenReturn(Optional.of(route));
        when(favoriteRouteRepository.existsByUserIdAndNameAndIdNot(USER_ID, "New Name", ROUTE_ID))
            .thenReturn(false);
        when(favoriteRouteRepository.save(route)).thenReturn(route);

        favoriteRouteProcessor.updateFavoriteRoute(USER_ID, ROUTE_ID, "New Name", null, null, null);

        assertThat(route.getName()).isEqualTo("New Name");
        assertThat(route.getOrigin()).isEqualTo(ORIGIN);
        assertThat(route.getDestination()).isEqualTo(DESTINATION);
        assertThat(route.getTransportType()).isEqualTo(TRANSPORT_TYPE);
      }

      @Test
      @DisplayName("updates only the origin when only origin is provided")
      void updatesOnlyOrigin_whenOnlyOriginIsProvided() {
        FavoriteRouteEntity route = existingRoute();
        when(favoriteRouteRepository.findByUserIdAndId(USER_ID, ROUTE_ID))
            .thenReturn(Optional.of(route));
        when(favoriteRouteRepository.save(route)).thenReturn(route);

        favoriteRouteProcessor.updateFavoriteRoute(USER_ID, ROUTE_ID, null, "Lausanne", null, null);

        assertThat(route.getName()).isEqualTo(ROUTE_NAME);
        assertThat(route.getOrigin()).isEqualTo("Lausanne");
        assertThat(route.getDestination()).isEqualTo(DESTINATION);
        assertThat(route.getTransportType()).isEqualTo(TRANSPORT_TYPE);
      }

      @Test
      @DisplayName("updates only the destination when only destination is provided")
      void updatesOnlyDestination_whenOnlyDestinationIsProvided() {
        FavoriteRouteEntity route = existingRoute();
        when(favoriteRouteRepository.findByUserIdAndId(USER_ID, ROUTE_ID))
            .thenReturn(Optional.of(route));
        when(favoriteRouteRepository.save(route)).thenReturn(route);

        favoriteRouteProcessor.updateFavoriteRoute(USER_ID, ROUTE_ID, null, null, "Geneva", null);

        assertThat(route.getName()).isEqualTo(ROUTE_NAME);
        assertThat(route.getOrigin()).isEqualTo(ORIGIN);
        assertThat(route.getDestination()).isEqualTo("Geneva");
        assertThat(route.getTransportType()).isEqualTo(TRANSPORT_TYPE);
      }

      @Test
      @DisplayName("updates only the transportType when only transportType is provided")
      void updatesOnlyTransportType_whenOnlyTransportTypeIsProvided() {
        FavoriteRouteEntity route = existingRoute();
        when(favoriteRouteRepository.findByUserIdAndId(USER_ID, ROUTE_ID))
            .thenReturn(Optional.of(route));
        when(favoriteRouteRepository.save(route)).thenReturn(route);

        favoriteRouteProcessor.updateFavoriteRoute(
            USER_ID, ROUTE_ID, null, null, null, TransportationType.BUS);

        assertThat(route.getName()).isEqualTo(ROUTE_NAME);
        assertThat(route.getOrigin()).isEqualTo(ORIGIN);
        assertThat(route.getDestination()).isEqualTo(DESTINATION);
        assertThat(route.getTransportType()).isEqualTo(TransportationType.BUS);
      }

      @Test
      @DisplayName("updates all fields when all arguments are provided")
      void updatesAllFields_whenAllArgumentsAreProvided() {
        FavoriteRouteEntity route = existingRoute();
        when(favoriteRouteRepository.findByUserIdAndId(USER_ID, ROUTE_ID))
            .thenReturn(Optional.of(route));
        when(favoriteRouteRepository.existsByUserIdAndNameAndIdNot(USER_ID, "New Name", ROUTE_ID))
            .thenReturn(false);
        when(favoriteRouteRepository.save(route)).thenReturn(route);

        favoriteRouteProcessor.updateFavoriteRoute(
            USER_ID, ROUTE_ID, "New Name", "Basel", "Zurich", TransportationType.BUS);

        assertThat(route.getName()).isEqualTo("New Name");
        assertThat(route.getOrigin()).isEqualTo("Basel");
        assertThat(route.getDestination()).isEqualTo("Zurich");
        assertThat(route.getTransportType()).isEqualTo(TransportationType.BUS);
      }

      @Test
      @DisplayName("does not check name uniqueness when name argument is null")
      void doesNotCheckNameUniqueness_whenNameIsNull() {
        FavoriteRouteEntity route = existingRoute();
        when(favoriteRouteRepository.findByUserIdAndId(USER_ID, ROUTE_ID))
            .thenReturn(Optional.of(route));
        when(favoriteRouteRepository.save(route)).thenReturn(route);

        favoriteRouteProcessor.updateFavoriteRoute(USER_ID, ROUTE_ID, null, "Basel", null, null);

        verify(favoriteRouteRepository, never()).existsByUserIdAndNameAndIdNot(any(), any(), any());
      }

      @Test
      @DisplayName("persists the entity via repository save after applying updates")
      void persistsEntity_viaRepositorySave() {
        FavoriteRouteEntity route = existingRoute();
        when(favoriteRouteRepository.findByUserIdAndId(USER_ID, ROUTE_ID))
            .thenReturn(Optional.of(route));
        when(favoriteRouteRepository.save(route)).thenReturn(route);

        favoriteRouteProcessor.updateFavoriteRoute(
            USER_ID, ROUTE_ID, null, null, null, TransportationType.TRAM);

        verify(favoriteRouteRepository, times(1)).save(route);
      }

      // ── not found ─────────────────────────────────────────────────────────

      @Test
      @DisplayName("throws NotFoundException when the route does not exist for the user")
      void throwsNotFoundException_whenRouteDoesNotExist() {
        when(favoriteRouteRepository.findByUserIdAndId(USER_ID, ROUTE_ID))
            .thenReturn(Optional.empty());

        assertThatThrownBy(
                () ->
                    favoriteRouteProcessor.updateFavoriteRoute(
                        USER_ID, ROUTE_ID, "Name", null, null, null))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Route not found");
      }

      @Test
      @DisplayName("does not call save when the route is not found")
      void doesNotCallSave_whenRouteNotFound() {
        when(favoriteRouteRepository.findByUserIdAndId(USER_ID, ROUTE_ID))
            .thenReturn(Optional.empty());

        assertThatThrownBy(
                () ->
                    favoriteRouteProcessor.updateFavoriteRoute(
                        USER_ID, ROUTE_ID, null, null, null, null))
            .isInstanceOf(NotFoundException.class);

        verify(favoriteRouteRepository, never()).save(any());
      }

      // ── name conflict ─────────────────────────────────────────────────────

      @Test
      @DisplayName("throws ConflictException when the new name already exists for another route")
      void throwsConflictException_whenNewNameAlreadyExistsForAnotherRoute() {
        FavoriteRouteEntity route = existingRoute();
        when(favoriteRouteRepository.findByUserIdAndId(USER_ID, ROUTE_ID))
            .thenReturn(Optional.of(route));
        when(favoriteRouteRepository.existsByUserIdAndNameAndIdNot(USER_ID, "Taken Name", ROUTE_ID))
            .thenReturn(true);

        assertThatThrownBy(
                () ->
                    favoriteRouteProcessor.updateFavoriteRoute(
                        USER_ID, ROUTE_ID, "Taken Name", null, null, null))
            .isInstanceOf(ConflictException.class)
            .hasMessage("Favorite route name already exists");
      }

      @Test
      @DisplayName("does not call save when a name conflict is detected")
      void doesNotCallSave_whenNameConflictIsDetected() {
        FavoriteRouteEntity route = existingRoute();
        when(favoriteRouteRepository.findByUserIdAndId(USER_ID, ROUTE_ID))
            .thenReturn(Optional.of(route));
        when(favoriteRouteRepository.existsByUserIdAndNameAndIdNot(USER_ID, "Taken Name", ROUTE_ID))
            .thenReturn(true);

        assertThatThrownBy(
                () ->
                    favoriteRouteProcessor.updateFavoriteRoute(
                        USER_ID, ROUTE_ID, "Taken Name", null, null, null))
            .isInstanceOf(ConflictException.class);

        verify(favoriteRouteRepository, never()).save(any());
      }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteFavoriteRoute
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteFavoriteRoute")
    class DeleteFavoriteRoute {

      private static final UUID ROUTE_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

      // ── success ───────────────────────────────────────────────────────────

      @Test
      @DisplayName("deletes the route when it exists and belongs to the user")
      void deletesRoute_whenItExistsAndBelongsToUser() {
        FavoriteRouteEntity route = buildSavedEntity(buildUser());
        when(favoriteRouteRepository.findByUserIdAndId(USER_ID, ROUTE_ID))
            .thenReturn(Optional.of(route));

        favoriteRouteProcessor.deleteFavoriteRoute(USER_ID, ROUTE_ID);

        verify(favoriteRouteRepository).delete(route);
      }

      @Test
      @DisplayName("calls delete exactly once with the found entity")
      void callsDelete_exactlyOnce_withFoundEntity() {
        FavoriteRouteEntity route = buildSavedEntity(buildUser());
        when(favoriteRouteRepository.findByUserIdAndId(USER_ID, ROUTE_ID))
            .thenReturn(Optional.of(route));

        favoriteRouteProcessor.deleteFavoriteRoute(USER_ID, ROUTE_ID);

        verify(favoriteRouteRepository, times(1)).delete(route);
      }

      // ── not found ─────────────────────────────────────────────────────────

      @Test
      @DisplayName("throws NotFoundException when route does not exist for the user")
      void throwsNotFoundException_whenRouteDoesNotExist() {
        when(favoriteRouteRepository.findByUserIdAndId(USER_ID, ROUTE_ID))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoriteRouteProcessor.deleteFavoriteRoute(USER_ID, ROUTE_ID))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("Route not found");
      }

      @Test
      @DisplayName("does not call delete when route is not found")
      void doesNotCallDelete_whenRouteIsNotFound() {
        when(favoriteRouteRepository.findByUserIdAndId(USER_ID, ROUTE_ID))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoriteRouteProcessor.deleteFavoriteRoute(USER_ID, ROUTE_ID))
            .isInstanceOf(NotFoundException.class);

        verify(favoriteRouteRepository, never()).delete(any());
      }
    }
  }
}
