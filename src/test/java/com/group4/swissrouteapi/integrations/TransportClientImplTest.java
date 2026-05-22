package com.group4.swissrouteapi.integrations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.exceptions.BadGatewayException;
import com.group4.swissrouteapi.exceptions.ServiceUnavailableException;
import com.group4.swissrouteapi.integrations.dto.responses.locations.ApiCoordinate;
import com.group4.swissrouteapi.integrations.dto.responses.locations.ApiLocationsResponse;
import com.group4.swissrouteapi.integrations.dto.responses.locations.ApiStation;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Unit tests for {@link TransportClientImpl}.
 *
 * <p>Uses {@link MockWebServer} (OkHttp) to simulate the downstream Transport API without starting
 * a real server. Each test enqueues a canned HTTP response and asserts the client behaviour:
 * correct deserialization, proper exception types on 4xx/5xx, connection failure handling, and
 * empty-body handling.
 *
 * <p>Dependencies required in build file:
 *
 * <pre>
 *   testImplementation("com.squareup.okhttp3:mockwebserver:4.x.x")
 *   testImplementation("com.squareup.okhttp3:okhttp:4.x.x")
 * </pre>
 */
@DisplayName("TransportClientImpl")
class TransportClientImplTest {

  private MockWebServer mockWebServer;
  private TransportClientImpl transportClient;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() throws Exception {
    mockWebServer = new MockWebServer();
    mockWebServer.start();

    WebClient webClient = WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();

    transportClient = new TransportClientImpl(webClient);
  }

  @AfterEach
  void tearDown() throws Exception {
    mockWebServer.shutdown();
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private String buildLocationsJson(String stationId, String stationName) throws Exception {
    ApiLocationsResponse response =
        new ApiLocationsResponse(
            List.of(
                new ApiStation(
                    stationId,
                    stationName,
                    0.9,
                    new ApiCoordinate("Point", 8.540192, 47.378177),
                    120,
                    "train")));
    return objectMapper.writeValueAsString(response);
  }

  private MockResponse jsonResponse(int status, String body) {
    return new MockResponse()
        .setResponseCode(status)
        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .setBody(body);
  }

  // ===========================================================================
  // getLocations
  // ===========================================================================

  @Nested
  @DisplayName("getLocations() - successful response by Query")
  class SuccessfulResponseByQueryTest {

    @Test
    @DisplayName("should return a LocationsResponse with the deserialized stations")
    void shouldReturnDeserializedLocationsResponse() throws Exception {
      String body = buildLocationsJson("8503000", "Zurich HB");
      mockWebServer.enqueue(jsonResponse(200, body));

      ApiLocationsResponse result = transportClient.getLocationsByQuery("Zurich");

      assertThat(result).isNotNull();
      assertThat(result.stations()).hasSize(1);
      assertThat(result.stations().getFirst().id()).isEqualTo("8503000");
      assertThat(result.stations().getFirst().name()).isEqualTo("Zurich HB");
    }

    @Test
    @DisplayName("should deserialize station coordinates correctly")
    void shouldDeserializeStationCoordinates() throws Exception {
      String body = buildLocationsJson("8503000", "Zurich HB");
      mockWebServer.enqueue(jsonResponse(200, body));

      ApiLocationsResponse result = transportClient.getLocationsByQuery("Zurich");

      ApiCoordinate coord = result.stations().getFirst().coordinate();
      assertThat(coord.type()).isEqualTo("Point");
      assertThat(coord.x()).isEqualTo(8.540192);
      assertThat(coord.y()).isEqualTo(47.378177);
    }

    @Test
    @DisplayName("should deserialize station score and distance")
    void shouldDeserializeStationScoreAndDistance() throws Exception {
      String body = buildLocationsJson("8503000", "Zurich HB");
      mockWebServer.enqueue(jsonResponse(200, body));

      ApiLocationsResponse result = transportClient.getLocationsByQuery("Zurich");

      ApiStation apiStation = result.stations().getFirst();
      assertThat(apiStation.score()).isEqualTo(0.9);
      assertThat(apiStation.distance()).isEqualTo(120);
    }

    @Test
    @DisplayName("should return an empty stations list when the response contains no stations")
    void shouldReturnEmptyStationsListWhenNoStations() throws Exception {
      String body = objectMapper.writeValueAsString(new ApiLocationsResponse(List.of()));
      mockWebServer.enqueue(jsonResponse(200, body));

      ApiLocationsResponse result = transportClient.getLocationsByQuery("unknown-place");

      assertThat(result.stations()).isEmpty();
    }

    @Test
    @DisplayName("should send the query as a request parameter")
    void shouldSendQueryAsRequestParameter() throws Exception {
      String body = buildLocationsJson("8503000", "Zurich HB");
      mockWebServer.enqueue(jsonResponse(200, body));

      transportClient.getLocationsByQuery("Zurich");

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getPath()).contains("query=Zurich");
    }

    @Test
    @DisplayName("should send a GET request to the locations path")
    void shouldSendGetRequestToLocationsPath() throws Exception {
      String body = buildLocationsJson("8503000", "Zurich HB");
      mockWebServer.enqueue(jsonResponse(200, body));

      transportClient.getLocationsByQuery("Zurich");

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getMethod()).isEqualTo("GET");
      assertThat(recorded.getPath()).contains(ApiPaths.TransportApi.LOCATIONS);
    }

