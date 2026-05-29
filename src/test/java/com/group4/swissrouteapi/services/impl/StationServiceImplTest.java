package com.group4.swissrouteapi.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.group4.swissrouteapi.dtos.requests.StationsQueryParams;
import com.group4.swissrouteapi.dtos.responses.stations.Station;
import com.group4.swissrouteapi.dtos.responses.stations.StationsResponse;
import com.group4.swissrouteapi.exceptions.NotFoundException;
import com.group4.swissrouteapi.integrations.TransportClient;
import com.group4.swissrouteapi.integrations.dto.responses.ApiCoordinate;
import com.group4.swissrouteapi.integrations.dto.responses.locations.ApiLocationsResponse;
import com.group4.swissrouteapi.integrations.dto.responses.ApiStation;
import com.group4.swissrouteapi.utils.mappers.StationMapper;
import java.util.Collections;
import java.util.List;
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

  // ---------------------------------------------------------------------------
  // Shared fixtures
  // ---------------------------------------------------------------------------

  private static final String QUERY = "Basel";
  private static final double LATITUDE = 47.5596;
  private static final double LONGITUDE = 7.5886;

  private ApiStation buildApiStation(String id, String name, Integer distance) {
    return new ApiStation(
        id, name, 0.9, new ApiCoordinate("Point", LONGITUDE, LATITUDE), distance, "train");
  }

  private Station buildStation(String id, String name, Integer distance) {
    return Station.builder()
        .id(id)
        .name(name)
        .latitude(LATITUDE)
        .longitude(LONGITUDE)
        .distance(distance)
        .build();
  }

  private StationsQueryParams queryParams() {
    return StationsQueryParams.builder().query(QUERY).build();
  }

  private StationsQueryParams coordParams() {
    return StationsQueryParams.builder().latitude(LATITUDE).longitude(LONGITUDE).build();
  }

  private StationsQueryParams coordParams(double lat, double lon) {
    return StationsQueryParams.builder().latitude(lat).longitude(lon).build();
  }

  // ===========================================================================
  // Routing — getStations delegates based on params
  // ===========================================================================

  @Nested
  @DisplayName("getStations() - routing")
  class RoutingTest {

    @Test
    @DisplayName("should call getLocationsByQuery when only query is set")
    void shouldCallGetLocationsByQueryWhenQueryOnly() {
      ApiStation api = buildApiStation("8503000", "Basel SBB", null);
      when(transportClient.getLocationsByQuery(QUERY))
          .thenReturn(new ApiLocationsResponse(List.of(api)));
      when(stationMapper.toStations(api)).thenReturn(buildStation("8503000", "Basel SBB", null));

      stationService.getStations(queryParams());

      verify(transportClient).getLocationsByQuery(QUERY);
      verify(transportClient, never()).getLocationsByCoordinates(LATITUDE, LONGITUDE);
    }

    @Test
    @DisplayName("should call getLocationsByCoordinates when latitude and longitude are set")
    void shouldCallGetLocationsByCoordinatesWhenCoordsSet() {
      ApiStation api = buildApiStation("8503000", "Basel SBB", 120);
      when(transportClient.getLocationsByCoordinates(LATITUDE, LONGITUDE))
          .thenReturn(new ApiLocationsResponse(List.of(api)));
      when(stationMapper.toStations(api)).thenReturn(buildStation("8503000", "Basel SBB", 120));

      stationService.getStations(coordParams());

      verify(transportClient).getLocationsByCoordinates(LATITUDE, LONGITUDE);
      verify(transportClient, never()).getLocationsByQuery(QUERY);
    }
  }

  // ===========================================================================
  // getStationsByName (via query param)
  // ===========================================================================

  @Nested
  @DisplayName("getStations() - search by name")
  class GetStationsByNameTest {

    @Test
    @DisplayName("should return a mapped StationsResponse for a valid query")
    void shouldReturnMappedResponseForValidQuery() {
      ApiStation api = buildApiStation("8503000", "Basel SBB", null);
      Station mapped = buildStation("8503000", "Basel SBB", null);

      when(transportClient.getLocationsByQuery(QUERY))
          .thenReturn(new ApiLocationsResponse(List.of(api)));
      when(stationMapper.toStations(api)).thenReturn(mapped);

      StationsResponse result = stationService.getStations(queryParams());

      assertThat(result.getStations()).containsExactly(mapped);
    }

    @Test
    @DisplayName("should map every station returned by the client")
    void shouldMapEveryStation() {
      ApiStation api1 = buildApiStation("8503000", "Basel SBB", null);
      ApiStation api2 = buildApiStation("8503001", "Basel Bad Bf", null);
      Station s1 = buildStation("8503000", "Basel SBB", null);
      Station s2 = buildStation("8503001", "Basel Bad Bf", null);

      when(transportClient.getLocationsByQuery(QUERY))
          .thenReturn(new ApiLocationsResponse(List.of(api1, api2)));
      when(stationMapper.toStations(api1)).thenReturn(s1);
      when(stationMapper.toStations(api2)).thenReturn(s2);

      StationsResponse result = stationService.getStations(queryParams());

      assertThat(result.getStations()).containsExactly(s1, s2);
    }

    @Test
    @DisplayName("should delegate mapping to stationMapper for each station")
    void shouldDelegateMappingToMapper() {
      ApiStation api = buildApiStation("8503000", "Basel SBB", null);

      when(transportClient.getLocationsByQuery(QUERY))
          .thenReturn(new ApiLocationsResponse(List.of(api)));
      when(stationMapper.toStations(api)).thenReturn(buildStation("8503000", "Basel SBB", null));

      stationService.getStations(queryParams());

      verify(stationMapper).toStations(api);
    }

    @Test
    @DisplayName("should throw NotFoundException when the API returns an empty list")
    void shouldThrowNotFoundWhenApiReturnsEmptyList() {
      when(transportClient.getLocationsByQuery(QUERY))
          .thenReturn(new ApiLocationsResponse(Collections.emptyList()));

      assertThatThrownBy(() -> stationService.getStations(queryParams()))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("No stations found with the name: " + QUERY);
    }

    @Test
    @DisplayName("should throw NotFoundException when the API returns a null stations list")
    void shouldThrowNotFoundWhenApiReturnsNullList() {
      when(transportClient.getLocationsByQuery(QUERY)).thenReturn(new ApiLocationsResponse(null));

      assertThatThrownBy(() -> stationService.getStations(queryParams()))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("No stations found with the name: " + QUERY);
    }

    @Test
    @DisplayName("should not call the mapper when no stations are found")
    void shouldNotCallMapperWhenNoStationsFound() {
      when(transportClient.getLocationsByQuery(QUERY))
          .thenReturn(new ApiLocationsResponse(Collections.emptyList()));

      assertThatThrownBy(() -> stationService.getStations(queryParams()))
          .isInstanceOf(NotFoundException.class);

      verify(stationMapper, never()).toStations(org.mockito.ArgumentMatchers.any());
    }
  }

  // ===========================================================================
  // getStationsByCoordinates (via latitude + longitude params)
  // ===========================================================================

  @Nested
  @DisplayName("getStations() - search by coordinates")
  class GetStationsByCoordinatesTest {

    @Test
    @DisplayName("should return a mapped StationsResponse for valid coordinates")
    void shouldReturnMappedResponseForValidCoordinates() {
      ApiStation api = buildApiStation("8503000", "Basel SBB", 120);
      Station mapped = buildStation("8503000", "Basel SBB", 120);

      when(transportClient.getLocationsByCoordinates(LATITUDE, LONGITUDE))
          .thenReturn(new ApiLocationsResponse(List.of(api)));
      when(stationMapper.toStations(api)).thenReturn(mapped);

      StationsResponse result = stationService.getStations(coordParams());

      assertThat(result.getStations()).containsExactly(mapped);
    }

    @Test
    @DisplayName("should include distance in the mapped station")
    void shouldIncludeDistanceInMappedStation() {
      ApiStation api = buildApiStation("8503000", "Basel SBB", 250);
      Station mapped = buildStation("8503000", "Basel SBB", 250);

      when(transportClient.getLocationsByCoordinates(LATITUDE, LONGITUDE))
          .thenReturn(new ApiLocationsResponse(List.of(api)));
      when(stationMapper.toStations(api)).thenReturn(mapped);

      StationsResponse result = stationService.getStations(coordParams());

      assertThat(result.getStations().getFirst().getDistance()).isEqualTo(250);
    }

    @Test
    @DisplayName("should map every station returned by the client")
    void shouldMapEveryStation() {
      ApiStation api1 = buildApiStation("8503000", "Basel SBB", 120);
      ApiStation api2 = buildApiStation("8503001", "Basel Bad Bf", 340);
      Station s1 = buildStation("8503000", "Basel SBB", 120);
      Station s2 = buildStation("8503001", "Basel Bad Bf", 340);

      when(transportClient.getLocationsByCoordinates(LATITUDE, LONGITUDE))
          .thenReturn(new ApiLocationsResponse(List.of(api1, api2)));
      when(stationMapper.toStations(api1)).thenReturn(s1);
      when(stationMapper.toStations(api2)).thenReturn(s2);

      StationsResponse result = stationService.getStations(coordParams());

      assertThat(result.getStations()).containsExactly(s1, s2);
    }

    @Test
    @DisplayName("should delegate mapping to stationMapper for each station")
    void shouldDelegateMappingToMapper() {
      ApiStation api = buildApiStation("8503000", "Basel SBB", 120);

      when(transportClient.getLocationsByCoordinates(LATITUDE, LONGITUDE))
          .thenReturn(new ApiLocationsResponse(List.of(api)));
      when(stationMapper.toStations(api)).thenReturn(buildStation("8503000", "Basel SBB", 120));

      stationService.getStations(coordParams());

      verify(stationMapper).toStations(api);
    }

    @Test
    @DisplayName("should throw NotFoundException when the API returns an empty list")
    void shouldThrowNotFoundWhenApiReturnsEmptyList() {
      when(transportClient.getLocationsByCoordinates(LATITUDE, LONGITUDE))
          .thenReturn(new ApiLocationsResponse(Collections.emptyList()));

      assertThatThrownBy(() -> stationService.getStations(coordParams()))
          .isInstanceOf(NotFoundException.class)
          .hasMessage(
              "No stations found at the coordinates: (" + LATITUDE + ", " + LONGITUDE + ")");
    }

    @Test
    @DisplayName("should throw NotFoundException when the API returns a null stations list")
    void shouldThrowNotFoundWhenApiReturnsNullList() {
      when(transportClient.getLocationsByCoordinates(LATITUDE, LONGITUDE))
          .thenReturn(new ApiLocationsResponse(null));

      assertThatThrownBy(() -> stationService.getStations(coordParams()))
          .isInstanceOf(NotFoundException.class)
          .hasMessage(
              "No stations found at the coordinates: (" + LATITUDE + ", " + LONGITUDE + ")");
    }

    @Test
    @DisplayName("should not call the mapper when no stations are found")
    void shouldNotCallMapperWhenNoStationsFound() {
      when(transportClient.getLocationsByCoordinates(LATITUDE, LONGITUDE))
          .thenReturn(new ApiLocationsResponse(Collections.emptyList()));

      assertThatThrownBy(() -> stationService.getStations(coordParams()))
          .isInstanceOf(NotFoundException.class);

      verify(stationMapper, never()).toStations(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("should pass the exact coordinates to the client")
    void shouldPassExactCoordinatesToClient() {
      double lat = 46.9481;
      double lon = 7.4474;
      ApiStation api = buildApiStation("8507000", "Bern", 50);

      when(transportClient.getLocationsByCoordinates(lat, lon))
          .thenReturn(new ApiLocationsResponse(List.of(api)));
      when(stationMapper.toStations(api)).thenReturn(buildStation("8507000", "Bern", 50));

      stationService.getStations(coordParams(lat, lon));

      verify(transportClient).getLocationsByCoordinates(lat, lon);
    }
  }

  // ===========================================================================
  // distance field — name search returns null, coordinates search returns value
  // ===========================================================================

  @Nested
  @DisplayName("distance field behaviour")
  class DistanceFieldTest {

    @Test
    @DisplayName("should return null distance when searching by name")
    void shouldReturnNullDistanceForNameSearch() {
      ApiStation api = buildApiStation("8503000", "Basel SBB", null);
      Station mapped = buildStation("8503000", "Basel SBB", null);

      when(transportClient.getLocationsByQuery(QUERY))
          .thenReturn(new ApiLocationsResponse(List.of(api)));
      when(stationMapper.toStations(api)).thenReturn(mapped);

      StationsResponse result = stationService.getStations(queryParams());

      assertThat(result.getStations().getFirst().getDistance()).isNull();
    }

    @Test
    @DisplayName("should return non-null distance when searching by coordinates")
    void shouldReturnNonNullDistanceForCoordinatesSearch() {
      ApiStation api = buildApiStation("8503000", "Basel SBB", 185);
      Station mapped = buildStation("8503000", "Basel SBB", 185);

      when(transportClient.getLocationsByCoordinates(LATITUDE, LONGITUDE))
          .thenReturn(new ApiLocationsResponse(List.of(api)));
      when(stationMapper.toStations(api)).thenReturn(mapped);

      StationsResponse result = stationService.getStations(coordParams());

      assertThat(result.getStations().getFirst().getDistance()).isEqualTo(185);
    }
  }
}
