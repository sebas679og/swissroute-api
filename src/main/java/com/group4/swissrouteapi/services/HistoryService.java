package com.group4.swissrouteapi.services;

import com.group4.swissrouteapi.dtos.requests.HistoryQueryParams;
import com.group4.swissrouteapi.dtos.responses.history.HistoryResponse;
import java.util.UUID;

/**
 * HistoryService
 *
 * <p>Service interface for retrieving user search history. Defines the contract for fetching
 * paginated history records based on query parameters and user identification.
 */
public interface HistoryService {

  HistoryResponse getAllHistory(HistoryQueryParams queryParams, UUID userId);

  void deleteHistoryItem(UUID itemId, UUID userId);
}
