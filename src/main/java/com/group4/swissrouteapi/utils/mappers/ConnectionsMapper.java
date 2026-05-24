package com.group4.swissrouteapi.utils.mappers;

import com.group4.swissrouteapi.dtos.responses.connections.Connection;
import com.group4.swissrouteapi.dtos.responses.connections.Section;
import com.group4.swissrouteapi.integrations.dto.responses.connections.ApiConnection;
import com.group4.swissrouteapi.integrations.dto.responses.connections.ApiSection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * ConnectionsMapper
 *
 * <p>MapStruct mapper interface for converting transport API models into application response DTOs.
 *
 * <p>Provides mappings for:
 *
 * <ul>
 *   <li>{@link ApiConnection} → {@link Connection}
 *   <li>{@link ApiSection} → {@link Section}
 * </ul>
 *
 * <p>MapStruct automatically composes mappings: when converting an {@link ApiConnection} that
 * contains a list of {@link ApiSection}, the mapper will call {@link #toSection(ApiSection)} for
 * each element to build the {@code sections} list in {@link Connection}.
 *
 * <p>Configured with {@code componentModel = "spring"} to allow dependency injection in Spring
 * applications.
 */
@Mapper(componentModel = "spring")
public interface ConnectionsMapper {

  @Mapping(source = "from.station.name", target = "origin")
  @Mapping(source = "to.station.name", target = "destination")
  Connection toConnectionResponse(ApiConnection connection);

  @Mapping(source = "journey.category", target = "category")
  @Mapping(source = "journey.number", target = "number")
  @Mapping(source = "journey.operator", target = "operator")
  @Mapping(source = "journey.to", target = "destination")
  @Mapping(source = "departure.station.name", target = "departureStation")
  @Mapping(source = "departure.departure", target = "departureTime")
  @Mapping(source = "arrival.station.name", target = "arrivalStation")
  @Mapping(source = "arrival.arrival", target = "arrivalTime")
  @Mapping(source = "departure.platform", target = "platform")
  Section toSection(ApiSection section);
}
