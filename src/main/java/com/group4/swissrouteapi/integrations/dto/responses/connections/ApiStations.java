package com.group4.swissrouteapi.integrations.dto.responses.connections;

import java.util.List;

/**
 * ApiStations
 *
 * <p>Record representing station lists from the transport API. Holds origin stations ({@link
 * ApiStationConnection} from) and destination stations ({@link ApiStationConnection} to).
 */
public record ApiStations(List<ApiStationConnection> from, List<ApiStationConnection> to) {}
