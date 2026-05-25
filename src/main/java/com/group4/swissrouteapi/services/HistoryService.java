package com.group4.swissrouteapi.services;

import com.group4.swissrouteapi.dtos.responses.history.HistoryResponse;

import java.util.UUID;

public interface HistoryService {

    HistoryResponse getAllHistory(UUID userId);
}
