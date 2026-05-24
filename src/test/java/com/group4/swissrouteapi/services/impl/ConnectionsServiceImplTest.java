package com.group4.swissrouteapi.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.group4.swissrouteapi.dtos.requests.ConnectionsQueryParams;
import com.group4.swissrouteapi.dtos.responses.connections.Connection;
import com.group4.swissrouteapi.dtos.responses.connections.ConnectionsResponse;
import com.group4.swissrouteapi.dtos.responses.connections.Section;
import com.group4.swissrouteapi.exceptions.NotFoundException;
import com.group4.swissrouteapi.integrations.TransportClient;
import com.group4.swissrouteapi.integrations.dto.responses.connections.ApiConnection;
import com.group4.swissrouteapi.integrations.dto.responses.connections.ApiConnectionsResponse;
import com.group4.swissrouteapi.integrations.dto.responses.connections.ApiCoordinateConnection;
import com.group4.swissrouteapi.integrations.dto.responses.connections.ApiEndpoint;
import com.group4.swissrouteapi.integrations.dto.responses.connections.ApiJourney;
import com.group4.swissrouteapi.integrations.dto.responses.connections.ApiPrognosis;
import com.group4.swissrouteapi.integrations.dto.responses.connections.ApiSection;
import com.group4.swissrouteapi.integrations.dto.responses.connections.ApiStationConnection;
import com.group4.swissrouteapi.integrations.dto.responses.connections.ApiStations;
import com.group4.swissrouteapi.utils.enums.TransportationType;
import com.group4.swissrouteapi.utils.mappers.ConnectionsMapper;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link ConnectionsServiceImpl}.
 *
 * <p>Verifies delegation to {@link TransportClient}, empty/null-response guards, mapping delegation
 * to {@link ConnectionsMapper}, and correct passing of all optional query parameters. All
 * collaborators are mocked.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConnectionsServiceImpl")
class ConnectionsServiceImplTest {

  @Mock private TransportClient transportClient;
  @Mock private ConnectionsMapper connectionsMapper;

  @InjectMocks private ConnectionsServiceImpl connectionsService;

  // ---------------------------------------------------------------------------
  // Shared fixtures
  // ---------------------------------------------------------------------------

  private static final String FROM = "Basel";
  private static final String TO = "Bern";
  private static final LocalDate DATE = LocalDate.of(2024, 10, 10);
  private static final LocalTime TIME = LocalTime.of(8, 0);

  private ConnectionsQueryParams buildParams() {
    return ConnectionsQueryParams.builder().from(FROM).to(TO).build();
  }

  private ConnectionsQueryParams buildParamsWithOptionals() {
    return ConnectionsQueryParams.builder()
        .from(FROM)
        .to(TO)
        .date(DATE)
        .time(TIME)
        .transportations(List.of(TransportationType.TRAIN))
        .build();
  }

  private ApiStationConnection apiStation(String id, String name) {
    return new ApiStationConnection(
        id, name, 1.0, new ApiCoordinateConnection("Point", 7.58, 47.56), null);
  }

  private ApiEndpoint apiEndpoint(
      ApiStationConnection station,
      String platform,
      OffsetDateTime departure,
      OffsetDateTime arrival) {
    ApiPrognosis prognosis = new ApiPrognosis(null, null, null, null, null);
    return new ApiEndpoint(
        station, arrival, null, departure, null, 0, platform, prognosis, null, station);
  }

  private ApiConnection buildApiConnection() {
    ApiStationConnection fromStation = apiStation("8503000", "Basel SBB");
    ApiStationConnection toStation = apiStation("8507000", "Bern");

    OffsetDateTime dep = OffsetDateTime.parse("2024-10-10T08:00:00+02:00");
    OffsetDateTime arr = OffsetDateTime.parse("2024-10-10T08:57:00+02:00");

    ApiEndpoint departure = apiEndpoint(fromStation, "3", dep, arr);
    ApiEndpoint arrival = apiEndpoint(toStation, "7", dep, arr);

    ApiJourney journey =
        new ApiJourney(
            "IC 1", "IC", null, "1", "1", "SBB", "Bern", List.of(departure, arrival), 2, 3);

    ApiSection section = new ApiSection(journey, null, departure, arrival);

    return new ApiConnection(
        departure, arrival, "00d00:57:00", 0, null, List.of("IC"), 2, 3, List.of(section));
  }

  private ApiConnectionsResponse buildApiResponse(List<ApiConnection> connections) {
    ApiStationConnection from = apiStation("8503000", "Basel SBB");
    ApiStationConnection to = apiStation("8507000", "Bern");
    return new ApiConnectionsResponse(
        connections, from, to, new ApiStations(List.of(from), List.of(to)));
  }

  private Connection buildMappedConnection() {
    Section section =
        Section.builder()
            .category("IC")
            .number("1")
            .operator("SBB")
            .destination("Bern")
            .departureStation("Basel SBB")
            .departureTime(OffsetDateTime.parse("2024-10-10T08:00:00+02:00"))
            .arrivalStation("Bern")
            .arrivalTime(OffsetDateTime.parse("2024-10-10T08:57:00+02:00"))
            .platform("3")
            .build();

    return Connection.builder()
        .origin("Basel SBB")
        .destination("Bern")
        .duration("00d00:57:00")
        .products(List.of("IC"))
        .sections(List.of(section))
        .build();
  }

