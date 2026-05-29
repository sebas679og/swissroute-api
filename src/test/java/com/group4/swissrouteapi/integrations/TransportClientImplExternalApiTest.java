package com.group4.swissrouteapi.integrations;

import static org.assertj.core.api.Assertions.assertThat;

import com.group4.swissrouteapi.integrations.dto.responses.locations.ApiLocationsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Optional integration test for {@link TransportClientImpl} against the real Transport API.
 *
 * <p>This test is <strong>disabled by default</strong>. It only runs when the environment variable
 * {@code TRANSPORT_INTEGRATION_TESTS} is set to {@code "true"}, preventing accidental execution in
 * CI pipelines or offline environments.
 *
 * <p>To run locally:
 *
 * <pre>
 *   # Activate the Maven profile (recommended)
 *   mvn verify -P integration
 *
 *   # Or pass the system property directly
 *   mvn verify -Dtransport.integration.tests=true
 *
 *   # Run only the IT class
 *   mvn verify -P integration -Dit.test=TransportClientImplExternalApiTest
 *
 *   # IntelliJ IDEA
 *   Run > Edit Configurations > VM Options > -Dtransport.integration.tests=true
 * </pre>
 *
 * <p><strong>Note:</strong> these tests make real HTTP calls to the external API and depend on
 * network availability. Do not add them to the standard test suite.
 */
@DisplayName("TransportClientImpl — Integration")
@EnabledIfSystemProperty(
    named = "transport.integration.tests",
    matches = "true",
    disabledReason =
        "Run with -Ptransport-integration or -Dtransport.integration.tests=true to enable")
class TransportClientImplExternalApiTest {

  /** Base URL of the real Transport API. Adjust if your WebClient bean uses a different base. */
  private static final String BASE_URL = "https://transport.opendata.ch/v1";

  private TransportClientImpl transportClient;

  @BeforeEach
  void setUp() {
    WebClient webClient =
        WebClient.builder().baseUrl(BASE_URL).defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).build();


    transportClient = new TransportClientImpl(webClient);
  }

  @Test
  @DisplayName("should return 200 with stations when querying a known location")
  void shouldReturnStationsForKnownQuery() {
    ApiLocationsResponse response = transportClient.getLocationsByQuery("Zurich");

    assertThat(response).isNotNull();
    assertThat(response.stations()).isNotEmpty();
  }

  @Test
  @DisplayName("should return stations with non-blank id and name")
  void shouldReturnStationsWithIdAndName() {
    ApiLocationsResponse response = transportClient.getLocationsByQuery("Bern");

    assertThat(response.stations())
        .allSatisfy(
            station -> {
              assertThat(station.id()).isNotBlank();
              assertThat(station.name()).isNotBlank();
            });
  }

  @Test
  @DisplayName("should return stations with valid coordinates")
  void shouldReturnStationsWithValidCoordinates() {
    ApiLocationsResponse response = transportClient.getLocationsByQuery("Geneva");

    assertThat(response.stations())
        .filteredOn(s -> s.coordinate() != null)
        .allSatisfy(
            station -> {
              assertThat(station.coordinate().x()).isNotNull();
              assertThat(station.coordinate().y()).isNotNull();
            });
  }

  @Test
  @DisplayName("should return an empty stations list for a nonsense query")
  void shouldReturnEmptyListForNonsenseQuery() {
    ApiLocationsResponse response = transportClient.getLocationsByQuery("xqzwvpqzwvpqzwvp");

    assertThat(response).isNotNull();
    assertThat(response.stations()).isEmpty();
  }
}
