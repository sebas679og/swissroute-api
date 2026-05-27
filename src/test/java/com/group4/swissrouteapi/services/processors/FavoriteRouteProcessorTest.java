package com.group4.swissrouteapi.services.processors;

import com.group4.swissrouteapi.exceptions.ConflictException;
import com.group4.swissrouteapi.models.FavoriteRouteEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.repositories.FavoriteRouteRepository;
import com.group4.swissrouteapi.utils.enums.TransportationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FavoriteRouteProcessor")
class FavoriteRouteProcessorTest {

    @Mock
    private FavoriteRouteRepository favoriteRouteRepository;

    @InjectMocks
    private FavoriteRouteProcessor favoriteRouteProcessor;

    // ─────────────────────────────────────────────────────────────────────────
    // Fixtures
    // ─────────────────────────────────────────────────────────────────────────

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String ROUTE_NAME    = "Zurich–Bern Express";
    private static final String ORIGIN        = "Zurich HB";
    private static final String DESTINATION   = "Bern";
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

            FavoriteRouteEntity result = favoriteRouteProcessor.saveFavoriteRoute(
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

            favoriteRouteProcessor.saveFavoriteRoute(
                    user, ROUTE_NAME, ORIGIN, DESTINATION, null);

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
        @DisplayName("throws ConflictException when a route with the same name already exists for the user")
        void throwsConflictException_whenRouteNameAlreadyExistsForUser() {
            UserEntity user = buildUser();
            when(favoriteRouteRepository.existsByUserIdAndName(USER_ID, ROUTE_NAME)).thenReturn(true);

            assertThatThrownBy(() -> favoriteRouteProcessor.saveFavoriteRoute(
                    user, ROUTE_NAME, ORIGIN, DESTINATION, TRANSPORT_TYPE))
                    .isInstanceOf(ConflictException.class);
        }

        @Test
        @DisplayName("exception message is 'Favorite route name already exists'")
        void exceptionMessage_isFavoriteRouteNameAlreadyExists() {
            UserEntity user = buildUser();
            when(favoriteRouteRepository.existsByUserIdAndName(USER_ID, ROUTE_NAME)).thenReturn(true);

            assertThatThrownBy(() -> favoriteRouteProcessor.saveFavoriteRoute(
                    user, ROUTE_NAME, ORIGIN, DESTINATION, TRANSPORT_TYPE))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("Favorite route name already exists");
        }

        @Test
        @DisplayName("does not call repository save when a conflict is detected")
        void doesNotCallRepositorySave_whenConflictIsDetected() {
            UserEntity user = buildUser();
            when(favoriteRouteRepository.existsByUserIdAndName(USER_ID, ROUTE_NAME)).thenReturn(true);

            assertThatThrownBy(() -> favoriteRouteProcessor.saveFavoriteRoute(
                    user, ROUTE_NAME, ORIGIN, DESTINATION, TRANSPORT_TYPE))
                    .isInstanceOf(ConflictException.class);

            verify(favoriteRouteRepository, never()).save(any());
        }

        @Test
        @DisplayName("checks uniqueness before attempting to persist the entity")
        void checksUniqueness_beforePersisting() {
            UserEntity user = buildUser();
            when(favoriteRouteRepository.existsByUserIdAndName(USER_ID, ROUTE_NAME)).thenReturn(true);

            assertThatThrownBy(() -> favoriteRouteProcessor.saveFavoriteRoute(
                    user, ROUTE_NAME, ORIGIN, DESTINATION, TRANSPORT_TYPE))
                    .isInstanceOf(ConflictException.class);

            verify(favoriteRouteRepository).existsByUserIdAndName(USER_ID, ROUTE_NAME);
            verify(favoriteRouteRepository, never()).save(any());
        }
    }
}
