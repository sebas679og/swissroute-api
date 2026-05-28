package com.group4.swissrouteapi.controllers;

import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.dtos.requests.StationRequest;
import com.group4.swissrouteapi.dtos.responses.favorites.StationResponse;
import com.group4.swissrouteapi.dtos.responses.favorites.StationsResponse;
import com.group4.swissrouteapi.services.FavoriteStationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(
        name = "Favorites stations",
        description = "Controller in charge of processing requests from favorite stations.")
public class FavoriteStationController {

    private final FavoriteStationService favoriteStationService;

    @PostMapping(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
    public ResponseEntity<StationResponse> addFavoriteStations(Authentication authentication, @RequestBody @Valid StationRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(favoriteStationService.addFavoriteStation(
                UUID.fromString(authentication.getName()),
                request));
    }

    @GetMapping(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
    public ResponseEntity<StationsResponse> getFavoriteStations(Authentication authentication){
        return ResponseEntity.status(HttpStatus.OK).body(favoriteStationService.getFavoriteStation(UUID.fromString(authentication.getName())));
    }

    @DeleteMapping(ApiPaths.FavoriteStations.FAVORITE_STATION)
    public ResponseEntity<Void> deleteFavoriteStations(Authentication authentication, @PathVariable UUID stationId){
        favoriteStationService.removeFavoriteStation(UUID.fromString(authentication.getName()), stationId);
        return ResponseEntity.noContent().build();
    }
}
