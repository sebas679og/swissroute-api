package com.group4.swissrouteapi.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.group4.swissrouteapi.dtos.requests.FavoriteRouteRequest;
import com.group4.swissrouteapi.dtos.responses.favorites.RegisterRouteResponse;
import com.group4.swissrouteapi.exceptions.ConflictException;
import com.group4.swissrouteapi.exceptions.NotFoundException;
import com.group4.swissrouteapi.models.FavoriteRouteEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.services.components.UserFinder;
import com.group4.swissrouteapi.services.processors.FavoriteRouteProcessor;
import com.group4.swissrouteapi.utils.enums.TransportationType;
import com.group4.swissrouteapi.utils.mappers.FavoriteRouteMapper;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("FavoriteRouteServiceImpl")
class FavoriteRouteServiceImplTest {

  @Mock private FavoriteRouteProcessor favoriteRouteProcessor;

  @Mock private UserFinder userFinder;

  @Mock private FavoriteRouteMapper favoriteRouteMapper;

  @InjectMocks private FavoriteRouteServiceImpl favoriteRouteService;

  // ─────────────────────────────────────────────────────────────────────────
  // Fixtures
  // ─────────────────────────────────────────────────────────────────────────

  private static final UUID USER_ID = UUID.randomUUID();

  private UserEntity buildUser() {
    UserEntity user = new UserEntity();
    user.setId(USER_ID);
    return user;
  }

  private FavoriteRouteRequest buildRequest() {
    return FavoriteRouteRequest.builder()
        .name("Zurich–Bern Express")
        .origin("Zurich HB")
        .destination("Bern")
        .transportationType(TransportationType.TRAIN)
        .build();
  }

  private FavoriteRouteEntity buildEntity() {
    return FavoriteRouteEntity.builder()
        .id(UUID.randomUUID())
        .name("Zurich–Bern Express")
        .origin("Zurich HB")
        .destination("Bern")
        .transportType(TransportationType.TRAIN)
        .createdAt(Instant.now())
        .build();
  }

  private RegisterRouteResponse buildResponse(FavoriteRouteEntity entity) {
    return RegisterRouteResponse.builder()
        .id(entity.getId())
        .name(entity.getName())
        .origin(entity.getOrigin())
        .destination(entity.getDestination())
        .transportType(entity.getTransportType())
        .createdAt(entity.getCreatedAt())
        .build();
  }

  // ─────────────────────────────────────────────────────────────────────────
  // addFavoriteRoute — happy path
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("addFavoriteRoute — success")
  class AddFavoriteRouteSuccess {

    @Test
    @DisplayName("returns the mapped FavoriteRouteResponse produced by the mapper")
    void returnsMappedResponse_producedByMapper() {
      UserEntity user = buildUser();
      FavoriteRouteRequest request = buildRequest();
      FavoriteRouteEntity entity = buildEntity();
      RegisterRouteResponse expectedResponse = buildResponse(entity);

      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(favoriteRouteProcessor.saveFavoriteRoute(
              user,
              request.getName(),
              request.getOrigin(),
              request.getDestination(),
              request.getTransportationType()))
          .thenReturn(entity);
      when(favoriteRouteMapper.toFavoriteRouteResponse(entity)).thenReturn(expectedResponse);

      RegisterRouteResponse result = favoriteRouteService.addFavoriteRoute(USER_ID, request);

      assertThat(result).isSameAs(expectedResponse);
    }

    @Test
    @DisplayName("resolves the user via UserFinder before delegating to the processor")
    void resolvesUser_viaUserFinder_beforeDelegatingToProcessor() {
      UserEntity user = buildUser();
      final FavoriteRouteRequest request = buildRequest();
      FavoriteRouteEntity entity = buildEntity();

      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(favoriteRouteProcessor.saveFavoriteRoute(any(), any(), any(), any(), any()))
          .thenReturn(entity);
      when(favoriteRouteMapper.toFavoriteRouteResponse(entity)).thenReturn(buildResponse(entity));

      favoriteRouteService.addFavoriteRoute(USER_ID, request);

      verify(userFinder).findById(USER_ID);
    }

    @Test
    @DisplayName("delegates to processor with the resolved user and all request fields")
    void delegatesToProcessor_withResolvedUserAndAllRequestFields() {
      UserEntity user = buildUser();
      FavoriteRouteRequest request = buildRequest();
      FavoriteRouteEntity entity = buildEntity();

      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(favoriteRouteProcessor.saveFavoriteRoute(
              user,
              request.getName(),
              request.getOrigin(),
              request.getDestination(),
              request.getTransportationType()))
          .thenReturn(entity);
      when(favoriteRouteMapper.toFavoriteRouteResponse(entity)).thenReturn(buildResponse(entity));

      favoriteRouteService.addFavoriteRoute(USER_ID, request);

      verify(favoriteRouteProcessor)
          .saveFavoriteRoute(
              user,
              request.getName(),
              request.getOrigin(),
              request.getDestination(),
              request.getTransportationType());
    }

