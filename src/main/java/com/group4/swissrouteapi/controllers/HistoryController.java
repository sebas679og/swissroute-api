package com.group4.swissrouteapi.controllers;

import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.dtos.requests.HistoryQueryParams;
import com.group4.swissrouteapi.dtos.responses.history.HistoryResponse;
import com.group4.swissrouteapi.services.HistoryService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HistoryController
 *
 * <p>Spring REST controller responsible for exposing endpoints related to user search history.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class HistoryController {

  private final HistoryService historyService;

  @GetMapping(ApiPaths.History.HISTORY)
  public ResponseEntity<HistoryResponse> getHistory(
      Authentication authentication, @Valid @ModelAttribute HistoryQueryParams queryParams) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(historyService.getAllHistory(queryParams, UUID.fromString(authentication.getName())));
  }

  @DeleteMapping(ApiPaths.History.HISTORY_ITEM)
  public ResponseEntity<Void> deleteHistoryItem(
      Authentication authentication, @PathVariable UUID id) {
    historyService.deleteHistoryItem(id, UUID.fromString(authentication.getName()));
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping(ApiPaths.History.HISTORY)
  public ResponseEntity<Void> clearHistory(Authentication authentication) {
    historyService.clearHistory(UUID.fromString(authentication.getName()));
    return ResponseEntity.noContent().build();
  }
}