  // ===========================================================================
  // Successful response
  // ===========================================================================

  @Nested
  @DisplayName("getConnections() - successful response")
  class SuccessfulResponseTest {

    @Test
    @DisplayName("should return a mapped ConnectionsResponse")
    void shouldReturnMappedConnectionsResponse() {
      ApiConnection apiConn = buildApiConnection();
      Connection mapped = buildMappedConnection();

      when(transportClient.getConnections(FROM, TO, null, null, new ArrayList<>()))
          .thenReturn(buildApiResponse(List.of(apiConn)));
      when(connectionsMapper.toConnectionResponse(apiConn)).thenReturn(mapped);

      ConnectionsResponse result = connectionsService.getConnections(buildParams());

      assertThat(result.getConnections()).containsExactly(mapped);
    }

    @Test
    @DisplayName("should map every connection returned by the client")
    void shouldMapEveryConnection() {
      ApiConnection apiConn1 = buildApiConnection();
      ApiConnection apiConn2 = buildApiConnection();
      Connection mapped1 = buildMappedConnection();
      Connection mapped2 = buildMappedConnection();

      when(transportClient.getConnections(FROM, TO, null, null, new ArrayList<>()))
          .thenReturn(buildApiResponse(List.of(apiConn1, apiConn2)));
      when(connectionsMapper.toConnectionResponse(apiConn1)).thenReturn(mapped1);
      when(connectionsMapper.toConnectionResponse(apiConn2)).thenReturn(mapped2);

      ConnectionsResponse result = connectionsService.getConnections(buildParams());

      assertThat(result.getConnections()).containsExactly(mapped1, mapped2);
    }

    @Test
    @DisplayName("should delegate mapping to the connections mapper for each connection")
    void shouldDelegateMappingToMapper() {
      ApiConnection apiConn = buildApiConnection();

      when(transportClient.getConnections(FROM, TO, null, null, new ArrayList<>()))
          .thenReturn(buildApiResponse(List.of(apiConn)));
      when(connectionsMapper.toConnectionResponse(apiConn)).thenReturn(buildMappedConnection());

      connectionsService.getConnections(buildParams());

      verify(connectionsMapper).toConnectionResponse(apiConn);
    }

    @Test
    @DisplayName("should pass all optional params to the transport client")
    void shouldPassAllOptionalParamsToClient() {
      ApiConnection apiConn = buildApiConnection();

      when(transportClient.getConnections(FROM, TO, DATE, TIME, List.of(TransportationType.TRAIN)))
          .thenReturn(buildApiResponse(List.of(apiConn)));
      when(connectionsMapper.toConnectionResponse(apiConn)).thenReturn(buildMappedConnection());

      connectionsService.getConnections(buildParamsWithOptionals());

      verify(transportClient)
          .getConnections(FROM, TO, DATE, TIME, List.of(TransportationType.TRAIN));
    }

    @Test
    @DisplayName("should pass null date and time when not provided")
    void shouldPassNullDateAndTimeWhenNotProvided() {
      ApiConnection apiConn = buildApiConnection();

      when(transportClient.getConnections(FROM, TO, null, null, new ArrayList<>()))
          .thenReturn(buildApiResponse(List.of(apiConn)));
      when(connectionsMapper.toConnectionResponse(apiConn)).thenReturn(buildMappedConnection());

      connectionsService.getConnections(buildParams());

      verify(transportClient).getConnections(FROM, TO, null, null, new ArrayList<>());
    }
  }

  // ===========================================================================
  // Empty / null response guard
  // ===========================================================================

  @Nested
  @DisplayName("getConnections() - empty or null response")
  class EmptyResponseGuardTest {

    @Test
    @DisplayName("should throw NotFoundException when the API returns a null response")
    void shouldThrowNotFoundWhenApiReturnsNull() {
      when(transportClient.getConnections(FROM, TO, null, null, new ArrayList<>()))
          .thenReturn(null);

      assertThatThrownBy(() -> connectionsService.getConnections(buildParams()))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("No connections found for the given parameters");
    }

    @Test
    @DisplayName("should throw NotFoundException when the API returns a null connections list")
    void shouldThrowNotFoundWhenConnectionsListIsNull() {
      when(transportClient.getConnections(FROM, TO, null, null, new ArrayList<>()))
          .thenReturn(buildApiResponse(null));

      assertThatThrownBy(() -> connectionsService.getConnections(buildParams()))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("No connections found for the given parameters");
    }

    @Test
    @DisplayName("should throw NotFoundException when the API returns an empty connections list")
    void shouldThrowNotFoundWhenConnectionsListIsEmpty() {
      when(transportClient.getConnections(FROM, TO, null, null, new ArrayList<>()))
          .thenReturn(buildApiResponse(Collections.emptyList()));

      assertThatThrownBy(() -> connectionsService.getConnections(buildParams()))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("No connections found for the given parameters");
    }

    @Test
    @DisplayName("should not call the mapper when no connections are found")
    void shouldNotCallMapperWhenNoConnectionsFound() {
      when(transportClient.getConnections(FROM, TO, null, null, new ArrayList<>()))
          .thenReturn(buildApiResponse(Collections.emptyList()));

      assertThatThrownBy(() -> connectionsService.getConnections(buildParams()))
          .isInstanceOf(NotFoundException.class);

      verify(connectionsMapper, never()).toConnectionResponse(org.mockito.ArgumentMatchers.any());
    }
  }
}
