package com.group4.swissrouteapi.dtos.responses.favorites;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FavStationsResponse {
  List<StationResponse> favoriteStations;
}
