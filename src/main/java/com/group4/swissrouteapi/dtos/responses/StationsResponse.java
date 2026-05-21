package com.group4.swissrouteapi.dtos.responses;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class StationsResponse {
    List<Station> stations;
}
