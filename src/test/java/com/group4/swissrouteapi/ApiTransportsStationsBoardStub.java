package com.group4.swissrouteapi;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.group4.swissrouteapi.config.constants.ApiPaths;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

public class ApiTransportsStationsBoardStub {

    private final WireMockExtension wireMock;

    public ApiTransportsStationsBoardStub(WireMockExtension wireMock) {
        this.wireMock = wireMock;
    }
    
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

    public void stubStationsBoardByTransportations(String station, String transportation) {
        wireMock.stubFor(
                get(urlPathEqualTo(ApiPaths.TransportApi.STATION_BOARD))
                        .withQueryParam("station", equalTo(station))
                        .withQueryParam("transportations[]", equalTo(transportation))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile("api-transports/stations/station-board-by-transportations.json")));
    }

    public void stubStationsBoardByTwoTransportations(
            String station, List<String> transportations) {
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
    
    public void stubStationsBoardByTransportationsNotFound(String station, String transportation) {
        wireMock.stubFor(
                get(urlPathEqualTo(ApiPaths.TransportApi.STATION_BOARD))
                        .withQueryParam("station", equalTo(station))
                        .withQueryParam("transportations[]", equalTo(transportation))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile("api-transports/stations/station-board-by-not-found-by-filters.json")));
    }
    
    public void stubStationsBoardByNotFound(String station, String id) {
        wireMock.stubFor(
                get(urlPathEqualTo(ApiPaths.TransportApi.STATION_BOARD))
                        .withQueryParam("station", equalTo(station))
                        .withQueryParam("id", equalTo(id))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpStatus.OK.value())
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBodyFile("api-transports/stations/station-board-by-not-found-by-station.json")));
    }
    
    
    public void stubStationsBoardAllFilters(String station, String id, String limit, List<String> transportations) {
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
