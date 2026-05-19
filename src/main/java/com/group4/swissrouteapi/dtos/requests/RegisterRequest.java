package com.group4.swissrouteapi.dtos.requests;

import com.group4.swissrouteapi.utils.validators.passwords.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * RegisterRequest
 *
 * <p>Data transfer object representing the payload required to register a new user in the system.
 *
 * <p>Contains user details such as name, email, password, and base city. Validation annotations
 * ensure that all fields are provided and meet the required format and constraints:
 *
 * <ul>
 *   <li>{@code @NotBlank} enforces non-empty values.
 *   <li>{@code @Email} validates proper email format.
 *   <li>{@code @ValidPassword} applies custom password rules.
 * </ul>
 *
 * <p>Annotated with Lombok {@link Getter}, {@link Setter}, and {@link Builder} to generate
 * boilerplate code and support fluent object construction.
 */
@Getter
@Setter
@Builder
public class RegisterRequest {

  @NotBlank(message = "Name is required")
  private String name;

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  private String email;

  @NotBlank(message = "Password is required")
  @ValidPassword
  private String password;

  @NotBlank(message = "Base city is required")
  private String baseCity;
}
