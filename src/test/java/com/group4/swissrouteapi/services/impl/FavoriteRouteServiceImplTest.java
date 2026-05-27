package com.group4.swissrouteapi.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.group4.swissrouteapi.dtos.requests.FavoriteRouteRequest;
import com.group4.swissrouteapi.dtos.requests.RouteUpdateRequest;
import com.group4.swissrouteapi.dtos.responses.favorites.RouteResponse;
import com.group4.swissrouteapi.dtos.responses.favorites.RoutesResponse;
import com.group4.swissrouteapi.exceptions.BadRequestException;
import com.group4.swissrouteapi.exceptions.ConflictException;
import com.group4.swissrouteapi.exceptions.NotFoundException;
import com.group4.swissrouteapi.models.FavoriteRouteEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.services.components.UserFinder;
import com.group4.swissrouteapi.services.processors.FavoriteRouteProcessor;
import com.group4.swissrouteapi.utils.enums.TransportType;
import com.group4.swissrouteapi.utils.mappers.FavoriteRouteMapper;
import java.time.Instant;
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
  private static final UUID ROUTE_ID = UUID.randomUUID();

  private UserEntity buildUser() {
    UserEntity user = new UserEntity();
    user.setId(USER_ID);
    return user;
  }

  private FavoriteRouteRequest buildAddRequest() {
    return FavoriteRouteRequest.builder()
        .name("Zurich–Bern Express")
        .origin("Zurich HB")
        .destination("Bern")
        .transportType(TransportType.TRAIN)
        .build();
  }

  private FavoriteRouteEntity buildEntity() {
    return FavoriteRouteEntity.builder()
        .id(ROUTE_ID)
        .name("Zurich–Bern Express")
        .origin("Zurich HB")
        .destination("Bern")
        .transportType(TransportType.TRAIN)
        .createdAt(Instant.now())
        .build();
  }

  private RouteResponse buildRouteResponse(FavoriteRouteEntity entity) {
    return RouteResponse.builder()
        .id(entity.getId())
        .name(entity.getName())
        .origin(entity.getOrigin())
        .destination(entity.getDestination())
        .transportType(entity.getTransportType())
        .createdAt(entity.getCreatedAt())
        .build();
  }

  // ─────────────────────────────────────────────────────────────────────────
  // addFavoriteRoute
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("addFavoriteRoute")
  class AddFavoriteRoute {

    @Test
    @DisplayName("returns the mapped RouteResponse produced by the mapper")
    void returnsMappedResponse_producedByMapper() {
      UserEntity user = buildUser();
      FavoriteRouteRequest request = buildAddRequest();
      FavoriteRouteEntity entity = buildEntity();
      RouteResponse expected = buildRouteResponse(entity);

      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(favoriteRouteProcessor.saveFavoriteRoute(
              user,
              request.getName(),
              request.getOrigin(),
              request.getDestination(),
              request.getTransportType()))
          .thenReturn(entity);
      when(favoriteRouteMapper.toFavoriteRouteResponse(entity)).thenReturn(expected);

      RouteResponse result = favoriteRouteService.addFavoriteRoute(USER_ID, request);

      assertThat(result).isSameAs(expected);
    }

    @Test
    @DisplayName("resolves the user via UserFinder before delegating to the processor")
    void resolvesUser_viaUserFinder() {
      UserEntity user = buildUser();
      final FavoriteRouteRequest request = buildAddRequest();
      FavoriteRouteEntity entity = buildEntity();

      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(favoriteRouteProcessor.saveFavoriteRoute(any(), any(), any(), any(), any()))
          .thenReturn(entity);
      when(favoriteRouteMapper.toFavoriteRouteResponse(entity))
          .thenReturn(buildRouteResponse(entity));

      favoriteRouteService.addFavoriteRoute(USER_ID, request);

      verify(userFinder).findById(USER_ID);
    }

    @Test
    @DisplayName("delegates to processor with resolved user and all request fields")
    void delegatesToProcessor_withResolvedUserAndAllRequestFields() {
      UserEntity user = buildUser();
      FavoriteRouteRequest request = buildAddRequest();
      FavoriteRouteEntity entity = buildEntity();

      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(favoriteRouteProcessor.saveFavoriteRoute(
              user,
              request.getName(),
              request.getOrigin(),
              request.getDestination(),
              request.getTransportType()))
          .thenReturn(entity);
      when(favoriteRouteMapper.toFavoriteRouteResponse(entity))
          .thenReturn(buildRouteResponse(entity));

      favoriteRouteService.addFavoriteRoute(USER_ID, request);

      verify(favoriteRouteProcessor)
          .saveFavoriteRoute(
              user,
              request.getName(),
              request.getOrigin(),
              request.getDestination(),
              request.getTransportType());
    }

    @Test
    @DisplayName("maps the entity returned by the processor through the mapper")
    void mapsEntityReturnedByProcessor_throughMapper() {
      UserEntity user = buildUser();
      final FavoriteRouteRequest request = buildAddRequest();
      FavoriteRouteEntity entity = buildEntity();

      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(favoriteRouteProcessor.saveFavoriteRoute(any(), any(), any(), any(), any()))
          .thenReturn(entity);
      when(favoriteRouteMapper.toFavoriteRouteResponse(entity))
          .thenReturn(buildRouteResponse(entity));

      favoriteRouteService.addFavoriteRoute(USER_ID, request);

      verify(favoriteRouteMapper).toFavoriteRouteResponse(entity);
    }

    @Test
    @DisplayName("propagates NotFoundException when user is not found")
    void propagatesNotFoundException_whenUserIsNotFound() {
      when(userFinder.findById(USER_ID)).thenThrow(new NotFoundException("User not found"));

      assertThatThrownBy(() -> favoriteRouteService.addFavoriteRoute(USER_ID, buildAddRequest()))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("User not found");

      verifyNoInteractions(favoriteRouteProcessor, favoriteRouteMapper);
    }

    @Test
    @DisplayName("propagates ConflictException thrown by the processor")
    void propagatesConflictException_thrownByProcessor() {
      UserEntity user = buildUser();
      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(favoriteRouteProcessor.saveFavoriteRoute(any(), any(), any(), any(), any()))
          .thenThrow(new ConflictException("Favorite route name already exists"));

      assertThatThrownBy(() -> favoriteRouteService.addFavoriteRoute(USER_ID, buildAddRequest()))
          .isInstanceOf(ConflictException.class)
          .hasMessage("Favorite route name already exists");

      verifyNoInteractions(favoriteRouteMapper);
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // getFavoriteRoutes
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("getFavoriteRoutes")
  class GetFavoriteRoutes {

    @Test
    @DisplayName("returns RoutesResponse containing all mapped routes")
    void returnsRoutesResponse_containingAllMappedRoutes() {
      UserEntity user = buildUser();
      FavoriteRouteEntity e1 = buildEntity();
      FavoriteRouteEntity e2 = buildEntity();
      RouteResponse r1 = buildRouteResponse(e1);
      RouteResponse r2 = buildRouteResponse(e2);

      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(favoriteRouteProcessor.getAllFavoriteRoutes(USER_ID)).thenReturn(List.of(e1, e2));
      when(favoriteRouteMapper.toFavoriteRouteResponse(e1)).thenReturn(r1);
      when(favoriteRouteMapper.toFavoriteRouteResponse(e2)).thenReturn(r2);

      RoutesResponse result = favoriteRouteService.getFavoriteRoutes(USER_ID);

      assertThat(result.getFavoriteRoutes()).containsExactly(r1, r2);
    }

    @Test
    @DisplayName("returns RoutesResponse with empty list when user has no routes")
    void returnsEmptyRoutesResponse_whenUserHasNoRoutes() {
      UserEntity user = buildUser();
      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(favoriteRouteProcessor.getAllFavoriteRoutes(USER_ID)).thenReturn(List.of());

      RoutesResponse result = favoriteRouteService.getFavoriteRoutes(USER_ID);

      assertThat(result.getFavoriteRoutes()).isEmpty();
      verifyNoInteractions(favoriteRouteMapper);
    }

    @Test
    @DisplayName("delegates to processor with the resolved user's id")
    void delegatesToProcessor_withResolvedUserId() {
      UserEntity user = buildUser();
      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(favoriteRouteProcessor.getAllFavoriteRoutes(USER_ID)).thenReturn(List.of());

      favoriteRouteService.getFavoriteRoutes(USER_ID);

      verify(favoriteRouteProcessor).getAllFavoriteRoutes(USER_ID);
    }

    @Test
    @DisplayName("maps every entity in the list through the mapper")
    void mapsEveryEntity_throughMapper() {
      UserEntity user = buildUser();
      FavoriteRouteEntity e1 = buildEntity();
      FavoriteRouteEntity e2 = buildEntity();

      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(favoriteRouteProcessor.getAllFavoriteRoutes(USER_ID)).thenReturn(List.of(e1, e2));
      when(favoriteRouteMapper.toFavoriteRouteResponse(e1)).thenReturn(buildRouteResponse(e1));
      when(favoriteRouteMapper.toFavoriteRouteResponse(e2)).thenReturn(buildRouteResponse(e2));

      favoriteRouteService.getFavoriteRoutes(USER_ID);

      verify(favoriteRouteMapper).toFavoriteRouteResponse(e1);
      verify(favoriteRouteMapper).toFavoriteRouteResponse(e2);
    }

    @Test
    @DisplayName("propagates NotFoundException when user is not found")
    void propagatesNotFoundException_whenUserIsNotFound() {
      when(userFinder.findById(USER_ID)).thenThrow(new NotFoundException("User not found"));

      assertThatThrownBy(() -> favoriteRouteService.getFavoriteRoutes(USER_ID))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("User not found");

      verifyNoInteractions(favoriteRouteProcessor, favoriteRouteMapper);
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // updateFavoriteRoute
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("updateFavoriteRoute")
  class UpdateFavoriteRoute {

    private RouteUpdateRequest validUpdateRequest() {
      return RouteUpdateRequest.builder().name("New Name").build();
    }

    @Test
    @DisplayName("returns the mapped RouteResponse after a successful update")
    void returnsMappedResponse_afterSuccessfulUpdate() {
      UserEntity user = buildUser();
      FavoriteRouteEntity entity = buildEntity();
      RouteUpdateRequest request = validUpdateRequest();
      RouteResponse expected = buildRouteResponse(entity);

      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(favoriteRouteProcessor.updateFavoriteRoute(
              USER_ID,
              ROUTE_ID,
              request.getName(),
              request.getOrigin(),
              request.getDestination(),
              request.getTransportType()))
          .thenReturn(entity);
      when(favoriteRouteMapper.toFavoriteRouteResponse(entity)).thenReturn(expected);

      RouteResponse result = favoriteRouteService.updateFavoriteRoute(USER_ID, ROUTE_ID, request);

      assertThat(result).isSameAs(expected);
    }

    @Test
    @DisplayName("delegates to processor with resolved user's id, routeId, and all request fields")
    void delegatesToProcessor_withResolvedUserIdAndAllRequestFields() {
      UserEntity user = buildUser();
      FavoriteRouteEntity entity = buildEntity();
      final RouteUpdateRequest request =
          RouteUpdateRequest.builder()
              .name("Updated")
              .origin("Basel")
              .destination("Zurich")
              .transportType(TransportType.BUS)
              .build();

      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(favoriteRouteProcessor.updateFavoriteRoute(
              USER_ID, ROUTE_ID, "Updated", "Basel", "Zurich", TransportType.BUS))
          .thenReturn(entity);
      when(favoriteRouteMapper.toFavoriteRouteResponse(entity))
          .thenReturn(buildRouteResponse(entity));

      favoriteRouteService.updateFavoriteRoute(USER_ID, ROUTE_ID, request);

      verify(favoriteRouteProcessor)
          .updateFavoriteRoute(USER_ID, ROUTE_ID, "Updated", "Basel", "Zurich", TransportType.BUS);
    }

    @Test
    @DisplayName("maps the entity returned by the processor through the mapper")
    void mapsEntityReturnedByProcessor_throughMapper() {
      UserEntity user = buildUser();
      FavoriteRouteEntity entity = buildEntity();
      final RouteUpdateRequest request = validUpdateRequest();

      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(favoriteRouteProcessor.updateFavoriteRoute(any(), any(), any(), any(), any(), any()))
          .thenReturn(entity);
      when(favoriteRouteMapper.toFavoriteRouteResponse(entity))
          .thenReturn(buildRouteResponse(entity));

      favoriteRouteService.updateFavoriteRoute(USER_ID, ROUTE_ID, request);

      verify(favoriteRouteMapper).toFavoriteRouteResponse(entity);
    }

    // ── BadRequestException ───────────────────────────────────────────────

    @Test
    @DisplayName("throws BadRequestException when request is null")
    void throwsBadRequestException_whenRequestIsNull() {
      assertThatThrownBy(() -> favoriteRouteService.updateFavoriteRoute(USER_ID, ROUTE_ID, null))
          .isInstanceOf(BadRequestException.class)
          .hasMessage("The request cannot be completely empty or null");

      verifyNoInteractions(userFinder, favoriteRouteProcessor, favoriteRouteMapper);
    }

    @Test
    @DisplayName("throws BadRequestException when request is empty")
    void throwsBadRequestException_whenRequestIsEmpty() {
      RouteUpdateRequest emptyRequest = new RouteUpdateRequest();

      assertThatThrownBy(
              () -> favoriteRouteService.updateFavoriteRoute(USER_ID, ROUTE_ID, emptyRequest))
          .isInstanceOf(BadRequestException.class)
          .hasMessage("The request cannot be completely empty or null");

      verifyNoInteractions(userFinder, favoriteRouteProcessor, favoriteRouteMapper);
    }

    @Test
    @DisplayName("does not interact with userFinder when request is null")
    void doesNotInteractWithUserFinder_whenRequestIsNull() {
      assertThatThrownBy(() -> favoriteRouteService.updateFavoriteRoute(USER_ID, ROUTE_ID, null))
          .isInstanceOf(BadRequestException.class);

      verifyNoInteractions(userFinder);
    }

    // ── propagated exceptions ─────────────────────────────────────────────

    @Test
    @DisplayName("propagates NotFoundException when user is not found")
    void propagatesNotFoundException_whenUserIsNotFound() {
      when(userFinder.findById(USER_ID)).thenThrow(new NotFoundException("User not found"));

      assertThatThrownBy(
              () ->
                  favoriteRouteService.updateFavoriteRoute(USER_ID, ROUTE_ID, validUpdateRequest()))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("User not found");

      verifyNoInteractions(favoriteRouteProcessor, favoriteRouteMapper);
    }

    @Test
    @DisplayName("propagates NotFoundException thrown by the processor when route is not found")
    void propagatesNotFoundException_thrownByProcessor_whenRouteNotFound() {
      UserEntity user = buildUser();
      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(favoriteRouteProcessor.updateFavoriteRoute(any(), any(), any(), any(), any(), any()))
          .thenThrow(new NotFoundException("Route not found"));

      assertThatThrownBy(
              () ->
                  favoriteRouteService.updateFavoriteRoute(USER_ID, ROUTE_ID, validUpdateRequest()))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("Route not found");

      verifyNoInteractions(favoriteRouteMapper);
    }

    @Test
    @DisplayName("propagates ConflictException thrown by the processor on name conflict")
    void propagatesConflictException_thrownByProcessor_onNameConflict() {
      UserEntity user = buildUser();
      when(userFinder.findById(USER_ID)).thenReturn(user);
      when(favoriteRouteProcessor.updateFavoriteRoute(any(), any(), any(), any(), any(), any()))
          .thenThrow(new ConflictException("Favorite route name already exists"));

      assertThatThrownBy(
              () ->
                  favoriteRouteService.updateFavoriteRoute(USER_ID, ROUTE_ID, validUpdateRequest()))
          .isInstanceOf(ConflictException.class)
          .hasMessage("Favorite route name already exists");

      verifyNoInteractions(favoriteRouteMapper);
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // deleteFavoriteRoute
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("deleteFavoriteRoute")
  class DeleteFavoriteRoute {

    @Test
    @DisplayName("delegates deletion to processor with resolved user's id and routeId")
    void delegatesDeletionToProcessor_withResolvedUserIdAndRouteId() {
      UserEntity user = buildUser();
      when(userFinder.findById(USER_ID)).thenReturn(user);

      favoriteRouteService.deleteFavoriteRoute(USER_ID, ROUTE_ID);

      verify(favoriteRouteProcessor).deleteFavoriteRoute(USER_ID, ROUTE_ID);
    }

    @Test
    @DisplayName("does not interact with mapper during deletion")
    void doesNotInteractWithMapper_duringDeletion() {
      UserEntity user = buildUser();
      when(userFinder.findById(USER_ID)).thenReturn(user);

      favoriteRouteService.deleteFavoriteRoute(USER_ID, ROUTE_ID);

      verifyNoInteractions(favoriteRouteMapper);
    }

    @Test
    @DisplayName("propagates NotFoundException when user is not found")
    void propagatesNotFoundException_whenUserIsNotFound() {
      when(userFinder.findById(USER_ID)).thenThrow(new NotFoundException("User not found"));

      assertThatThrownBy(() -> favoriteRouteService.deleteFavoriteRoute(USER_ID, ROUTE_ID))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("User not found");

      verifyNoInteractions(favoriteRouteProcessor, favoriteRouteMapper);
    }

    @Test
    @DisplayName("propagates NotFoundException thrown by processor when route is not found")
    void propagatesNotFoundException_thrownByProcessor_whenRouteNotFound() {
      UserEntity user = buildUser();
      when(userFinder.findById(USER_ID)).thenReturn(user);
      doThrow(new NotFoundException("Route not found"))
          .when(favoriteRouteProcessor)
          .deleteFavoriteRoute(USER_ID, ROUTE_ID);

      assertThatThrownBy(() -> favoriteRouteService.deleteFavoriteRoute(USER_ID, ROUTE_ID))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("Route not found");
    }
  }
}
