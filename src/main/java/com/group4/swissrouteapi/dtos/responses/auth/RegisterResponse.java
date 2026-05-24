package com.group4.swissrouteapi.dtos.responses.auth;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

/**
 * RegisterResponse
 *
 * <p>Data transfer object representing the response returned after a successful user registration.
 *
 * <p>Contains basic user details such as name, email, and base city, along with the timestamp
 * indicating when the account was created.
 *
 * <p>Annotated with Lombok {@link Value} for immutability and {@link Builder} to support fluent
 * construction.
 */
@Value
@Builder
public class RegisterResponse {
  String name;
  String email;
  String baseCity;
  Instant createdAt;
}
