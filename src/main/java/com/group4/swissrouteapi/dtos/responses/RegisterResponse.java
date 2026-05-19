package com.group4.swissrouteapi.dtos.responses;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RegisterResponse {
  String name;
  String email;
  String baseCity;
  Instant createdAt;
}
