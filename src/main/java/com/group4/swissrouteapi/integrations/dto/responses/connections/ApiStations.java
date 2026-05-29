package com.group4.swissrouteapi.integrations.dto.responses.connections;

import com.group4.swissrouteapi.integrations.dto.responses.ApiStation;

import java.util.List;

/**
 * ApiStations
 *
 * <p>Record representing station lists from the transport API. Holds origin stations ({@link
 * ApiStation} from) and destination stations ({@link ApiStation} to).
 */
public record ApiStations(List<ApiStation> from, List<ApiStation> to) {}
