package com.group4.swissrouteapi.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.group4.swissrouteapi.dtos.requests.HistoryQueryParams;
import com.group4.swissrouteapi.dtos.responses.history.History;
import com.group4.swissrouteapi.dtos.responses.history.HistoryResponse;
import com.group4.swissrouteapi.models.SearchHistoryEntity;
import com.group4.swissrouteapi.services.processors.HistoryProcessor;
import com.group4.swissrouteapi.utils.mappers.HistoryMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("HistoryServiceImpl")
class HistoryServiceImplTest {

  @Mock private HistoryProcessor historyProcessor;

  @Mock private HistoryMapper historyMapper;

  @InjectMocks private HistoryServiceImpl historyService;

  // -------------------------------------------------------------------------
  // Fixtures
  // -------------------------------------------------------------------------

  private static final UUID USER_ID = UUID.randomUUID();
  private static final UUID ITEM_ID = UUID.randomUUID();

  private SearchHistoryEntity buildEntity(String origin, String destination) {
    return SearchHistoryEntity.builder()
        .id(UUID.randomUUID())
        .origin(origin)
        .destination(destination)
        .resultCount(5)
        .searchedAt(Instant.now())
        .build();
  }

  private History buildHistory(SearchHistoryEntity entity) {
    return History.builder()
        .id(entity.getId())
        .origin(entity.getOrigin())
        .destination(entity.getDestination())
        .resultCount(entity.getResultCount())
        .searchedAt(entity.getSearchedAt())
        .build();
  }

  private HistoryQueryParams defaultQueryParams() {
    return HistoryQueryParams.builder().page(1).size(20).build();
  }

  // -------------------------------------------------------------------------
  // getAllHistory
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getAllHistory")
  class GetAllHistory {

    @Test
    @DisplayName("returns mapped history response when page has results")
    void returnsHistoryResponse_whenPageHasResults() {
      SearchHistoryEntity entity = buildEntity("Zurich", "Bern");
      History mappedHistory = buildHistory(entity);
      Page<SearchHistoryEntity> page = new PageImpl<>(List.of(entity));
      HistoryQueryParams params = defaultQueryParams();

      when(historyProcessor.getAllHistoryByUserId(USER_ID, params.getPage(), params.getSize()))
          .thenReturn(page);
      when(historyMapper.toHistory(entity)).thenReturn(mappedHistory);

      HistoryResponse response = historyService.getAllHistory(params, USER_ID);

      assertThat(response.getHistory()).containsExactly(mappedHistory);
      assertThat(response.getPage()).isEqualTo(params.getPage());
      assertThat(response.getSize()).isEqualTo(params.getSize());
      assertThat(response.getTotalElements()).isEqualTo(page.getTotalElements());
      assertThat(response.getTotalPages()).isEqualTo(page.getTotalPages());
    }

    @Test
    @DisplayName("returns empty history list when page has no results")
    void returnsEmptyHistoryList_whenPageIsEmpty() {
      HistoryQueryParams params = defaultQueryParams();
      Page<SearchHistoryEntity> emptyPage = Page.empty();

      when(historyProcessor.getAllHistoryByUserId(USER_ID, params.getPage(), params.getSize()))
          .thenReturn(emptyPage);

      HistoryResponse response = historyService.getAllHistory(params, USER_ID);

      assertThat(response.getHistory()).isEmpty();
      assertThat(response.getTotalElements()).isZero();
      assertThat(response.getTotalPages()).isOne();
      verifyNoInteractions(historyMapper);
    }

    @Test
    @DisplayName("maps every entity in the page to a History domain object")
    void mapsEveryEntity_toHistoryDomainObject() {
      SearchHistoryEntity first = buildEntity("Geneva", "Lausanne");
      SearchHistoryEntity second = buildEntity("Basel", "Zurich");
      History firstHistory = buildHistory(first);
      History secondHistory = buildHistory(second);
      Page<SearchHistoryEntity> page = new PageImpl<>(List.of(first, second));
      HistoryQueryParams params = defaultQueryParams();

      when(historyProcessor.getAllHistoryByUserId(USER_ID, params.getPage(), params.getSize()))
          .thenReturn(page);
      when(historyMapper.toHistory(first)).thenReturn(firstHistory);
      when(historyMapper.toHistory(second)).thenReturn(secondHistory);

      HistoryResponse response = historyService.getAllHistory(params, USER_ID);

      assertThat(response.getHistory()).containsExactly(firstHistory, secondHistory);
      verify(historyMapper).toHistory(first);
      verify(historyMapper).toHistory(second);
    }

