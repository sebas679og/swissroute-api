package com.group4.swissrouteapi.integrations.dto.responses.stationboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.group4.swissrouteapi.integrations.dto.responses.ApiJourney;
import com.group4.swissrouteapi.integrations.dto.responses.ApiStation;

import java.util.List;

/**
 * Root response object from the station board API.
 *
 * @param station      the queried station
 * @param stationBoard list of upcoming journeys departing from the station
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StationBoardResponse(
        ApiStation station,
        @JsonProperty("stationboard") List<ApiJourney> stationBoard
) {
}
