package com.group4.swissrouteapi.services.impl;

import com.group4.swissrouteapi.dtos.responses.history.HistoryResponse;
import com.group4.swissrouteapi.services.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    @Override
    public HistoryResponse getAllHistory(UUID userId) {
        return null;
    }
}
