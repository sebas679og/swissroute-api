package com.group4.swissrouteapi.utils.mappers;

import com.group4.swissrouteapi.dtos.responses.board.StationBoard;
import com.group4.swissrouteapi.integrations.dto.responses.ApiJourney;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * StationBoardMapper
 *
 * <p>MapStruct mapper interface for transforming API journey data into {@link StationBoard} DTOs.
 */
@Mapper(componentModel = "spring")
public interface StationBoardMapper {

  @Mapping(target = "serviceName", source = "name")
  @Mapping(target = "destinationName", source = "to")
  @Mapping(target = "departureTime", source = "stop.departure")
  StationBoard toStationBoard(ApiJourney journey);
}
