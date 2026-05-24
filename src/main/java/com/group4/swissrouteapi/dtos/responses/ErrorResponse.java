package com.group4.swissrouteapi.dtos.responses;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.Builder;
import lombok.Value;

/**
 * ErrorResponse
 *
 * <p>Data transfer object representing standardized error details returned by the API when
 * exceptions occur.
 *
 * <p>Contains the HTTP status code, status name, and a descriptive message explaining the error. A
 * timestamp is included by default to indicate when the error was generated, truncated to
 * milliseconds for precision.
 *
 * <p>Annotated with Lombok {@link Value} for immutability and {@link Builder} to support fluent
 * construction.
 */
@Value
@Builder
public class ErrorResponse {
  int code;
  String name;
  String description;

  @Builder.Default Instant timestamp = Instant.now().truncatedTo(ChronoUnit.MILLIS);
}
