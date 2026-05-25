package com.group4.swissrouteapi.utils.mappers;

import com.group4.swissrouteapi.dtos.responses.history.History;
import com.group4.swissrouteapi.models.SearchHistoryEntity;
import org.mapstruct.Mapper;

/**
 * SearchHistoryMapper
 *
 * <p>MapStruct mapper interface for converting persistence entities into immutable DTOs for API
 * responses.
 */
@Mapper(componentModel = "spring")
public interface SearchHistoryMapper {
  History toHistory(SearchHistoryEntity entity);
}
