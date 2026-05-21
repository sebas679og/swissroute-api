package com.group4.swissrouteapi.dtos.responses;

import java.util.UUID;
import lombok.Builder;
import lombok.Value;

/** LoginResponse Represents the data returned after a successful login. */
@Value
@Builder
public class LoginResponse {
  String token;
  String tokenType;
  long expiresIn;
  UUID userId;
}
