package com.group4.swissrouteapi;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * ApiTransportsConnectionsStub
 *
 * <p>Utility class for stubbing the transport API `/connections` endpoint using {@link
 * com.github.tomakehurst.wiremock.junit5.WireMockExtension}. Provides predefined responses for
 * integration testing scenarios.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Stub connections by origin and destination.
 *   <li>Stub connections with date and/or time filters.
 *   <li>Stub connections with transportation type filters (single or multiple).
 *   <li>Stub "not found" responses for invalid queries.
 *   <li>Reset all registered stubs.
 * </ul>
 */
public class ApiTransportsConnectionsStub {

  private final WireMockExtension wireMock;

  public ApiTransportsConnectionsStub(WireMockExtension wireMock) {
    this.wireMock = wireMock;
  }

  /**
   * Stubs a standard connections response by origin and destination.
   *
   * @param from origin station name
   * @param to destination station name
   */
  public void stubConnectionsByFromAndTo(String from, String to) {
    wireMock.stubFor(
        get(urlPathEqualTo("/connections"))
            .withQueryParam("from", equalTo(from))
            .withQueryParam("to", equalTo(to))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBodyFile("api-transports/connections/connections-standard.json")));
  }

  /**
   * Stubs a connections response filtered by date.
   *
   * @param from origin station name
   * @param to destination station name
   * @param date date of travel (ISO format)
   */
  public void stubConnectionsByDate(String from, String to, String date) {
    wireMock.stubFor(
        get(urlPathEqualTo("/connections"))
            .withQueryParam("from", equalTo(from))
            .withQueryParam("to", equalTo(to))
            .withQueryParam("date", equalTo(date))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBodyFile("api-transports/connections/connections-by-datetime.json")));
  }

  /**
   * Stubs a connections response filtered by date and time.
   *
   * @param from origin station name
   * @param to destination station name
   * @param date date of travel (ISO format)
   * @param time time of travel (HH:mm:ss format)
   */
  public void stubConnectionsByDateAndTime(String from, String to, String date, String time) {
    wireMock.stubFor(
        get(urlPathEqualTo("/connections"))
            .withQueryParam("from", equalTo(from))
            .withQueryParam("to", equalTo(to))
            .withQueryParam("date", equalTo(date))
            .withQueryParam("time", equalTo(time))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBodyFile("api-transports/connections/connections-by-datetime.json")));
  }

  /**
   * Stubs a connections response filtered by date, time, and a single transportation type.
   *
   * @param from origin station name
   * @param to destination station name
   * @param date date of travel (ISO format)
   * @param time time of travel (HH:mm:ss format)
   * @param transportation transportation type filter (TRAIN, TRAM, etc.)
   */
  public void stubConnectionsByDateAndTimeAndTransportations(
      String from, String to, String date, String time, String transportation) {
    wireMock.stubFor(
        get(urlPathEqualTo("/connections"))
            .withQueryParam("from", equalTo(from))
            .withQueryParam("to", equalTo(to))
            .withQueryParam("date", equalTo(date))
            .withQueryParam("time", equalTo(time))
            .withQueryParam("transportations[]", equalTo(transportation))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBodyFile(
                        "api-transports/connections"
                            + "/connections-by-datetime-transportations.json")));
  }

  /**
   * Stubs a connections response filtered by date, time, and multiple transportation types.
   *
   * @param from origin station name
   * @param to destination station name
   * @param date date of travel (ISO format)
   * @param time time of travel (HH:mm:ss format)
   * @param transportations list of transportation type filters
   */
  public void stubConnectionsByDateAndTimeAndTransportations(
      String from, String to, String date, String time, List<String> transportations) {
    MappingBuilder stub =
        get(urlPathEqualTo("/connections"))
            .withQueryParam("from", equalTo(from))
            .withQueryParam("to", equalTo(to))
            .withQueryParam("date", equalTo(date))
            .withQueryParam("time", equalTo(time));

    stub =
        transportations.stream()
            .reduce(
                stub,
                (builder, type) -> builder.withQueryParam("transportations[]", equalTo(type)),
                (a, b) -> b);

    wireMock.stubFor(
        stub.willReturn(
            aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile(
                    "api-transports/connections/connections-by-two-transportations.json")));
  }

  /**
   * Stubs a "not found" response when no connections exist for the given parameters.
   *
   * @param from origin station name
   * @param to destination station name
   */
  public void stubConnectionsNotFound(String from, String to) {
    wireMock.stubFor(
        get(urlPathEqualTo("/connections"))
            .withQueryParam("from", equalTo(from))
            .withQueryParam("to", equalTo(to))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBodyFile("api-transports/connections/connections-not-found.json")));
  }

  public void reset() {
    wireMock.resetAll();
  }
}
