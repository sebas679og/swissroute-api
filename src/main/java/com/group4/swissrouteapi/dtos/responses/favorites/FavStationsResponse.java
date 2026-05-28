package com.group4.swissrouteapi.dtos.responses.favorites;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class FavStationsResponse {
    List<StationResponse> favoriteStations;
}
