package com.group4.swissrouteapi.providers;

import com.group4.swissrouteapi.models.FavoriteRouteEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.utils.enums.TransportationType;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Mock Factory Data for FavoriteRouteEntity. */
public class FavoriteRouteMockFactory {

  private static final Random RANDOM = new Random();

  private static final List<String> SWISS_CANTONS =
      List.of(
          "Aargau",
          "Appenzell Ausserrhoden",
          "Appenzell Innerrhoden",
          "Basel-Stadt",
          "Basel-Landschaft",
          "Bern",
          "Fribourg",
          "Geneva",
          "Glarus",
          "Graubünden",
          "Jura",
          "Lucerne",
          "Neuchâtel",
          "Nidwalden",
          "Obwalden",
          "St. Gallen",
          "Schaffhausen",
          "Schwyz",
          "Solothurn",
          "Ticino",
          "Thurgau",
          "Uri",
          "Valais",
          "Vaud",
          "Zug",
          "Zürich");

  private static final List<String> ROUTE_NAMES =
      List.of(
          "Morning Route",
          "Work Trip",
          "Weekend Ride",
          "Daily Commute",
          "Night Train",
          "Fast Route",
          "City Connection",
          "Family Trip",
          "Mountain Route",
          "Express Line");

  /**
   * Creates a mock {@link FavoriteRouteEntity}.
   *
   * @param user owner user
   * @return transient entity ready to persist
   */
  public static FavoriteRouteEntity createMockFavoriteRoute(UserEntity user) {

    String origin = getRandomCanton();
    String destination;

    do {
      destination = getRandomCanton();
    } while (origin.equals(destination));

    return FavoriteRouteEntity.builder()
        .user(user)
        .name(generateRandomRouteName())
        .origin(origin)
        .destination(destination)
        .transportType(getRandomTransportType())
        .build();
  }

  /**
   * Creates multiple mock favorite routes.
   *
   * @param user owner user
   * @param amount number of entities
   * @return list of entities
   */
  public static List<FavoriteRouteEntity> createMockFavoriteRoutes(UserEntity user, int amount) {

    List<FavoriteRouteEntity> routes = new ArrayList<>();

    for (int i = 0; i < amount; i++) {
      routes.add(createMockFavoriteRoute(user));
    }

    return routes;
  }

  private static String getRandomCanton() {
    return SWISS_CANTONS.get(RANDOM.nextInt(SWISS_CANTONS.size()));
  }

  private static String generateRandomRouteName() {
    String baseName = ROUTE_NAMES.get(RANDOM.nextInt(ROUTE_NAMES.size()));
    int suffix = RANDOM.nextInt(10000);

    return baseName + " " + suffix;
  }

  private static TransportationType getRandomTransportType() {
    TransportationType[] values = TransportationType.values();
    return values[RANDOM.nextInt(values.length)];
  }
}
