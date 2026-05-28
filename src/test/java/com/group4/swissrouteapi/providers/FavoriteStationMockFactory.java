package com.group4.swissrouteapi.providers;

import com.group4.swissrouteapi.models.FavoriteStationEntity;
import com.group4.swissrouteapi.models.UserEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/** Mock Factory Data for FavoriteStationEntity. */
public class FavoriteStationMockFactory {

  private static final Random RANDOM = new Random();
  private static final AtomicLong STATION_ID_COUNTER = new AtomicLong(8_503_000L);

  private static final List<String> SWISS_STATIONS =
      List.of(
          "Zürich HB",
          "Bern",
          "Basel SBB",
          "Geneva",
          "Lausanne",
          "Luzern",
          "Winterthur",
          "St. Gallen",
          "Lugano",
          "Biel/Bienne",
          "Thun",
          "Köniz",
          "La Chaux-de-Fonds",
          "Schaffhausen",
          "Fribourg",
          "Chur",
          "Vernier",
          "Neuchâtel",
          "Uster",
          "Sion",
          "Emmen",
          "Renens",
          "Lancy",
          "Davos Platz",
          "Interlaken Ost",
          "Zermatt");

  /**
   * Creates a mock {@link FavoriteStationEntity}.
   *
   * @param user owner user
   * @return transient entity ready to persist
   */
  public static FavoriteStationEntity createMockFavoriteStation(UserEntity user) {
    return FavoriteStationEntity.builder()
        .user(user)
        .externalStationId(generateExternalStationId())
        .stationName(getRandomStationName())
        .build();
  }

  /**
   * Creates a mock {@link FavoriteStationEntity} with a specific station name.
   *
   * @param user owner user
   * @param stationName station display name
   * @return transient entity ready to persist
   */
  public static FavoriteStationEntity createMockFavoriteStation(
      UserEntity user, String stationName) {
    return FavoriteStationEntity.builder()
        .user(user)
        .externalStationId(generateExternalStationId())
        .stationName(stationName)
        .build();
  }

  /**
   * Creates multiple mock favorite stations.
   *
   * @param user owner user
   * @param amount number of entities
   * @return list of entities
   */
  public static List<FavoriteStationEntity> createMockFavoriteStations(
      UserEntity user, int amount) {
    List<FavoriteStationEntity> stations = new ArrayList<>();
    for (int i = 0; i < amount; i++) {
      stations.add(createMockFavoriteStation(user));
    }
    return stations;
  }

  private static String getRandomStationName() {
    return SWISS_STATIONS.get(RANDOM.nextInt(SWISS_STATIONS.size()));
  }

  private static String generateExternalStationId() {
    return String.valueOf(STATION_ID_COUNTER.getAndIncrement());
  }
}
