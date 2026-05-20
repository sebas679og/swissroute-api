package com.group4.swissrouteapi.controllers;

import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.dtos.requests.LoginRequest;
import com.group4.swissrouteapi.dtos.requests.RegisterRequest;
import com.group4.swissrouteapi.dtos.responses.ErrorResponse;
import com.group4.swissrouteapi.dtos.responses.LoginResponse;
import com.group4.swissrouteapi.dtos.responses.RegisterResponse;
import com.group4.swissrouteapi.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AuthController
 *
 * <p>REST controller responsible solely for handling user registration and authentication requests.
 *
 * <p>Annotated with {@link RestController} to expose endpoints as RESTful services, {@link
 * RequestMapping} for base path configuration, and {@link RequiredArgsConstructor} to enable
 * constructor-based dependency injection.
 *
 * <p>Tagged with {@link io.swagger.v3.oas.annotations.tags.Tag} for API documentation, grouping
 * authentication-related endpoints under the "Auth" category.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(
    name = "Auth",
    description = "Controller responsible for receiving user authentication requests.")
public class AuthController {

  private final AuthService authService;

  @Operation(
      summary = "Register a new user",
      description =
          """
                   Endpoint to register a new user account.
                   This endpoint accepts user registration data and creates
                   a new account if the provided information is valid and does
                   not conflict with existing accounts.
                   """)
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "User registration data. All fields are required.",
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = RegisterRequest.class)))
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description =
            "User registered successfully - Returns the details of the newly created user account.",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = RegisterResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Validation error - Invalid input fields",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Conflict - email already registered",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ErrorResponse.class))),
  })
  @PostMapping(ApiPaths.Auth.REGISTER)
  public ResponseEntity<RegisterResponse> registerUser(
      @RequestBody @Valid RegisterRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(request));
  }

  @PostMapping(ApiPaths.Auth.LOGIN)
    public ResponseEntity<LoginResponse> loginUser(@RequestBody @Valid LoginRequest request){
      return ResponseEntity.status(HttpStatus.OK).body(authService.loginUser(request));
  }
}
