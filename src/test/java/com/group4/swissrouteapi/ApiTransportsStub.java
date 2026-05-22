package com.group4.swissrouteapi;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

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
public class ApiTransportsStub {

  private final WireMockExtension wireMock;

  public ApiTransportsStub(WireMockExtension wireMock) {
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
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("api-transports/locations/locations-by-query.json")));
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
                    .withHeader("Content-Type", "application/json")
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
                    .withHeader("Content-Type", "application/json")
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
                    .withHeader("Content-Type", "application/json")
                    .withBody("")));
  }

  public void reset() {
    wireMock.resetAll();
  }
}
