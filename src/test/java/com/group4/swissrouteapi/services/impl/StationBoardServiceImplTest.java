package com.group4.swissrouteapi.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.group4.swissrouteapi.dtos.requests.StationBoardQueryParams;
import com.group4.swissrouteapi.dtos.responses.board.StationBoard;
import com.group4.swissrouteapi.dtos.responses.board.StationsBoardResponse;
import com.group4.swissrouteapi.exceptions.NotFoundException;
import com.group4.swissrouteapi.integrations.TransportClient;
import com.group4.swissrouteapi.integrations.dto.responses.ApiJourney;
import com.group4.swissrouteapi.integrations.dto.responses.ApiStation;
import com.group4.swissrouteapi.integrations.dto.responses.stationboard.ApiStationBoardResponse;
import com.group4.swissrouteapi.utils.enums.TransportType;
import com.group4.swissrouteapi.utils.mappers.StationBoardMapper;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("StationBoardServiceImpl")
class StationBoardServiceImplTest {

  @Mock private TransportClient transportClient;

  @Mock private StationBoardMapper stationBoardMapper;

  @InjectMocks private StationBoardServiceImpl stationBoardService;

  // ─────────────────────────────────────────────────────────────────────────
  // Fixtures
  // ─────────────────────────────────────────────────────────────────────────

  private StationBoardQueryParams buildParams() {
    return StationBoardQueryParams.builder()
        .station("Aarau")
        .id("8502113")
        .limit(10)
        .transportType(List.of(TransportType.TRAIN))
        .build();
  }

  private StationBoardQueryParams buildMinimalParams() {
    return StationBoardQueryParams.builder().station("Aarau").build();
  }

  private ApiJourney buildApiJourney(String name, String category, String destination) {
    return new ApiJourney(
        null, name, category, null, "1", "1", "SBB", destination, List.of(), 2, 3);
  }

  private StationBoard buildStationBoard(String serviceName, String category, String destination) {
    return StationBoard.builder()
        .serviceName(serviceName)
        .category(category)
        .destinationName(destination)
        .departureTime(OffsetDateTime.parse("2024-10-10T08:00:00+02:00"))
        .build();
  }

  private ApiStationBoardResponse buildApiResponse(List<ApiJourney> journeys) {
    ApiStation station = new ApiStation("8502113", "Aarau", 1.0, null, 0, null);
    return new ApiStationBoardResponse(station, journeys);
  }

  // ─────────────────────────────────────────────────────────────────────────
  // getStationBoards — success
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("getStationBoards — success")
  class GetStationBoardsSuccess {

    @Test
    @DisplayName("returns StationsBoardResponse containing all mapped stationBoards")
    void returnsStationsBoardResponse_containingAllMappedBoards() {
      StationBoardQueryParams params = buildParams();
      ApiJourney journey = buildApiJourney("IC 1", "IC", "Bern");
      StationBoard board = buildStationBoard("IC 1", "IC", "Bern");
      ApiStationBoardResponse apiResponse = buildApiResponse(List.of(journey));

      when(transportClient.getStationBoard(
              params.getStation(), params.getId(),
              params.getLimit(), params.getTransportType()))
          .thenReturn(apiResponse);
      when(stationBoardMapper.toStationBoard(journey)).thenReturn(board);

      StationsBoardResponse result = stationBoardService.getStationBoards(params);

      assertThat(result.getStationBoards()).containsExactly(board);
    }

    @Test
    @DisplayName("maps every ApiJourney in the response through the mapper")
    void mapsEveryApiJourney_throughMapper() {
      StationBoardQueryParams params = buildParams();
      ApiJourney j1 = buildApiJourney("IC 1", "IC", "Bern");
      ApiJourney j2 = buildApiJourney("RE 5", "RE", "Zurich");
      StationBoard b1 = buildStationBoard("IC 1", "IC", "Bern");
      StationBoard b2 = buildStationBoard("RE 5", "RE", "Zurich");

      when(transportClient.getStationBoard(any(), any(), any(), any()))
          .thenReturn(buildApiResponse(List.of(j1, j2)));
      when(stationBoardMapper.toStationBoard(j1)).thenReturn(b1);
      when(stationBoardMapper.toStationBoard(j2)).thenReturn(b2);

      StationsBoardResponse result = stationBoardService.getStationBoards(params);

      assertThat(result.getStationBoards()).containsExactly(b1, b2);
      verify(stationBoardMapper).toStationBoard(j1);
      verify(stationBoardMapper).toStationBoard(j2);
    }

