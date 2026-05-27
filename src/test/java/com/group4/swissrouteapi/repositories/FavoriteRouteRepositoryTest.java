package com.group4.swissrouteapi.repositories;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.group4.swissrouteapi.AbstractIntegrationTest;
import com.group4.swissrouteapi.models.FavoriteRouteEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.providers.FavoriteRouteDataProvider;
import com.group4.swissrouteapi.providers.UserDataProvider;
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
}
