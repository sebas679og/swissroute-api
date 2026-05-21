package com.group4.swissrouteapi.controllers;

import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.dtos.requests.StationsQueryParams;
import com.group4.swissrouteapi.dtos.responses.StationsResponse;
import com.group4.swissrouteapi.services.StationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @GetMapping(ApiPaths.Station.STATIONS)
    public ResponseEntity<StationsResponse> getStations(
            @ParameterObject @Valid @ModelAttribute StationsQueryParams queryParams){
        return ResponseEntity.status(HttpStatus.OK).body(stationService.getStationsByName(queryParams));
    }
}
