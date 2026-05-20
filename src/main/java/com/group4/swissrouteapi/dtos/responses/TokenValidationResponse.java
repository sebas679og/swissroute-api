package com.group4.swissrouteapi.dtos.responses;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/** TokenValidationResponse Represents the result of validating a JWT token. */
@Value
@Builder
public class TokenValidationResponse {

    boolean valid;
    UUID userId;
    String email;
}
