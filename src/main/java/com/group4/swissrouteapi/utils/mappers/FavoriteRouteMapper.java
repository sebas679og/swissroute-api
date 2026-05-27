package com.group4.swissrouteapi.utils.mappers;

import com.group4.swissrouteapi.dtos.responses.favorites.RegisterRouteResponse;
import com.group4.swissrouteapi.dtos.responses.favorites.Route;
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

  RegisterRouteResponse toFavoriteRouteResponse(FavoriteRouteEntity favoriteRouteEntity);

  Route toRoute(FavoriteRouteEntity favoriteRouteEntity);
}
