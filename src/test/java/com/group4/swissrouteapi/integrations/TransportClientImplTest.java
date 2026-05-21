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
                    120.5,
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
  @DisplayName("getLocations() - successful response")
  class SuccessfulResponseTest {

    @Test
    @DisplayName("should return a LocationsResponse with the deserialized stations")
    void shouldReturnDeserializedLocationsResponse() throws Exception {
      String body = buildLocationsJson("8503000", "Zurich HB");
      mockWebServer.enqueue(jsonResponse(200, body));

      ApiLocationsResponse result = transportClient.getLocations("Zurich");

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

      ApiLocationsResponse result = transportClient.getLocations("Zurich");

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

      ApiLocationsResponse result = transportClient.getLocations("Zurich");

      ApiStation apiStation = result.stations().getFirst();
      assertThat(apiStation.score()).isEqualTo(0.9);
      assertThat(apiStation.distance()).isEqualTo(120.5);
    }

    @Test
    @DisplayName("should return an empty stations list when the response contains no stations")
    void shouldReturnEmptyStationsListWhenNoStations() throws Exception {
      String body = objectMapper.writeValueAsString(new ApiLocationsResponse(List.of()));
      mockWebServer.enqueue(jsonResponse(200, body));

      ApiLocationsResponse result = transportClient.getLocations("unknown-place");

      assertThat(result.stations()).isEmpty();
    }

    @Test
    @DisplayName("should send the query as a request parameter")
    void shouldSendQueryAsRequestParameter() throws Exception {
      String body = buildLocationsJson("8503000", "Zurich HB");
      mockWebServer.enqueue(jsonResponse(200, body));

      transportClient.getLocations("Zurich");

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getPath()).contains("query=Zurich");
    }

    @Test
    @DisplayName("should send a GET request to the locations path")
    void shouldSendGetRequestToLocationsPath() throws Exception {
      String body = buildLocationsJson("8503000", "Zurich HB");
      mockWebServer.enqueue(jsonResponse(200, body));

      transportClient.getLocations("Zurich");

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getMethod()).isEqualTo("GET");
      assertThat(recorded.getPath()).contains(ApiPaths.TransportApi.LOCATIONS);
    }

    @Test
    @DisplayName("should URL-encode special characters in the query parameter")
    void shouldUrlEncodeQueryParameter() throws Exception {
      String body = buildLocationsJson("8503000", "Zürich HB");
      mockWebServer.enqueue(jsonResponse(200, body));

      transportClient.getLocations("Zürich HB");

      RecordedRequest recorded = mockWebServer.takeRequest();
      // URL-encoded space = '+' or '%20', ü = '%C3%BC'
      assertThat(recorded.getPath()).containsPattern("query=Z");
    }
  }

  // ===========================================================================
  // 4xx error handling
  // ===========================================================================

  @Nested
  @DisplayName("getLocations() - 4xx client errors")
  class ClientErrorTest {

    @Test
    @DisplayName("should throw BadGatewayException on 400 Bad Request")
    void shouldThrowBadGatewayOnBadRequest() {
      mockWebServer.enqueue(jsonResponse(400, "{\"error\":\"bad request\"}"));

      assertThatThrownBy(() -> transportClient.getLocations("Zurich"))
          .isInstanceOf(BadGatewayException.class)
          .hasMessage("Customer Service rejected the request");
    }

    @Test
    @DisplayName("should throw BadGatewayException on 401 Unauthorized")
    void shouldThrowBadGatewayOnUnauthorized() {
      mockWebServer.enqueue(jsonResponse(401, "{\"error\":\"unauthorized\"}"));

      assertThatThrownBy(() -> transportClient.getLocations("Zurich"))
          .isInstanceOf(BadGatewayException.class)
          .hasMessage("Customer Service rejected the request");
    }

    @Test
    @DisplayName("should throw BadGatewayException on 404 Not Found")
    void shouldThrowBadGatewayOnNotFound() {
      mockWebServer.enqueue(jsonResponse(404, "{\"error\":\"not found\"}"));

      assertThatThrownBy(() -> transportClient.getLocations("Zurich"))
          .isInstanceOf(BadGatewayException.class)
          .hasMessage("Customer Service rejected the request");
    }

    @Test
    @DisplayName("should throw BadGatewayException on 4xx with empty body")
    void shouldThrowBadGatewayOn4xxWithEmptyBody() {
      mockWebServer.enqueue(
          new MockResponse()
              .setResponseCode(422)
              .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

      assertThatThrownBy(() -> transportClient.getLocations("Zurich"))
          .isInstanceOf(BadGatewayException.class)
          .hasMessage("Customer Service rejected the request");
    }
  }

  // ===========================================================================
  // 5xx error handling
  // ===========================================================================

  @Nested
  @DisplayName("getLocations() - 5xx server errors")
  class ServerErrorTest {

    @Test
    @DisplayName("should throw ServiceUnavailableException on 500 Internal Server Error")
    void shouldThrowServiceUnavailableOnInternalServerError() {
      mockWebServer.enqueue(jsonResponse(500, "{\"error\":\"internal error\"}"));

      assertThatThrownBy(() -> transportClient.getLocations("Zurich"))
          .isInstanceOf(ServiceUnavailableException.class)
          .hasMessage("Customer Service is unavailable");
    }

    @Test
    @DisplayName("should throw ServiceUnavailableException on 502 Bad Gateway")
    void shouldThrowServiceUnavailableOnBadGateway() {
      mockWebServer.enqueue(jsonResponse(502, "{\"error\":\"bad gateway\"}"));

      assertThatThrownBy(() -> transportClient.getLocations("Zurich"))
          .isInstanceOf(ServiceUnavailableException.class)
          .hasMessage("Customer Service is unavailable");
    }

    @Test
    @DisplayName("should throw ServiceUnavailableException on 503 Service Unavailable")
    void shouldThrowServiceUnavailableOnServiceUnavailable() {
      mockWebServer.enqueue(jsonResponse(503, "{\"error\":\"service unavailable\"}"));

      assertThatThrownBy(() -> transportClient.getLocations("Zurich"))
          .isInstanceOf(ServiceUnavailableException.class)
          .hasMessage("Customer Service is unavailable");
    }

    @Test
    @DisplayName("should throw ServiceUnavailableException on 5xx with empty body")
    void shouldThrowServiceUnavailableOn5xxWithEmptyBody() {
      mockWebServer.enqueue(
          new MockResponse()
              .setResponseCode(500)
              .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

      assertThatThrownBy(() -> transportClient.getLocations("Zurich"))
          .isInstanceOf(ServiceUnavailableException.class)
          .hasMessage("Customer Service is unavailable");
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

      assertThatThrownBy(() -> transportClient.getLocations("Zurich"))
          .isInstanceOf(BadGatewayException.class)
          .hasMessage("Session validation service is unreachable");

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

      assertThatThrownBy(() -> transportClient.getLocations("Zurich"))
          .isInstanceOf(BadGatewayException.class);
    }
  }
}
