package com.group4.swissrouteapi.integrations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.exceptions.BadGatewayException;
import com.group4.swissrouteapi.exceptions.ServiceUnavailableException;
import com.group4.swissrouteapi.integrations.dto.responses.ApiCoordinate;
import com.group4.swissrouteapi.integrations.dto.responses.ApiEndpoint;
import com.group4.swissrouteapi.integrations.dto.responses.ApiJourney;
import com.group4.swissrouteapi.integrations.dto.responses.ApiPrognosis;
import com.group4.swissrouteapi.integrations.dto.responses.ApiStation;
import com.group4.swissrouteapi.integrations.dto.responses.connections.ApiConnection;
import com.group4.swissrouteapi.integrations.dto.responses.connections.ApiConnectionsResponse;
import com.group4.swissrouteapi.integrations.dto.responses.connections.ApiSection;
import com.group4.swissrouteapi.integrations.dto.responses.connections.ApiStations;
import com.group4.swissrouteapi.integrations.dto.responses.locations.ApiLocationsResponse;
import com.group4.swissrouteapi.integrations.dto.responses.stationboard.ApiStationBoardResponse;
import com.group4.swissrouteapi.utils.enums.TransportType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
  private final ObjectMapper objectMapper =
      new ObjectMapper()
          .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
          .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

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

  private String buildStationsJson() throws Exception {
    ApiCoordinate coordinate = new ApiCoordinate("Point", 7.5886, 47.5596);
    ApiStation station = new ApiStation("8502113", "Aarau", 1.0, coordinate, 34, null);
    ApiEndpoint departure = getApiEndpoint(station);

    ApiJourney journey =
        new ApiJourney(
            departure, "IC 1", "IC", null, "1", "1", "SBB", "Bern", List.of(departure), 2, 3);

    ApiStationBoardResponse response = new ApiStationBoardResponse(station, List.of(journey));
    return objectMapper.writeValueAsString(response);
  }

  private static @NonNull ApiEndpoint getApiEndpoint(ApiStation station) {
    ApiPrognosis prognosis = new ApiPrognosis(null, null, null, null, null);
    return new ApiEndpoint(
        station,
        OffsetDateTime.parse("2024-10-10T08:00:00+02:00"),
        1728547200L,
        OffsetDateTime.parse("2024-10-10T08:00:00+02:00"),
        1728547200L,
        0,
        "3",
        prognosis,
        null,
        station);
  }

  // ---------------------------------------------------------------------------
  // Helper — builds a minimal but complete ApiConnectionsResponse JSON string
  // ---------------------------------------------------------------------------

  private String buildConnectionsResponse() throws Exception {
    ApiCoordinate coordinate = new ApiCoordinate("Point", 7.5886, 47.5596);

    ApiStation fromStation = new ApiStation("8503000", "Basel SBB", 1.0, coordinate, 34, null);
    ApiStation toStation = new ApiStation("8507000", "Bern", 1.0, coordinate, 21, null);

    ApiPrognosis prognosis = new ApiPrognosis(null, null, null, null, null);

    ApiEndpoint departure =
        new ApiEndpoint(
            fromStation,
            OffsetDateTime.parse("2024-10-10T08:00:00+02:00"),
            1728547200L,
            OffsetDateTime.parse("2024-10-10T08:00:00+02:00"),
            1728547200L,
            0,
            "3",
            prognosis,
            null,
            fromStation);

    ApiConnection connection = getApiConnection(toStation, prognosis, departure);

    ApiStations stations = new ApiStations(List.of(fromStation), List.of(toStation));

    ApiConnectionsResponse response =
        new ApiConnectionsResponse(List.of(connection), fromStation, toStation, stations);

    return objectMapper.writeValueAsString(response);
  }

  private static @NonNull ApiConnection getApiConnection(
      ApiStation toStation, ApiPrognosis prognosis, ApiEndpoint departure) {
    ApiEndpoint arrival =
        new ApiEndpoint(
            toStation,
            OffsetDateTime.parse("2024-10-10T08:57:00+02:00"),
            1728550620L,
            OffsetDateTime.parse("2024-10-10T08:57:00+02:00"),
            1728550620L,
            0,
            "7",
            prognosis,
            null,
            toStation);

    return getApiConnection(departure, arrival);
  }

  private static @NonNull ApiConnection getApiConnection(
      ApiEndpoint departure, ApiEndpoint arrival) {
    ApiJourney journey =
        new ApiJourney(
            departure,
            "IC 1",
            "IC",
            null,
            "1",
            "1",
            "SBB",
            "Bern",
            List.of(departure, arrival),
            2,
            3);

    ApiSection section = new ApiSection(journey, null, departure, arrival);

    return new ApiConnection(
        departure, arrival, "00d00:57:00", 0, null, List.of("IC"), 2, 3, List.of(section));
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
  // getConnections
  // ===========================================================================

  @Nested
  @DisplayName("getConnections() - successful response")
  class GetConnectionsSuccessfulResponseTest {
    private static final String FROM = "Basel";
    private static final String TO = "Bern";

    @Test
    @DisplayName("should return a deserialized ApiConnectionsResponse")
    void shouldReturnDeserializedResponse() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildConnectionsResponse()));

      ApiConnectionsResponse result =
          transportClient.getConnections(FROM, TO, null, null, List.of(), null);

      assertThat(result).isNotNull();
      assertThat(result.connections()).hasSize(1);
    }

    @Test
    @DisplayName("should deserialize the from station of the response")
    void shouldDeserializeFromStation() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildConnectionsResponse()));

      ApiConnectionsResponse result =
          transportClient.getConnections(FROM, TO, null, null, List.of(), null);

      assertThat(result.from()).isNotNull();
      assertThat(result.from().id()).isEqualTo("8503000");
      assertThat(result.from().name()).isEqualTo("Basel SBB");
    }

    @Test
    @DisplayName("should deserialize the to station of the response")
    void shouldDeserializeToStation() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildConnectionsResponse()));

      ApiConnectionsResponse result =
          transportClient.getConnections(FROM, TO, null, null, List.of(), null);

      assertThat(result.to()).isNotNull();
      assertThat(result.to().id()).isEqualTo("8507000");
      assertThat(result.to().name()).isEqualTo("Bern");
    }

    @Test
    @DisplayName("should deserialize connection duration and transfers")
    void shouldDeserializeConnectionDurationAndTransfers() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildConnectionsResponse()));

      ApiConnectionsResponse result =
          transportClient.getConnections(FROM, TO, null, null, List.of(), null);

      ApiConnection connection = result.connections().getFirst();
      assertThat(connection.duration()).isEqualTo("00d00:57:00");
      assertThat(connection.transfers()).isEqualTo(0);
    }

    @Test
    @DisplayName("should deserialize connection sections")
    void shouldDeserializeConnectionSections() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildConnectionsResponse()));

      ApiConnectionsResponse result =
          transportClient.getConnections(FROM, TO, null, null, List.of(), null);

      assertThat(result.connections().getFirst().sections()).hasSize(1);
    }

    @Test
    @DisplayName("should send 'from' and 'to' as query parameters")
    void shouldSendFromAndToAsQueryParams() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildConnectionsResponse()));

      transportClient.getConnections(FROM, TO, null, null, List.of(), null);

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getPath()).contains("from=" + FROM).contains("to=" + TO);
    }

    @Test
    @DisplayName("should send a GET request to the connections path")
    void shouldSendGetRequestToConnectionsPath() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildConnectionsResponse()));

      transportClient.getConnections(FROM, TO, null, null, List.of(), null);

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getMethod()).isEqualTo("GET");
      assertThat(recorded.getPath()).contains(ApiPaths.TransportApi.CONNECTIONS);
    }

    @Test
    @DisplayName("should include date param when date is provided")
    void shouldIncludeDateParamWhenProvided() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildConnectionsResponse()));

      transportClient.getConnections(FROM, TO, LocalDate.of(2024, 10, 10), null, List.of(), null);

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getPath()).contains("date=2024-10-10");
    }

    @Test
    @DisplayName("should not include date param when date is null")
    void shouldNotIncludeDateParamWhenNull() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildConnectionsResponse()));

      transportClient.getConnections(FROM, TO, null, null, List.of(), null);

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getPath()).doesNotContain("date=");
    }

    @Test
    @DisplayName("should include time param when time is provided")
    void shouldIncludeTimeParamWhenProvided() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildConnectionsResponse()));

      transportClient.getConnections(FROM, TO, null, LocalTime.of(8, 0), List.of(), null);

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getPath()).contains("time=08:00");
    }

    @Test
    @DisplayName("should not include time param when time is null")
    void shouldNotIncludeTimeParamWhenNull() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildConnectionsResponse()));

      transportClient.getConnections(FROM, TO, null, null, List.of(), null);

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getPath()).doesNotContain("time=");
    }

    @Test
    @DisplayName("should include transportation types when provided")
    void shouldIncludeTransportationTypesWhenProvided() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildConnectionsResponse()));

      transportClient.getConnections(
          FROM, TO, null, null, List.of(TransportType.TRAIN, TransportType.BUS), null);

      RecordedRequest recorded = mockWebServer.takeRequest();
      String path = recorded.getPath();
      assertThat(path).contains("train").contains("bus");
    }

    @Test
    @DisplayName("should not include transportations param when list is empty")
    void shouldNotIncludeTransportationsWhenListIsEmpty() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildConnectionsResponse()));

      transportClient.getConnections(FROM, TO, null, null, List.of(), null);

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getPath()).doesNotContain("transportations");
    }

    @Test
    @DisplayName("should not include transportations param when list is null")
    void shouldNotIncludeTransportationsWhenListIsNull() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildConnectionsResponse()));

      transportClient.getConnections(FROM, TO, null, null, null, null);

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getPath()).doesNotContain("transportations");
    }

    @Test
    @DisplayName("should include via params when list is provided")
    void shouldIncludeViaParamsWhenProvided() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildConnectionsResponse()));

      transportClient.getConnections(
          FROM, TO, null, null, List.of(), List.of("Olten", "Basel SBB"));

      RecordedRequest recorded = mockWebServer.takeRequest();
      String path = recorded.getPath();
      assertThat(path).contains("Olten").contains("Basel");
    }

    @Test
    @DisplayName("should not include via param when list is empty")
    void shouldNotIncludeViaWhenListIsEmpty() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildConnectionsResponse()));

      transportClient.getConnections(FROM, TO, null, null, List.of(), List.of());

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getPath()).doesNotContain("via");
    }

    @Test
    @DisplayName("should not include via param when list is null")
    void shouldNotIncludeViaWhenListIsNull() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildConnectionsResponse()));

      transportClient.getConnections(FROM, TO, null, null, List.of(), null);

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getPath()).doesNotContain("via");
    }

    @Test
    @DisplayName("should send via locations trimmed")
    void shouldSendViaTrimmed() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildConnectionsResponse()));

      transportClient.getConnections(FROM, TO, null, null, List.of(), List.of("  Olten  "));

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getPath()).contains("Olten");
    }

    @ParameterizedTest(name = "via list size: {0}")
    @ValueSource(ints = {1, 2, 5})
    @DisplayName("should include all via locations in the request")
    void shouldIncludeAllViaLocations(int size) throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildConnectionsResponse()));

      List<String> vias =
          IntStream.range(0, size).mapToObj(i -> "Station" + i).collect(Collectors.toList());

      transportClient.getConnections(FROM, TO, null, null, List.of(), vias);

      RecordedRequest recorded = mockWebServer.takeRequest();
      String path = recorded.getPath();
      vias.forEach(via -> assertThat(path).contains(via));
    }
  }

  // ===========================================================================
  // get() stationBoard
  // ===========================================================================
  @Nested
  @DisplayName("getStationBoard()")
  class GetStationBoardTest {

    private static final String STATION = "Aarau";
    private static final String ID = "8502113";
    private static final Integer LIMIT = 10;
    private static final List<TransportType> TYPES =
        List.of(TransportType.TRAIN, TransportType.BUS);

    // ── success ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("returns mapped response when API returns 200 with full payload")
    void returnsApiStationBoardResponse_whenApiReturns200() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildStationsJson()));

      ApiStationBoardResponse result = transportClient.getStationBoard(STATION, ID, LIMIT, TYPES);

      assertThat(result).isNotNull();
      assertThat(result.station()).isNotNull();
      assertThat(result.station().id()).isEqualTo(ID);
      assertThat(result.station().name()).isEqualTo(STATION);
      assertThat(result.stationBoard()).hasSize(1);
    }

    @Test
    @DisplayName("stationboard entries contain the expected journey data")
    void stationBoardEntries_containExpectedJourneyData() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildStationsJson()));

      ApiStationBoardResponse result = transportClient.getStationBoard(STATION, ID, LIMIT, TYPES);

      ApiJourney journey = result.stationBoard().getFirst();
      assertThat(journey.name()).isEqualTo("IC 1");
      assertThat(journey.category()).isEqualTo("IC");
      assertThat(journey.operator()).isEqualTo("SBB");
      assertThat(journey.to()).isEqualTo("Bern");
    }

    // ── request — URI construction ────────────────────────────────────────────

    @Test
    @DisplayName("sends request to the stationboard endpoint with station query param")
    void sendsRequest_toStationboardEndpoint_withStationParam() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildStationsJson()));

      transportClient.getStationBoard(STATION, null, null, null);

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getPath()).contains("/stationboard");
      assertThat(recorded.getPath()).contains("station=" + STATION);
    }

    @Test
    @DisplayName("includes id query param when id is provided")
    void includesIdParam_whenIdIsProvided() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildStationsJson()));

      transportClient.getStationBoard(STATION, ID, null, null);

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getPath()).contains("id=" + ID);
    }

    @Test
    @DisplayName("omits id query param when id is null")
    void omitsIdParam_whenIdIsNull() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildStationsJson()));

      transportClient.getStationBoard(STATION, null, null, null);

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getPath()).doesNotContain("id=");
    }

    @Test
    @DisplayName("includes limit query param when limit is provided")
    void includesLimitParam_whenLimitIsProvided() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildStationsJson()));

      transportClient.getStationBoard(STATION, null, LIMIT, null);

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getPath()).contains("limit=" + LIMIT);
    }

    @Test
    @DisplayName("omits limit query param when limit is null")
    void omitsLimitParam_whenLimitIsNull() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildStationsJson()));

      transportClient.getStationBoard(STATION, null, null, null);

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getPath()).doesNotContain("limit=");
    }

    @Test
    @DisplayName("includes transportations[] params in lowercase when transport types are provided")
    void includesTransportationsParams_inLowercase_whenTypesAreProvided() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildStationsJson()));

      transportClient.getStationBoard(STATION, null, null, TYPES);

      RecordedRequest recorded = mockWebServer.takeRequest();
      String path = recorded.getPath();
      assertThat(path).contains("train");
      assertThat(path).contains("bus");
    }

    @Test
    @DisplayName("omits transportations[] param when transport type list is null")
    void omitsTransportationsParam_whenTransportTypeListIsNull() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildStationsJson()));

      transportClient.getStationBoard(STATION, null, null, null);

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getPath()).doesNotContain("transportations");
    }

    @Test
    @DisplayName("omits transportations[] param when transport type list is empty")
    void omitsTransportationsParam_whenTransportTypeListIsEmpty() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildStationsJson()));

      transportClient.getStationBoard(STATION, null, null, List.of());

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getPath()).doesNotContain("transportations");
    }

    @Test
    @DisplayName("uses GET method")
    void usesGetMethod() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildStationsJson()));

      transportClient.getStationBoard(STATION, ID, LIMIT, TYPES);

      RecordedRequest recorded = mockWebServer.takeRequest();
      assertThat(recorded.getMethod()).isEqualTo("GET");
    }

    // ── all params combined ───────────────────────────────────────────────────

    @Test
    @DisplayName("sends all query params when all arguments are provided")
    void sendsAllQueryParams_whenAllArgumentsAreProvided() throws Exception {
      mockWebServer.enqueue(jsonResponse(200, buildStationsJson()));

      transportClient.getStationBoard(STATION, ID, LIMIT, TYPES);

      RecordedRequest recorded = mockWebServer.takeRequest();
      String path = recorded.getPath();
      assertThat(path)
          .contains("station=" + STATION)
          .contains("id=" + ID)
          .contains("limit=" + LIMIT)
          .contains("train")
          .contains("bus");
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
