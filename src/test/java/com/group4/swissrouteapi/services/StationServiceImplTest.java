package com.group4.swissrouteapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.group4.swissrouteapi.dtos.requests.StationsQueryParams;
import com.group4.swissrouteapi.dtos.responses.Station;
import com.group4.swissrouteapi.dtos.responses.StationsResponse;
import com.group4.swissrouteapi.exceptions.NotFoundException;
import com.group4.swissrouteapi.integrations.TransportClient;
import com.group4.swissrouteapi.integrations.dto.responses.locations.ApiCoordinate;
import com.group4.swissrouteapi.integrations.dto.responses.locations.ApiLocationsResponse;
import com.group4.swissrouteapi.integrations.dto.responses.locations.ApiStation;
import com.group4.swissrouteapi.utils.mappers.StationMapper;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("StationServiceImpl Unit Tests")
class StationServiceImplTest {

  @Mock private TransportClient transportClient;

  @Mock private StationMapper stationMapper;

  @InjectMocks private StationServiceImpl stationService;

  // -------------------------------------------------------------------------
  // Shared test fixtures
  // -------------------------------------------------------------------------

  private static final String QUERY = "Central";

  private StationsQueryParams queryParams;

  @BeforeEach
  void setUp() {
    queryParams = StationsQueryParams.builder().query(QUERY).build();
  }

  // =========================================================================
  // getStationsByName — happy path
  // =========================================================================

  @Nested
  @DisplayName("Given valid stations are returned by the transport client")
  class WhenStationsExist {

    @Test
    @DisplayName("should return a StationsResponse with the mapped stations")
    void shouldReturnMappedStations() {
      // Arrange
      ApiStation apiStation = buildApiStation("1", "Central Station", 40.7128, -74.0060);
      ApiLocationsResponse apiResponse = new ApiLocationsResponse(List.of(apiStation));

      Station mappedStation =
          Station.builder()
              .id("1")
              .name("Central Station")
              .latitude(40.7128)
              .longitude(-74.0060)
              .build();

      when(transportClient.getLocationsByQuery(QUERY)).thenReturn(apiResponse);
      when(stationMapper.toStations(apiStation)).thenReturn(mappedStation);

      // Act
      StationsResponse result = stationService.getStationsByName(queryParams);

      // Assert
      assertThat(result).isNotNull();
      assertThat(result.getStations()).hasSize(1).containsExactly(mappedStation);

      verify(transportClient).getLocationsByQuery(QUERY);
      verify(stationMapper).toStations(apiStation);
    }

    @Test
    @DisplayName("should map every station returned by the API")
    void shouldMapAllStations() {
      // Arrange
      ApiStation first = buildApiStation("1", "Station A", 10.0, 20.0);
      ApiStation second = buildApiStation("2", "Station B", 30.0, 40.0);
      ApiStation third = buildApiStation("3", "Station C", 50.0, 60.0);

      ApiLocationsResponse apiResponse = new ApiLocationsResponse(List.of(first, second, third));

      Station stationA = buildStation("1", "Station A", 10.0, 20.0);
      Station stationB = buildStation("2", "Station B", 30.0, 40.0);
      Station stationC = buildStation("3", "Station C", 50.0, 60.0);

      when(transportClient.getLocationsByQuery(QUERY)).thenReturn(apiResponse);
      when(stationMapper.toStations(first)).thenReturn(stationA);
      when(stationMapper.toStations(second)).thenReturn(stationB);
      when(stationMapper.toStations(third)).thenReturn(stationC);

      // Act
      StationsResponse result = stationService.getStationsByName(queryParams);

      // Assert
      assertThat(result.getStations()).hasSize(3).containsExactly(stationA, stationB, stationC);

      verify(stationMapper, times(3)).toStations(any(ApiStation.class));
    }

    @Test
    @DisplayName("should preserve the order of stations as returned by the API")
    void shouldPreserveStationOrder() {
      // Arrange
      ApiStation first = buildApiStation("10", "Alpha", 1.0, 1.0);
      ApiStation second = buildApiStation("20", "Beta", 2.0, 2.0);

      when(transportClient.getLocationsByQuery(QUERY))
          .thenReturn(new ApiLocationsResponse(List.of(first, second)));

      Station alpha = buildStation("10", "Alpha", 1.0, 1.0);
      Station beta = buildStation("20", "Beta", 2.0, 2.0);

      when(stationMapper.toStations(first)).thenReturn(alpha);
      when(stationMapper.toStations(second)).thenReturn(beta);

      // Act
      StationsResponse result = stationService.getStationsByName(queryParams);

      // Assert
      assertThat(result.getStations()).containsExactly(alpha, beta);
    }

