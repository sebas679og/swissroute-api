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
}
