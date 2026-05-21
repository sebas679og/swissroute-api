package com.group4.swissrouteapi.services;

import com.group4.swissrouteapi.dtos.requests.StationsQueryParams;
import com.group4.swissrouteapi.dtos.responses.StationsResponse;

public interface StationService {

    StationsResponse getStationsByName(StationsQueryParams requestParams);
}