    @Test
    @DisplayName("maps the entity returned by the processor through the mapper")
    void mapsEntityReturnedByProcessor_throughMapper() {
      UserEntity user = buildUser();
      final FavoriteRouteRequest request = buildRequest();
      FavoriteRouteEntity entity = buildEntity();

      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(favoriteRouteProcessor.saveFavoriteRoute(any(), any(), any(), any(), any()))
          .thenReturn(entity);
      when(favoriteRouteMapper.toFavoriteRouteResponse(entity)).thenReturn(buildResponse(entity));

      favoriteRouteService.addFavoriteRoute(USER_ID, request);

      verify(favoriteRouteMapper).toFavoriteRouteResponse(entity);
    }

    @Test
    @DisplayName("passes request fields to processor without modification")
    void passesRequestFields_toProcessor_withoutModification() {
      UserEntity user = buildUser();
      final FavoriteRouteRequest request =
          FavoriteRouteRequest.builder()
              .name("Night Ship")
              .origin("Geneva")
              .destination("Lucerne")
              .transportationType(TransportationType.SHIP)
              .build();
      FavoriteRouteEntity entity = buildEntity();

      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(favoriteRouteProcessor.saveFavoriteRoute(
              user, "Night Ship", "Geneva", "Lucerne", TransportationType.SHIP))
          .thenReturn(entity);
      when(favoriteRouteMapper.toFavoriteRouteResponse(entity)).thenReturn(buildResponse(entity));

      favoriteRouteService.addFavoriteRoute(USER_ID, request);

      verify(favoriteRouteProcessor)
          .saveFavoriteRoute(user, "Night Ship", "Geneva", "Lucerne", TransportationType.SHIP);
    }

    @Test
    @DisplayName("passes null transportationType to processor when request has no transport type")
    void passesNullTransportationType_toProcessor_whenRequestHasNoTransportType() {
      UserEntity user = buildUser();
      final FavoriteRouteRequest request =
          FavoriteRouteRequest.builder()
              .name("Any Route")
              .origin("Basel")
              .destination("Zurich")
              .transportationType(null)
              .build();
      FavoriteRouteEntity entity = buildEntity();

      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(favoriteRouteProcessor.saveFavoriteRoute(user, "Any Route", "Basel", "Zurich", null))
          .thenReturn(entity);
      when(favoriteRouteMapper.toFavoriteRouteResponse(entity)).thenReturn(buildResponse(entity));

      favoriteRouteService.addFavoriteRoute(USER_ID, request);

      verify(favoriteRouteProcessor).saveFavoriteRoute(user, "Any Route", "Basel", "Zurich", null);
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // addFavoriteRoute — user not found
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("addFavoriteRoute — user not found")
  class AddFavoriteRouteUserNotFound {

    @Test
    @DisplayName("propagates NotFoundException thrown by UserFinder")
    void propagatesNotFoundException_thrownByUserFinder() {
      when(userFinder.findById(USER_ID)).thenThrow(new NotFoundException("User not found"));

      assertThatThrownBy(() -> favoriteRouteService.addFavoriteRoute(USER_ID, buildRequest()))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("User not found");
    }

    @Test
    @DisplayName("does not interact with processor or mapper when user is not found")
    void doesNotInteractWithProcessorOrMapper_whenUserIsNotFound() {
      when(userFinder.findById(USER_ID)).thenThrow(new NotFoundException("User not found"));

      assertThatThrownBy(() -> favoriteRouteService.addFavoriteRoute(USER_ID, buildRequest()))
          .isInstanceOf(NotFoundException.class);

      verifyNoInteractions(favoriteRouteProcessor, favoriteRouteMapper);
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // addFavoriteRoute — route name conflict
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("addFavoriteRoute — route name conflict")
  class AddFavoriteRouteConflict {

    @Test
    @DisplayName("propagates ConflictException thrown by the processor")
    void propagatesConflictException_thrownByProcessor() {
      UserEntity user = buildUser();
      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(favoriteRouteProcessor.saveFavoriteRoute(any(), any(), any(), any(), any()))
          .thenThrow(new ConflictException("Favorite route name already exists"));

      assertThatThrownBy(() -> favoriteRouteService.addFavoriteRoute(USER_ID, buildRequest()))
          .isInstanceOf(ConflictException.class)
          .hasMessage("Favorite route name already exists");
    }

    @Test
    @DisplayName("does not interact with mapper when processor throws ConflictException")
    void doesNotInteractWithMapper_whenProcessorThrowsConflictException() {
      UserEntity user = buildUser();
      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(favoriteRouteProcessor.saveFavoriteRoute(any(), any(), any(), any(), any()))
          .thenThrow(new ConflictException("Favorite route name already exists"));

      assertThatThrownBy(() -> favoriteRouteService.addFavoriteRoute(USER_ID, buildRequest()))
          .isInstanceOf(ConflictException.class);

      verifyNoInteractions(favoriteRouteMapper);
    }
  }
}
