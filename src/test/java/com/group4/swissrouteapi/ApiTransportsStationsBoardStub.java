package com.group4.swissrouteapi;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.group4.swissrouteapi.config.constants.ApiPaths;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * ApiTransportsStationsBoardStub
 *
 * <p>Utility class for stubbing the Transport API's station board endpoint in integration tests
 * using {@link com.github.tomakehurst.wiremock.junit5.WireMockExtension}.
 */
public class ApiTransportsStationsBoardStub {

  private final WireMockExtension wireMock;

  public ApiTransportsStationsBoardStub(WireMockExtension wireMock) {
    this.wireMock = wireMock;
  }

  /**
   * Stubs a station board response filtered by station name.
   *
   * @param station the station name to filter by
   */
  public void stubStationsBoardByStation(String station) {
    wireMock.stubFor(
        get(urlPathEqualTo(ApiPaths.TransportApi.STATION_BOARD))
            .withQueryParam("station", equalTo(station))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBodyFile("api-transports/stations/station-board.json")));
  }

  /**
   * Stubs a station board response filtered by station name and unique identifier.
   *
   * @param station the station name
   * @param id the unique station identifier
   */
  public void stubStationsBoardById(String station, String id) {
    wireMock.stubFor(
        get(urlPathEqualTo(ApiPaths.TransportApi.STATION_BOARD))
            .withQueryParam("station", equalTo(station))
            .withQueryParam("id", equalTo(id))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBodyFile("api-transports/stations/station-board-by-id.json")));
  }

  /**
   * Stubs a station board response filtered by station name and a limit on results.
   *
   * @param station the station name
   * @param limit the maximum number of connections to return
   */
  public void stubStationsBoardByLimit(String station, String limit) {
    wireMock.stubFor(
        get(urlPathEqualTo(ApiPaths.TransportApi.STATION_BOARD))
            .withQueryParam("station", equalTo(station))
            .withQueryParam("limit", equalTo(limit))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBodyFile("api-transports/stations/station-board-by-limit-15.json")));
  }

  /**
   * Stubs a station board response filtered by station name and a single transport type.
   *
   * @param station the station name
   * @param transportation the transport type (e.g., train, bus)
   */
  public void stubStationsBoardByTransportations(String station, String transportation) {
    wireMock.stubFor(
        get(urlPathEqualTo(ApiPaths.TransportApi.STATION_BOARD))
            .withQueryParam("station", equalTo(station))
            .withQueryParam("transportations[]", equalTo(transportation))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBodyFile(
                        "api-transports/stations/station-board-by-transportations-train.json")));
  }

  /**
   * Stubs a station board response filtered by station name and multiple transport types.
   *
   * @param station the station name
   * @param transportations list of transport types to filter by
   */
  public void stubStationsBoardByTwoTransportations(String station, List<String> transportations) {
    MappingBuilder stub =
        get(urlPathEqualTo(ApiPaths.TransportApi.STATION_BOARD))
            .withQueryParam("station", equalTo(station));

    stub =
        transportations.stream()
            .reduce(
                stub,
                (builder, type) -> builder.withQueryParam("transportations[]", equalTo(type)),
                (a, b) -> b);

    wireMock.stubFor(
        stub.willReturn(
            aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile(
                    "api-transports/stations/station-board-by-two-transportations.json")));
  }

  /**
   * Stubs a station board response where the limit is set to zero.
   *
   * @param station the station name
   * @param limit the limit value (expected to be "0")
   */
  public void stubStationsBoardByLimitIsZero(String station, String limit) {
    wireMock.stubFor(
        get(urlPathEqualTo(ApiPaths.TransportApi.STATION_BOARD))
            .withQueryParam("station", equalTo(station))
            .withQueryParam("limit", equalTo(limit))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBodyFile("api-transports/stations/station-board-by-limit-0.json")));
  }

  /**
   * Stubs a station board response where filtering by transport type yields no results.
   *
   * @param station the station name
   * @param transportation the transport type filter
   */
  public void stubStationsBoardByTransportationsNotFound(String station, String transportation) {
    wireMock.stubFor(
        get(urlPathEqualTo(ApiPaths.TransportApi.STATION_BOARD))
            .withQueryParam("station", equalTo(station))
            .withQueryParam("transportations[]", equalTo(transportation))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBodyFile(
                        "api-transports/stations/station-board-not-found-by-filters.json")));
  }

  /**
   * Stubs a station board response where filtering by station and ID yields no results.
   *
   * @param station the station name
   * @param id the unique station identifier
   */
  public void stubStationsBoardByNotFound(String station, String id) {
    wireMock.stubFor(
        get(urlPathEqualTo(ApiPaths.TransportApi.STATION_BOARD))
            .withQueryParam("station", equalTo(station))
            .withQueryParam("id", equalTo(id))
            .willReturn(
                aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBodyFile(
                        "api-transports/stations/station-board-not-found-by-station.json")));
  }

  /**
   * Stubs a station board response applying all filters: station name, ID, limit, and multiple
   * transport types.
   *
   * @param station the station name
   * @param id the unique station identifier
   * @param limit the maximum number of connections
   * @param transportations list of transport types to filter by
   */
  public void stubStationsBoardAllFilters(
      String station, String id, String limit, List<String> transportations) {
    MappingBuilder stub =
        get(urlPathEqualTo(ApiPaths.TransportApi.STATION_BOARD))
            .withQueryParam("station", equalTo(station))
            .withQueryParam("id", equalTo(id))
            .withQueryParam("limit", equalTo(limit));

    stub =
        transportations.stream()
            .reduce(
                stub,
                (builder, type) -> builder.withQueryParam("transportations[]", equalTo(type)),
                (a, b) -> b);

    wireMock.stubFor(
        stub.willReturn(
            aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBodyFile(
                    "api-transports/stations/station-board-by-two-transportations.json")));
  }

  public void reset() {
    wireMock.resetAll();
  }
}