    @Test
    @DisplayName("should delegate the query string to the transport client unchanged")
    void shouldForwardQueryToTransportClient() {
      // Arrange
      String specificQuery = "Gare du Nord";
      StationsQueryParams params = StationsQueryParams.builder().query(specificQuery).build();

      ApiStation apiStation = buildApiStation("99", "Gare du Nord", 48.88, 2.35);
      when(transportClient.getLocationsByQuery(specificQuery))
          .thenReturn(new ApiLocationsResponse(List.of(apiStation)));
      when(stationMapper.toStations(apiStation))
          .thenReturn(buildStation("99", "Gare du Nord", 48.88, 2.35));

      // Act
      stationService.getStationsByName(params);

      // Assert
      verify(transportClient).getLocationsByQuery(specificQuery);
      verify(transportClient, never()).getLocationsByQuery(argThat(q -> !q.equals(specificQuery)));
    }
  }

  // =========================================================================
  // getStationsByName — NotFoundException scenarios
  // =========================================================================

  @Nested
  @DisplayName("Given no stations are returned by the transport client")
  class WhenStationsAreAbsent {

    @Test
    @DisplayName("should throw NotFoundException when the station list is null")
    void shouldThrowNotFoundExceptionWhenStationListIsNull() {
      // Arrange
      when(transportClient.getLocationsByQuery(QUERY)).thenReturn(new ApiLocationsResponse(null));

      // Act & Assert
      assertThatThrownBy(() -> stationService.getStationsByName(queryParams))
          .isInstanceOf(NotFoundException.class)
          .hasMessageContaining(QUERY);

      verifyNoInteractions(stationMapper);
    }

    @Test
    @DisplayName("should throw NotFoundException when the station list is empty")
    void shouldThrowNotFoundExceptionWhenStationListIsEmpty() {
      // Arrange
      when(transportClient.getLocationsByQuery(QUERY))
          .thenReturn(new ApiLocationsResponse(Collections.emptyList()));

      // Act & Assert
      assertThatThrownBy(() -> stationService.getStationsByName(queryParams))
          .isInstanceOf(NotFoundException.class)
          .hasMessageContaining(QUERY);

      verifyNoInteractions(stationMapper);
    }

    @Test
    @DisplayName("should include the query name in the NotFoundException message")
    void shouldIncludeQueryInExceptionMessage() {
      // Arrange
      String query = "UnknownPlace";
      StationsQueryParams params = StationsQueryParams.builder().query(query).build();

      when(transportClient.getLocationsByQuery(query))
          .thenReturn(new ApiLocationsResponse(Collections.emptyList()));

      // Act & Assert
      assertThatThrownBy(() -> stationService.getStationsByName(params))
          .isInstanceOf(NotFoundException.class)
          .hasMessageContaining("No stations found with the name: " + query);
    }

    @Test
    @DisplayName("should never invoke the mapper when stations are absent")
    void shouldNotInvokeMapperWhenNoStationsFound() {
      // Arrange
      when(transportClient.getLocationsByQuery(anyString()))
          .thenReturn(new ApiLocationsResponse(Collections.emptyList()));

      // Act & Assert
      assertThatThrownBy(() -> stationService.getStationsByName(queryParams))
          .isInstanceOf(NotFoundException.class);

      verifyNoInteractions(stationMapper);
    }
  }

  // =========================================================================
  // getStationsByName — interaction verifications
  // =========================================================================

  @Nested
  @DisplayName("Interaction verifications")
  class InteractionVerifications {

    @Test
    @DisplayName("should call the transport client exactly once per invocation")
    void shouldCallTransportClientExactlyOnce() {
      // Arrange
      ApiStation apiStation = buildApiStation("1", "Main St", 0.0, 0.0);
      when(transportClient.getLocationsByQuery(QUERY))
          .thenReturn(new ApiLocationsResponse(List.of(apiStation)));
      when(stationMapper.toStations(apiStation)).thenReturn(buildStation("1", "Main St", 0.0, 0.0));

      // Act
      stationService.getStationsByName(queryParams);

      // Assert
      verify(transportClient, times(1)).getLocationsByQuery(QUERY);
    }

    @Test
    @DisplayName("should call stationMapper once per ApiStation in the response")
    void shouldCallMapperOncePerApiStation() {
      // Arrange
      List<ApiStation> apiStations =
          List.of(buildApiStation("1", "A", 1.0, 1.0), buildApiStation("2", "B", 2.0, 2.0));
      when(transportClient.getLocationsByQuery(QUERY))
          .thenReturn(new ApiLocationsResponse(apiStations));
      when(stationMapper.toStations(any())).thenReturn(buildStation("x", "X", 0.0, 0.0));

      // Act
      stationService.getStationsByName(queryParams);

      // Assert
      verify(stationMapper, times(apiStations.size())).toStations(any(ApiStation.class));
    }
  }

  // =========================================================================
  // Private helpers
  // =========================================================================

  private ApiStation buildApiStation(String id, String name, Double x, Double y) {
    return new ApiStation(id, name, 1.0, new ApiCoordinate("WGS84", x, y), 0.0, "icon");
  }

  private Station buildStation(String id, String name, Double latitude, Double longitude) {
    return Station.builder().id(id).name(name).latitude(latitude).longitude(longitude).build();
  }
}