    @Test
    @DisplayName("delegates to transport client with all query param fields")
    void delegatesToTransportClient_withAllQueryParamFields() {
      StationBoardQueryParams params = buildParams();
      ApiJourney journey = buildApiJourney("IC 1", "IC", "Bern");

      when(transportClient.getStationBoard(
              params.getStation(), params.getId(),
              params.getLimit(), params.getTransportType()))
          .thenReturn(buildApiResponse(List.of(journey)));
      when(stationBoardMapper.toStationBoard(journey))
          .thenReturn(buildStationBoard("IC 1", "IC", "Bern"));

      stationBoardService.getStationBoards(params);

      verify(transportClient)
          .getStationBoard(
              params.getStation(), params.getId(),
              params.getLimit(), params.getTransportType());
    }

    @Test
    @DisplayName("delegates to transport client with null optional fields when not set")
    void delegatesToTransportClient_withNullOptionalFields_whenNotSet() {
      StationBoardQueryParams params = buildMinimalParams();
      ApiJourney journey = buildApiJourney("S1", "S", "Olten");

      when(transportClient.getStationBoard("Aarau", null, null, List.of()))
          .thenReturn(buildApiResponse(List.of(journey)));
      when(stationBoardMapper.toStationBoard(journey))
          .thenReturn(buildStationBoard("S1", "S", "Olten"));

      stationBoardService.getStationBoards(params);

      verify(transportClient).getStationBoard("Aarau", null, null, List.of());
    }
  }

  // ─────────────────────────────────────────────────────────────────────────
  // getStationBoards — NotFoundException
  // ─────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("getStationBoards — NotFoundException")
  class GetStationBoardsNotFound {

    @Test
    @DisplayName("throws NotFoundException when transport client returns null")
    void throwsNotFoundException_whenTransportClientReturnsNull() {
      when(transportClient.getStationBoard(any(), any(), any(), any())).thenReturn(null);

      assertThatThrownBy(() -> stationBoardService.getStationBoards(buildParams()))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("Station Board not found.");
    }

    @Test
    @DisplayName("throws NotFoundException when stationBoard list in response is null")
    void throwsNotFoundException_whenStationBoardListIsNull() {
      ApiStationBoardResponse responseWithNullBoard = new ApiStationBoardResponse(null, null);
      when(transportClient.getStationBoard(any(), any(), any(), any()))
          .thenReturn(responseWithNullBoard);

      assertThatThrownBy(() -> stationBoardService.getStationBoards(buildParams()))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("Station Board not found.");
    }

    @Test
    @DisplayName("throws NotFoundException when stationBoard list in response is empty")
    void throwsNotFoundException_whenStationBoardListIsEmpty() {
      when(transportClient.getStationBoard(any(), any(), any(), any()))
          .thenReturn(buildApiResponse(List.of()));

      assertThatThrownBy(() -> stationBoardService.getStationBoards(buildParams()))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("Station Board not found.");
    }

    @Test
    @DisplayName("does not interact with mapper when transport client returns null")
    void doesNotInteractWithMapper_whenTransportClientReturnsNull() {
      when(transportClient.getStationBoard(any(), any(), any(), any())).thenReturn(null);

      assertThatThrownBy(() -> stationBoardService.getStationBoards(buildParams()))
          .isInstanceOf(NotFoundException.class);

      verifyNoInteractions(stationBoardMapper);
    }

    @Test
    @DisplayName("does not interact with mapper when stationBoard list is empty")
    void doesNotInteractWithMapper_whenStationBoardListIsEmpty() {
      when(transportClient.getStationBoard(any(), any(), any(), any()))
          .thenReturn(buildApiResponse(List.of()));

      assertThatThrownBy(() -> stationBoardService.getStationBoards(buildParams()))
          .isInstanceOf(NotFoundException.class);

      verifyNoInteractions(stationBoardMapper);
    }
  }
}