    @Test
    @DisplayName("propagates page and size from query params into the response")
    void propagatesPageAndSize_fromQueryParamsIntoResponse() {
      HistoryQueryParams params = HistoryQueryParams.builder().page(3).size(10).build();
      Page<SearchHistoryEntity> page =
          new PageImpl<>(List.of(), org.springframework.data.domain.PageRequest.of(2, 10), 0);

      when(historyProcessor.getAllHistoryByUserId(USER_ID, 3, 10)).thenReturn(page);

      HistoryResponse response = historyService.getAllHistory(params, USER_ID);

      assertThat(response.getPage()).isEqualTo(3);
      assertThat(response.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("delegates to processor with correct userId, page, and size")
    void delegatesToProcessor_withCorrectArguments() {
      HistoryQueryParams params = HistoryQueryParams.builder().page(2).size(5).build();
      when(historyProcessor.getAllHistoryByUserId(USER_ID, 2, 5)).thenReturn(Page.empty());

      historyService.getAllHistory(params, USER_ID);

      verify(historyProcessor).getAllHistoryByUserId(USER_ID, 2, 5);
    }

    @Test
    @DisplayName("propagates exception thrown by processor")
    void propagatesException_thrownByProcessor() {
      HistoryQueryParams params = defaultQueryParams();
      when(historyProcessor.getAllHistoryByUserId(USER_ID, params.getPage(), params.getSize()))
          .thenThrow(new RuntimeException("Processor failure"));

      org.assertj.core.api.Assertions.assertThatThrownBy(
              () -> historyService.getAllHistory(params, USER_ID))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Processor failure");
    }
  }

  // -------------------------------------------------------------------------
  // deleteHistoryItem
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("deleteHistoryItem")
  class DeleteHistoryItem {

    @Test
    @DisplayName("delegates deletion to processor with correct itemId and userId")
    void delegatesDeletionToProcessor_withCorrectArguments() {
      historyService.deleteHistoryItem(ITEM_ID, USER_ID);

      verify(historyProcessor).deleteHistoryItem(ITEM_ID, USER_ID);
    }

    @Test
    @DisplayName("does not interact with historyMapper during deletion")
    void doesNotInteractWithMapper_duringDeletion() {
      historyService.deleteHistoryItem(ITEM_ID, USER_ID);

      verifyNoInteractions(historyMapper);
    }

    @Test
    @DisplayName("propagates exception thrown by processor during deletion")
    void propagatesException_thrownByProcessor() {
      doThrow(new RuntimeException("Item not found"))
          .when(historyProcessor)
          .deleteHistoryItem(ITEM_ID, USER_ID);

      org.assertj.core.api.Assertions.assertThatThrownBy(
              () -> historyService.deleteHistoryItem(ITEM_ID, USER_ID))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Item not found");
    }
  }

  // -------------------------------------------------------------------------
  // clearHistory
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("clearHistory")
  class ClearHistory {

    @Test
    @DisplayName("delegates clearing to processor with correct userId")
    void delegatesClearingToProcessor_withCorrectUserId() {
      historyService.clearHistory(USER_ID);

      verify(historyProcessor).clearHistory(USER_ID);
    }

    @Test
    @DisplayName("does not interact with historyMapper when clearing history")
    void doesNotInteractWithMapper_whenClearingHistory() {
      historyService.clearHistory(USER_ID);

      verifyNoInteractions(historyMapper);
    }

    @Test
    @DisplayName("propagates exception thrown by processor when clearing history")
    void propagatesException_thrownByProcessor() {
      doThrow(new RuntimeException("User not found")).when(historyProcessor).clearHistory(USER_ID);

      org.assertj.core.api.Assertions.assertThatThrownBy(() -> historyService.clearHistory(USER_ID))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("User not found");
    }
  }
}
