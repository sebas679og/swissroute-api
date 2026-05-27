package com.group4.swissrouteapi.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.group4.swissrouteapi.AbstractIntegrationTest;
import com.group4.swissrouteapi.models.FavoriteRouteEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.providers.FavoriteRouteDataProvider;
import com.group4.swissrouteapi.providers.UserDataProvider;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class FavoriteRouteRepositoryTest extends AbstractIntegrationTest {

  @Autowired FavoriteRouteRepository favoriteRouteRepository;
  @Autowired UserRepository userRepository;

  private UserEntity userA;
  private UserEntity userB;

  @BeforeEach
  void setUp() {
    favoriteRouteRepository.deleteAll();
    userRepository.deleteAll();
    userA = userRepository.save(UserDataProvider.createMockUser());
    userB = userRepository.save(UserDataProvider.createAnotherMockUser());
  }

  // ─── existsByUserIdAndName ───────────────────────────────────────────────────

  @Test
  void shouldReturnTrueWhenUserHasRouteWithThatName() {
    FavoriteRouteEntity saved =
        favoriteRouteRepository.save(FavoriteRouteDataProvider.createMockRoute(userA));

    assertTrue(favoriteRouteRepository.existsByUserIdAndName(userA.getId(), saved.getName()));
  }

  @Test
  void shouldReturnFalseWhenNameDoesNotExistForUser() {
    favoriteRouteRepository.save(FavoriteRouteDataProvider.createMockRoute(userA));

    assertFalse(favoriteRouteRepository.existsByUserIdAndName(userA.getId(), "Non Existent Route"));
  }

  @Test
  void shouldReturnFalseWhenRepositoryIsEmpty() {
    assertFalse(favoriteRouteRepository.existsByUserIdAndName(userA.getId(), "Any Route"));
  }

  @Test
  void shouldReturnFalseWhenNameExistsButBelongsToAnotherUser() {
    FavoriteRouteEntity saved =
        favoriteRouteRepository.save(FavoriteRouteDataProvider.createMockRoute(userA));

    assertFalse(favoriteRouteRepository.existsByUserIdAndName(userB.getId(), saved.getName()));
  }

  @Test
  void shouldReturnTrueOnlyForTheMatchingUserAmongMultiple() {
    FavoriteRouteEntity routeA =
        favoriteRouteRepository.save(FavoriteRouteDataProvider.createMockRoute(userA));
    favoriteRouteRepository.save(FavoriteRouteDataProvider.createAnotherMockRoute(userB));

    assertTrue(favoriteRouteRepository.existsByUserIdAndName(userA.getId(), routeA.getName()));
    assertFalse(favoriteRouteRepository.existsByUserIdAndName(userB.getId(), routeA.getName()));
  }

  @Test
  void shouldAllowSameNameForDifferentUsers() {
    favoriteRouteRepository.save(FavoriteRouteDataProvider.createMockRoute(userA));
    favoriteRouteRepository.save(FavoriteRouteDataProvider.createMockRoute(userB));

    assertTrue(
        favoriteRouteRepository.existsByUserIdAndName(
            userA.getId(), FavoriteRouteDataProvider.ROUTE_NAME));
    assertTrue(
        favoriteRouteRepository.existsByUserIdAndName(
            userB.getId(), FavoriteRouteDataProvider.ROUTE_NAME));
  }

  // ─── existsByUserIdAndNameAndIdNot ───────────────────────────────────────────

  @Test
  void shouldReturnFalseWhenTheOnlyMatchIsTheExcludedRoute() {
    // Escenario típico de renombrar una ruta: el nombre ya existe pero es el propio registro
    FavoriteRouteEntity saved =
        favoriteRouteRepository.save(FavoriteRouteDataProvider.createMockRoute(userA));

    assertFalse(
        favoriteRouteRepository.existsByUserIdAndNameAndIdNot(
            userA.getId(), saved.getName(), saved.getId()));
  }

  @Test
  void shouldReturnTrueWhenAnotherRouteOfSameUserHasTheSameName() {
    FavoriteRouteEntity routeA1 =
        favoriteRouteRepository.save(FavoriteRouteDataProvider.createMockRoute(userA));
    FavoriteRouteEntity routeA2 =
        favoriteRouteRepository.save(FavoriteRouteDataProvider.createAnotherMockRoute(userA));

    // Intentar renombrar routeA2 con el nombre de routeA1 → conflicto
    assertTrue(
        favoriteRouteRepository.existsByUserIdAndNameAndIdNot(
            userA.getId(), routeA1.getName(), routeA2.getId()));
  }

  @Test
  void shouldReturnFalseWhenSameNameExistsForDifferentUser() {
    FavoriteRouteEntity routeA =
        favoriteRouteRepository.save(FavoriteRouteDataProvider.createMockRoute(userA));
    FavoriteRouteEntity routeB =
        favoriteRouteRepository.save(FavoriteRouteDataProvider.createAnotherMockRoute(userB));

    // El nombre de routeA existe, pero pertenece a userA, no a userB
    assertFalse(
        favoriteRouteRepository.existsByUserIdAndNameAndIdNot(
            userB.getId(), routeA.getName(), routeB.getId()));
  }

  @Test
  void shouldReturnFalseWhenNameDoesNotExistAtAllForUser() {
    FavoriteRouteEntity saved =
        favoriteRouteRepository.save(FavoriteRouteDataProvider.createMockRoute(userA));

    assertFalse(
        favoriteRouteRepository.existsByUserIdAndNameAndIdNot(
            userA.getId(), "Non Existent Route", saved.getId()));
  }

  // ─── findByUserIdAndId ───────────────────────────────────────────────────────

  @Test
  void shouldReturnRouteWhenUserIdAndIdMatch() {
    FavoriteRouteEntity saved =
        favoriteRouteRepository.save(FavoriteRouteDataProvider.createMockRoute(userA));

    Optional<FavoriteRouteEntity> result =
        favoriteRouteRepository.findByUserIdAndId(userA.getId(), saved.getId());

    assertTrue(result.isPresent());
    assertEquals(saved.getId(), result.get().getId());
    assertEquals(saved.getName(), result.get().getName());
    assertEquals(saved.getOrigin(), result.get().getOrigin());
    assertEquals(saved.getDestination(), result.get().getDestination());
  }

  @Test
  void shouldReturnEmptyWhenRouteIdDoesNotExist() {
    Optional<FavoriteRouteEntity> result =
        favoriteRouteRepository.findByUserIdAndId(userA.getId(), UUID.randomUUID());

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnEmptyWhenRouteBelongsToAnotherUser() {
    FavoriteRouteEntity saved =
        favoriteRouteRepository.save(FavoriteRouteDataProvider.createMockRoute(userA));

    Optional<FavoriteRouteEntity> result =
        favoriteRouteRepository.findByUserIdAndId(userB.getId(), saved.getId());

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnCorrectRouteAmongMultipleForSameUser() {
    FavoriteRouteEntity routeA1 =
        favoriteRouteRepository.save(FavoriteRouteDataProvider.createMockRoute(userA));
    FavoriteRouteEntity routeA2 =
        favoriteRouteRepository.save(FavoriteRouteDataProvider.createAnotherMockRoute(userA));

    Optional<FavoriteRouteEntity> result =
        favoriteRouteRepository.findByUserIdAndId(userA.getId(), routeA1.getId());

    assertTrue(result.isPresent());
    assertEquals(routeA1.getId(), result.get().getId());
    assertNotEquals(routeA2.getId(), result.get().getId());
  }

  // ─── findByUserId ────────────────────────────────────────────────────────────

  @Test
  void shouldReturnAllRoutesForUser() {
    favoriteRouteRepository.save(FavoriteRouteDataProvider.createMockRoute(userA));
    favoriteRouteRepository.save(FavoriteRouteDataProvider.createAnotherMockRoute(userA));

    List<FavoriteRouteEntity> result = favoriteRouteRepository.findByUserId(userA.getId());

    assertEquals(2, result.size());
  }

  @Test
  void shouldReturnEmptyListWhenUserHasNoRoutes() {
    favoriteRouteRepository.save(FavoriteRouteDataProvider.createMockRoute(userB));

    List<FavoriteRouteEntity> result = favoriteRouteRepository.findByUserId(userA.getId());

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnEmptyListWhenRepositoryIsEmpty() {
    List<FavoriteRouteEntity> result = favoriteRouteRepository.findByUserId(userA.getId());

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnOnlyRoutesOfRequestedUser() {
    FavoriteRouteEntity routeA =
        favoriteRouteRepository.save(FavoriteRouteDataProvider.createMockRoute(userA));
    favoriteRouteRepository.save(FavoriteRouteDataProvider.createMockRoute(userB));

    List<FavoriteRouteEntity> result = favoriteRouteRepository.findByUserId(userA.getId());

    assertEquals(1, result.size());
    assertEquals(routeA.getId(), result.getFirst().getId());
  }
}
