package com.group4.swissrouteapi.services.impl;

import com.group4.swissrouteapi.dtos.requests.HistoryQueryParams;
import com.group4.swissrouteapi.dtos.responses.history.HistoryResponse;
import com.group4.swissrouteapi.models.SearchHistoryEntity;
import com.group4.swissrouteapi.services.HistoryService;
import com.group4.swissrouteapi.services.processors.HistoryProcessor;
import com.group4.swissrouteapi.utils.mappers.HistoryMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

/**
 * HistoryServiceImpl
 *
 * <p>Spring service implementation of {@link HistoryService}. Handles retrieval of user search
 * history by delegating queries to {@link HistoryProcessor} and mapping persistence entities into
 * immutable DTOs using {@link HistoryMapper}.
 */
@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

  private final HistoryProcessor historyProcessor;
  private final HistoryMapper historyMapper;

  @Override
  public HistoryResponse getAllHistory(HistoryQueryParams queryParams, UUID userId) {
    Page<SearchHistoryEntity> pageResult =
        historyProcessor.getAllHistoryByUserId(
            userId, queryParams.getPage(), queryParams.getSize());

    return HistoryResponse.builder()
        .history(pageResult.getContent().stream().map(historyMapper::toHistory).toList())
        .page(queryParams.getPage())
        .size(queryParams.getSize())
        .totalElements(pageResult.getTotalElements())
        .totalPages(pageResult.getTotalPages())
        .build();
  }

  @Override
  public void deleteHistoryItem(UUID itemId, UUID userId) {
    historyProcessor.deleteHistoryItem(itemId, userId);
  }

  @Override
  public void clearHistory(UUID userId) {
    historyProcessor.clearHistory(userId);
  }
}
