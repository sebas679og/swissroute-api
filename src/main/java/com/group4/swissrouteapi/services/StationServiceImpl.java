package com.group4.swissrouteapi.services;

import com.group4.swissrouteapi.dtos.requests.StationsQueryParams;
import com.group4.swissrouteapi.dtos.responses.StationsResponse;
import com.group4.swissrouteapi.exceptions.NotFoundException;
import com.group4.swissrouteapi.integrations.TransportClient;
import com.group4.swissrouteapi.integrations.dto.responses.locations.ApiLocationsResponse;
import com.group4.swissrouteapi.integrations.dto.responses.locations.ApiStation;
import com.group4.swissrouteapi.utils.mappers.StationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StationServiceImpl implements StationService{

    private final TransportClient transportClient;
    private final StationMapper stationMapper;

    @Override
    public StationsResponse getStationsByName(StationsQueryParams requestParams) {
        ApiLocationsResponse api = transportClient.getLocations(requestParams.getQuery());
        List<ApiStation> apiStations = api.apiStations();

        if (apiStations == null || apiStations.isEmpty()){
            throw new NotFoundException("No stations found with the name: " + requestParams.getQuery());
        }

        return StationsResponse.builder()
                .stations(apiStations.stream().map(stationMapper::toStations).toList())
                .build();
    }
}