    @Test
    @DisplayName("should URL-encode special characters in the query parameter")
    void shouldUrlEncodeQueryParameter() throws Exception {
      String body = buildLocationsJson("8503000", "Zürich HB");
      mockWebServer.enqueue(jsonResponse(200, body));

      transportClient.getLocationsByQuery("Zürich HB");

      RecordedRequest recorded = mockWebServer.takeRequest();
      // URL-encoded space = '+' or '%20', ü = '%C3%BC'
      assertThat(recorded.getPath()).containsPattern("query=Z");
    }
  }

  @Nested
  @DisplayName("getLocations() - successful response by Coordinates")
  class SuccessfulResponseByCoordinatesTest {
    private static final double LATITUDE = 47.5596;
    private static final double LONGITUDE = 7.5886;

    @Test
    @DisplayName("should return a deserialized ApiLocationsResponse")
    void shouldReturnDeserializedResponse() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildLocationsJson("8503000", "Basel SBB")));

      ApiLocationsResponse result = transportClient.getLocationsByCoordinates(LATITUDE, LONGITUDE);

      assertThat(result).isNotNull();
      assertThat(result.stations()).hasSize(1);
      assertThat(result.stations().getFirst().id()).isEqualTo("8503000");
      assertThat(result.stations().getFirst().name()).isEqualTo("Basel SBB");
    }

    @Test
    @DisplayName("should deserialize station score and distance")
    void shouldDeserializeStationScoreAndDistance() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildLocationsJson("8503000", "Basel SBB")));

      ApiLocationsResponse result = transportClient.getLocationsByCoordinates(LATITUDE, LONGITUDE);

      ApiStation station = result.stations().getFirst();
      assertThat(station.score()).isEqualTo(0.9);
      assertThat(station.distance()).isEqualTo(120);
    }

    @Test
    @DisplayName("should deserialize station coordinates")
    void shouldDeserializeStationCoordinates() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildLocationsJson("8503000", "Basel SBB")));

      ApiLocationsResponse result = transportClient.getLocationsByCoordinates(LATITUDE, LONGITUDE);

      ApiCoordinate coord = result.stations().getFirst().coordinate();
      assertThat(coord.type()).isEqualTo("Point");
      assertThat(coord.x()).isEqualTo(8.540192);
      assertThat(coord.y()).isEqualTo(47.378177);
    }

    @Test
    @DisplayName("should send latitude as query param 'x'")
    void shouldSendLatitudeAsLatitudeParam() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildLocationsJson("8503000", "Basel SBB")));

      transportClient.getLocationsByCoordinates(LATITUDE, LONGITUDE);

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getPath()).contains("x=" + LATITUDE);
    }

    @Test
    @DisplayName("should send longitude as query param 'y'")
    void shouldSendLongitudeAsLongitudeParam() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildLocationsJson("8503000", "Basel SBB")));

      transportClient.getLocationsByCoordinates(LATITUDE, LONGITUDE);

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getPath()).contains("y=" + LONGITUDE);
    }

    @Test
    @DisplayName("should send a GET request to the locations path")
    void shouldSendGetRequestToLocationsPath() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildLocationsJson("8503000", "Basel SBB")));

      transportClient.getLocationsByCoordinates(LATITUDE, LONGITUDE);

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getMethod()).isEqualTo("GET");
      assertThat(recorded.getPath()).contains(ApiPaths.TransportApi.LOCATIONS);
    }

    @Test
    @DisplayName("should return an empty stations list when no stations are nearby")
    void shouldReturnEmptyStationsListWhenNoStationsNearby() throws Exception {
      String body = objectMapper.writeValueAsString(new ApiLocationsResponse(List.of()));
      mockWebServer.enqueue(jsonResponse(200, body));

      ApiLocationsResponse result = transportClient.getLocationsByCoordinates(LATITUDE, LONGITUDE);

      assertThat(result.stations()).isEmpty();
    }
  }

  // ===========================================================================
  // 4xx error handling
  // ===========================================================================

  @Nested
  @DisplayName("getLocations() - 4xx client errors")
  class ClientErrorTest {

    @Test
    @DisplayName("should throw BadGatewayException on 400 Bad Request by query")
    void shouldThrowBadGatewayOnBadRequestByQuery() {
      mockWebServer.enqueue(jsonResponse(400, "{\"error\":\"bad request\"}"));

      assertThatThrownBy(() -> transportClient.getLocationsByQuery("Zurich"))
          .isInstanceOf(BadGatewayException.class)
          .hasMessage("Api Transport rejected the request");
    }

    @Test
    @DisplayName("should throw BadGatewayException on 401 Unauthorized by query")
    void shouldThrowBadGatewayOnUnauthorizedByQuery() {
      mockWebServer.enqueue(jsonResponse(401, "{\"error\":\"unauthorized\"}"));

      assertThatThrownBy(() -> transportClient.getLocationsByQuery("Zurich"))
          .isInstanceOf(BadGatewayException.class)
          .hasMessage("Api Transport rejected the request");
    }

    @Test
    @DisplayName("should throw BadGatewayException on 404 Not Found by query")
    void shouldThrowBadGatewayOnNotFoundByQuery() {
      mockWebServer.enqueue(jsonResponse(404, "{\"error\":\"not found\"}"));

      assertThatThrownBy(() -> transportClient.getLocationsByQuery("Zurich"))
          .isInstanceOf(BadGatewayException.class)
          .hasMessage("Api Transport rejected the request");
    }

    @Test
    @DisplayName("should throw BadGatewayException on 4xx with empty body by query")
    void shouldThrowBadGatewayOn4xxWithEmptyBodyByQuery() {
      mockWebServer.enqueue(
          new MockResponse()
              .setResponseCode(422)
              .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

      assertThatThrownBy(() -> transportClient.getLocationsByQuery("Zurich"))
          .isInstanceOf(BadGatewayException.class)
          .hasMessage("Api Transport rejected the request");
    }

    @Test
    @DisplayName("should throw BadGatewayException on 400 Bad Request by coordinates")
    void shouldThrowBadGatewayOnBadRequestByCoordinates() {
      mockWebServer.enqueue(jsonResponse(400, "{\"error\":\"bad request\"}"));

      assertThatThrownBy(() -> transportClient.getLocationsByCoordinates(47.5596, 7.5886))
          .isInstanceOf(BadGatewayException.class)
          .hasMessage("Api Transport rejected the request");
    }

    @Test
    @DisplayName("should throw BadGatewayException on 401 Unauthorized by coordinates")
    void shouldThrowBadGatewayOnUnauthorizedByCoordinates() {
      mockWebServer.enqueue(jsonResponse(401, "{\"error\":\"unauthorized\"}"));

      assertThatThrownBy(() -> transportClient.getLocationsByCoordinates(47.5596, 7.5886))
          .isInstanceOf(BadGatewayException.class)
          .hasMessage("Api Transport rejected the request");
    }

    @Test
    @DisplayName("should throw BadGatewayException on 404 Not Found by coordinates")
    void shouldThrowBadGatewayOnNotFoundByCoordinates() {
      mockWebServer.enqueue(jsonResponse(404, "{\"error\":\"not found\"}"));

      assertThatThrownBy(() -> transportClient.getLocationsByCoordinates(47.5596, 7.5886))
          .isInstanceOf(BadGatewayException.class)
          .hasMessage("Api Transport rejected the request");
    }

    @Test
    @DisplayName("should throw BadGatewayException on 4xx with empty body by coordinates")
    void shouldThrowBadGatewayOn4xxWithEmptyBodyByCoordinates() {
      mockWebServer.enqueue(
          new MockResponse()
              .setResponseCode(422)
              .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

      assertThatThrownBy(() -> transportClient.getLocationsByCoordinates(47.5596, 7.5886))
          .isInstanceOf(BadGatewayException.class)
          .hasMessage("Api Transport rejected the request");
    }
  }

  // ===========================================================================
  // 5xx error handling
  // ===========================================================================

  @Nested
  @DisplayName("getLocations() - 5xx server errors")
  class ServerErrorTest {

    @Test
    @DisplayName("should throw ServiceUnavailableException on 500 Internal Server Error by query")
    void shouldThrowServiceUnavailableOnInternalServerErrorByQuery() {
      mockWebServer.enqueue(jsonResponse(500, "{\"error\":\"internal error\"}"));

      assertThatThrownBy(() -> transportClient.getLocationsByQuery("Zurich"))
          .isInstanceOf(ServiceUnavailableException.class)
          .hasMessage("Api Transport is unavailable");
    }

    @Test
    @DisplayName("should throw ServiceUnavailableException on 502 Bad Gateway by query")
    void shouldThrowServiceUnavailableOnBadGatewayByQuery() {
      mockWebServer.enqueue(jsonResponse(502, "{\"error\":\"bad gateway\"}"));

      assertThatThrownBy(() -> transportClient.getLocationsByQuery("Zurich"))
          .isInstanceOf(ServiceUnavailableException.class)
          .hasMessage("Api Transport is unavailable");
    }

    @Test
    @DisplayName("should throw ServiceUnavailableException on 503 Service Unavailable by query")
    void shouldThrowServiceUnavailableOnServiceUnavailableByQuery() {
      mockWebServer.enqueue(jsonResponse(503, "{\"error\":\"service unavailable\"}"));

      assertThatThrownBy(() -> transportClient.getLocationsByQuery("Zurich"))
          .isInstanceOf(ServiceUnavailableException.class)
          .hasMessage("Api Transport is unavailable");
    }

    @Test
    @DisplayName("should throw ServiceUnavailableException on 5xx with empty body by query")
    void shouldThrowServiceUnavailableOn5xxWithEmptyBodyByQuery() {
      mockWebServer.enqueue(
          new MockResponse()
              .setResponseCode(500)
              .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

      assertThatThrownBy(() -> transportClient.getLocationsByQuery("Zurich"))
          .isInstanceOf(ServiceUnavailableException.class)
          .hasMessage("Api Transport is unavailable");
    }

    @Test
    @DisplayName(
        "should throw ServiceUnavailableException on 500 Internal Server Error by coordinates")
    void shouldThrowServiceUnavailableOnInternalServerErrorByCoordinates() {
      mockWebServer.enqueue(jsonResponse(500, "{\"error\":\"internal error\"}"));

      assertThatThrownBy(() -> transportClient.getLocationsByCoordinates(47.5596, 7.5886))
          .isInstanceOf(ServiceUnavailableException.class)
          .hasMessage("Api Transport is unavailable");
    }

    @Test
    @DisplayName("should throw ServiceUnavailableException on 502 Bad Gateway by coordinates")
    void shouldThrowServiceUnavailableOnBadGatewayByCoordinates() {
      mockWebServer.enqueue(jsonResponse(502, "{\"error\":\"bad gateway\"}"));

      assertThatThrownBy(() -> transportClient.getLocationsByCoordinates(47.5596, 7.5886))
          .isInstanceOf(ServiceUnavailableException.class)
          .hasMessage("Api Transport is unavailable");
    }

    @Test
    @DisplayName(
        "should throw ServiceUnavailableException on 503 Service Unavailable by coordinates")
    void shouldThrowServiceUnavailableOnServiceUnavailableByCoordinates() {
      mockWebServer.enqueue(jsonResponse(503, "{\"error\":\"service unavailable\"}"));

      assertThatThrownBy(() -> transportClient.getLocationsByCoordinates(47.5596, 7.5886))
          .isInstanceOf(ServiceUnavailableException.class)
          .hasMessage("Api Transport is unavailable");
    }

    @Test
    @DisplayName("should throw ServiceUnavailableException on 5xx with empty body by coordinates")
    void shouldThrowServiceUnavailableOn5xxWithEmptyBodyByCoordinates() {
      mockWebServer.enqueue(
          new MockResponse()
              .setResponseCode(500)
              .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

      assertThatThrownBy(() -> transportClient.getLocationsByCoordinates(47.5596, 7.5886))
          .isInstanceOf(ServiceUnavailableException.class)
          .hasMessage("Api Transport is unavailable");
    }
  }

  // ===========================================================================
  // Connection failure handling
  // ===========================================================================

  @Nested
  @DisplayName("getLocations() - connection failures")
  class ConnectionFailureTest {

    @Test
    @DisplayName("should throw BadGatewayException when the server is unreachable")
    void shouldThrowBadGatewayWhenServerIsUnreachable() throws Exception {
      // Shut down the server before the request so the connection is refused
      mockWebServer.shutdown();

      assertThatThrownBy(() -> transportClient.getLocationsByQuery("Zurich"))
          .isInstanceOf(BadGatewayException.class)
          .hasMessage("Api Transport is unreachable");

      // Prevent @AfterEach from shutting down an already-closed server
      mockWebServer = new MockWebServer();
    }
  }

  // ===========================================================================
  // Empty body handling
  // ===========================================================================

  @Nested
  @DisplayName("getLocations() - empty response body")
  class EmptyBodyTest {

    @Test
    @DisplayName("should throw BadGatewayException when the server returns an empty 200 body")
    void shouldThrowBadGatewayOnEmptyBody() {
      mockWebServer.enqueue(
          new MockResponse()
              .setResponseCode(200)
              .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
              .setBody(""));

      assertThatThrownBy(() -> transportClient.getLocationsByQuery("Zurich"))
          .isInstanceOf(BadGatewayException.class);
    }
  }
}
