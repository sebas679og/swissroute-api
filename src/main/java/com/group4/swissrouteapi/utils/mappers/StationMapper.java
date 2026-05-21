package com.group4.swissrouteapi.utils.mappers;

import com.group4.swissrouteapi.dtos.responses.Station;
import com.group4.swissrouteapi.integrations.dto.responses.locations.ApiStation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StationMapper {

    @Mapping(target = "latitude", source = "apiCoordinate.x")
    @Mapping(target = "longitude", source = "apiCoordinate.y")
    Station toStations(ApiStation apiStation);
}
