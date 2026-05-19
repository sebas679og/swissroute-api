package com.group4.swissrouteapi.dtos.responses;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Value
@Builder
public class ErrorResponse {
    int code;
    String name;
    String description;

    @Builder.Default
    Instant timestamp = Instant.now().truncatedTo(ChronoUnit.MILLIS);
}
