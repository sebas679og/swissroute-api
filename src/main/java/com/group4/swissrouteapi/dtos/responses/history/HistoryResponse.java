package com.group4.swissrouteapi.dtos.responses.history;

import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * HistoryResponse
 *
 * <p>Immutable DTO representing a paginated history response. Holds a list of {@link History}
 * records along with pagination metadata.
 */
@Value
@Builder
public class HistoryResponse {
  List<History> history;
  Integer page;
  Integer size;
  Integer totalElements;
  Integer totalPages;
}
