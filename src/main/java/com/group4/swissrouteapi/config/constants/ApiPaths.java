package com.group4.swissrouteapi.config.constants;

/**
 * ApiPaths
 *
 * <p>Utility class that centralizes API endpoint path constants used throughout the application.
 *
 * <p>Designed as a {@code final} class with a private constructor to prevent instantiation and
 * extension.
 */
public final class ApiPaths {

  private ApiPaths() {}

  /**
   * Auth
   *
   * <p>Defines API paths related to authentication operations.
   *
   * <p>Provides a constant for user registration endpoint.
   */
  public static final class Auth {
    public static final String REGISTER = "/api/users/register";
    public static final String LOGIN = "/api/users/login";
  }

  /**
   * Station
   *
   * <p>Defines API path constants related to station operations.
   *
   * <p>Serves as a centralized holder for endpoint URIs used by the application when interacting
   * with station-related resources.
   *
   * <p>Currently provides a constant for the stations endpoint.
   */
  public static final class Station {
    public static final String STATIONS = "/api/stations";
  }

  /**
   * Connection
   *
   * <p>Utility class holding API path constants related to transport connections.
   *
   * <p>Provides:
   *
   * <ul>
   *   <li>{@link #CONNECTIONS} → Base path for connections endpoint ({@code /api/connections}).
   * </ul>
   *
   * <p>Declared as {@code static final} to group constants in a type-safe manner.
   */
  public static final class Connection {
    public static final String CONNECTIONS = "/api/connections";
  }

  /**
   * History
   *
   * <p>Utility class holding API path constants for history endpoints.
   *
   * <p>Provides:
   *
   * <ul>
   *   <li>{@link #HISTORY} → Base path for retrieving history records ({@code /api/history}).
   *   <li>{@link #HISTORY_ITEM} → Path for deleting a specific history record by ID ({@code
   *       /api/history/{itemId}}).
   * </ul>
   *
   * <p>Declared as {@code static final} to group constants in a type-safe manner.
   */
  public static final class History {
    public static final String HISTORY = "/api/history";
    public static final String HISTORY_ITEM = "/api/history/{itemId}";
  }

  /**
   * FavoriteRoutes
   *
   * <p>Static holder class for API endpoint path constants related to favorite routes
   * functionality.
   */
  public static final class FavoriteRoutes {
    public static final String FAVORITE_ROUTES = "/api/favorite-routes";
    public static final String FAVORITE_ROUTE = "/api/favorite-routes/{routeId}";
  }

  /**
   * FavoriteStations
   *
   * <p>Static inner class that defines API path constants for managing the authenticated user's
   * favorite stations.
   */
  public static final class FavoriteStations {
    public static final String FAVORITE_STATIONS = "/api/favorite-stations";
    public static final String FAVORITE_STATION = "/api/favorite-stations/{externalStationId}";
  }

  public static final class StationBoard {
    public static final String STATION_BOARD = "/api/station-board";
  }

  /**
   * Docs
   *
   * <p>Defines API paths related to documentation endpoints.
   *
   * <p>Includes constants for Swagger UI and OpenAPI specification access.
   */
  public static final class Docs {
    public static final String SWAGGER_UI = "/swagger-ui/**";
    public static final String API_DOCS = "/v3/api-docs/**";
  }

  /**
   * TransportApi
   *
   * <p>Defines API path constants related to transport operations.
   *
   * <p>Serves as a centralized holder for endpoint URIs used by the transport client when
   * interacting with external services.
   */
  public static final class TransportApi {
    public static final String LOCATIONS = "/locations";
    public static final String CONNECTIONS = "/connections";
    public static final String STATION_BOARD = "/stationboard";
  }
}
