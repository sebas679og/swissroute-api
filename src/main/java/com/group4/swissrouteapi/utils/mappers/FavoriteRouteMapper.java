package com.group4.swissrouteapi.utils.mappers;

import com.group4.swissrouteapi.dtos.responses.favorites.RouteResponse;
import com.group4.swissrouteapi.models.FavoriteRouteEntity;
import org.mapstruct.Mapper;

/**
 * FavoriteRouteMapper
 *
 * <p>MapStruct mapper interface for converting persistence entities into immutable DTOs for API
 * responses.
 */
@Mapper(componentModel = "spring")
public interface FavoriteRouteMapper {

  RouteResponse toFavoriteRouteResponse(FavoriteRouteEntity favoriteRouteEntity);
}
