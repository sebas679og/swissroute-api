package com.group4.swissrouteapi.providers;

import com.group4.swissrouteapi.models.FavoriteRouteEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.utils.enums.TransportType;

/** Data provider for FavoriteRouteEntity entity. */
public class FavoriteRouteDataProvider {

  public static final String ROUTE_NAME = "Morning Commute";
  public static final String ORIGIN = "Madrid";
  public static final String DESTINATION = "Barcelona";
  public static final TransportType TYPE = TransportType.TRAIN;

  public static final String ALT_ROUTE_NAME = "Weekend Trip";
  public static final String ALT_ORIGIN = "Barcelona";
  public static final String ALT_DESTINATION = "Valencia";
  public static final TransportType ALT_TYPE = TransportType.BUS;

  /**
   * Creates a mock {@link FavoriteRouteEntity} for the given user.
   *
   * <p>{@code createdAt} is set automatically by the {@code @PrePersist} hook on persist.
   *
   * @param user the owning {@link UserEntity}
   * @return a transient mock entity ready to be saved
   */
  public static FavoriteRouteEntity createMockRoute(UserEntity user) {
    return FavoriteRouteEntity.builder()
        .user(user)
        .name(ROUTE_NAME)
        .origin(ORIGIN)
        .destination(DESTINATION)
        .transportType(TYPE)
        .build();
  }

  /**
   * Creates an alternative mock {@link FavoriteRouteEntity} with a different name and route.
   *
   * <p>Useful for scenarios requiring two distinct routes for the same user.
   *
   * @param user the owning {@link UserEntity}
   * @return a transient mock entity ready to be saved
   */
  public static FavoriteRouteEntity createAnotherMockRoute(UserEntity user) {
    return FavoriteRouteEntity.builder()
        .user(user)
        .name(ALT_ROUTE_NAME)
        .origin(ALT_ORIGIN)
        .destination(ALT_DESTINATION)
        .transportType(ALT_TYPE)
        .build();
  }
}
