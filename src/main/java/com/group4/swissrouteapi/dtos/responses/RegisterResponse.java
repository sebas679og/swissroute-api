package com.group4.swissrouteapi.dtos.responses;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class RegisterResponse {
    String name;
    String email;
    String baseCity;
    Instant createdAt;
}
