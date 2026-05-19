package com.group4.swissrouteapi.controllers;

import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.dtos.requests.RegisterRequest;
import com.group4.swissrouteapi.dtos.responses.RegisterResponse;
import com.group4.swissrouteapi.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping(ApiPaths.Auth.REGISTER)
  public ResponseEntity<RegisterResponse> registerUser(
      @RequestBody @Valid RegisterRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(request));
  }
}
