package com.group4.swissrouteapi.providers;

import com.group4.swissrouteapi.models.FavoriteStationEntity;
import com.group4.swissrouteapi.models.UserEntity;

/** FavoriteStationDataProvider Mock, test. */
public class FavoriteStationDataProvider {

  public static final String EXTERNAL_STATION_ID_A = "station-001";
  public static final String STATION_NAME_A = "Madrid Atocha";

  public static final String EXTERNAL_STATION_ID_B = "station-002";
  public static final String STATION_NAME_B = "Barcelona Sants";

  /**
   * Creates a mock {@link FavoriteStationEntity} for the given user.
   *
   * <p>{@code createdAt} is set automatically by the {@code @PrePersist} hook on persist.
   *
   * @param user the owning {@link UserEntity}
   * @return a transient mock entity ready to be saved
   */
  public static FavoriteStationEntity createMockStation(UserEntity user) {
    return FavoriteStationEntity.builder()
        .user(user)
        .externalStationId(EXTERNAL_STATION_ID_A)
        .stationName(STATION_NAME_A)
        .build();
  }

  /**
   * Creates an alternative mock {@link FavoriteStationEntity} with a different station.
   *
   * <p>Useful for scenarios requiring two distinct favorite stations for the same user.
   *
   * @param user the owning {@link UserEntity}
   * @return a transient mock entity ready to be saved
   */
  public static FavoriteStationEntity createAnotherMockStation(UserEntity user) {
    return FavoriteStationEntity.builder()
        .user(user)
        .externalStationId(EXTERNAL_STATION_ID_B)
        .stationName(STATION_NAME_B)
        .build();
  }
}
