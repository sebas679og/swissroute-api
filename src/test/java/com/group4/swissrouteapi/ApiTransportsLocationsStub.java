package com.group4.swissrouteapi;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * ApiTransportsStub
 *
 * <p>Utility class for stubbing transport API endpoints using {@link
 * com.github.tomakehurst.wiremock.junit5.WireMockExtension}.
 *
 * <p>Provides predefined responses for the `/locations` endpoint to simulate different scenarios
 * during integration testing, including successful queries, not found results, and error responses
 * (4xx and 5xx).
 */
public class ApiTransportsLocationsStub {

  private final WireMockExtension wireMock;

  public ApiTransportsLocationsStub(WireMockExtension wireMock) {
    this.wireMock = wireMock;
  }

  /**
   * Stubs a successful response for the `/locations` endpoint when queried with the given
   * parameter.
   *
   * @param query the query string used to search locations
   */
  public void stubLocationsByQuery(String query) {
    wireMock.stubFor(
        get(urlPathEqualTo("/locations"))
            .withQueryParam("query", equalTo(query))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBodyFile("api-transports/locations/locations-by-query.json")));
  }

  /**
   * Stubs a successful response for the `/locations` endpoint when queried with the given
   * parameter.
   *
   * @param latitude the latitude for the location query
   * @param longitude the longitude for the location query
   */
  public void stubLocationsByCoordinates(Double latitude, Double longitude) {
    wireMock.stubFor(
        get(urlPathEqualTo("/locations"))
            .withQueryParam("x", equalTo(latitude.toString()))
            .withQueryParam("y", equalTo(longitude.toString()))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBodyFile("api-transports/locations/locations-by-coordinates.json")));
  }

  /**
   * Stubs a "not found" response for the `/locations` endpoint when queried with the given
   * parameter.
   *
   * @param query the query string used to search locations
   */
  public void stubLocationsByQueryNotFound(String query) {
    wireMock.stubFor(
        get(urlPathEqualTo("/locations"))
            .withQueryParam("query", equalTo(query))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBodyFile("api-transports/locations/locations-by-query-not-found.json")));
  }

  /**
   * Stubs a 4xx client error response for the `/locations` endpoint.
   *
   * @param query the query string used to search locations
   */
  public void stubLocationResponse4xx(String query) {
    wireMock.stubFor(
        get(urlPathEqualTo("/locations"))
            .withQueryParam("query", equalTo(query))
            .willReturn(
                aResponse()
                    .withStatus(423)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody("")));
  }

  /**
   * Stubs a 5xx server error response for the `/locations` endpoint.
   *
   * @param query the query string used to search locations
   */
  public void stubLocationResponse5xx(String query) {
    wireMock.stubFor(
        get(urlPathEqualTo("/locations"))
            .withQueryParam("query", equalTo(query))
            .willReturn(
                aResponse()
                    .withStatus(504)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody("")));
  }

  public void reset() {
    wireMock.resetAll();
  }
}
