package com.group4.swissrouteapi.utils.mappers;

import com.group4.swissrouteapi.dtos.responses.favorites.StationResponse;
import com.group4.swissrouteapi.dtos.responses.stations.Station;
import com.group4.swissrouteapi.integrations.dto.responses.ApiStation;
import com.group4.swissrouteapi.models.FavoriteStationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * StationMapper
 *
 * <p>MapStruct mapper interface for converting external API station representations into internal
 * domain models.
 *
 * <p>Configured as a Spring component via {@code componentModel = "spring"} to enable dependency
 * injection.
 *
 * <p>Provides mapping logic to transform {@link ApiStation} objects into immutable {@link Station}
 * instances, including extraction of geographic coordinates.
 */
@Mapper(componentModel = "spring")
public interface StationMapper {

  /**
   * Maps an {@link ApiStation} object to a {@link Station} domain model.
   *
   * <p>Extracts latitude and longitude values from the nested {@code coordinate} object.
   *
   * @param apiStation the source API station object
   * @return the mapped {@link Station} domain model
   */
  @Mapping(target = "latitude", source = "coordinate.x")
  @Mapping(target = "longitude", source = "coordinate.y")
  @Mapping(target = "distance", source = "distance")
  Station toStations(ApiStation apiStation);

  StationResponse toStationResponse(FavoriteStationEntity favoriteStationEntity);
}
