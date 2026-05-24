package com.group4.swissrouteapi.dtos.responses;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

/** TokenValidationResponse Represents the result of validating a JWT token. */
@Value
@Builder
public class TokenValidationResponse {

  boolean valid;
  UUID userId;
  String email;
}
