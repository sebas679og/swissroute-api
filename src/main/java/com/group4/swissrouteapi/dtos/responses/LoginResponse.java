package com.group4.swissrouteapi.dtos.responses;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/** LoginResponse Represents the data returned after a successful login. */
@Value
@Builder
public class LoginResponse {
    String token;
    String tokenType;
    long expiresIn;
    UUID userId;
}