package com.group4.swissrouteapi.controllers;

import com.group4.swissrouteapi.AbstractIntegrationTest;
import com.group4.swissrouteapi.UserDataProvider;
import com.group4.swissrouteapi.dtos.requests.LoginRequest;
import com.group4.swissrouteapi.dtos.responses.auth.LoginResponse;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.repositories.UserRepository;
import com.group4.swissrouteapi.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

/** Integration tests for ConnectionController. */
@DisplayName("Integration tests for ConnectionController")
public class ConnectionController extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private UserRepository userRepository;
  @Autowired private AuthService authService;

  private static final String TYPE_TOKEN = "Bearer ";

  private String token;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
    UserEntity rawUser =
        UserEntity.builder()
            .name(UserDataProvider.VALID_NAME)
            .email(UserDataProvider.VALID_EMAIL)
            .password(passwordEncoder.encode(UserDataProvider.VALID_PASSWORD))
            .baseCity(UserDataProvider.VALID_BASE_CITY)
            .build();
    userRepository.save(rawUser);

    LoginRequest loginRequest =
        LoginRequest.builder()
            .email(UserDataProvider.VALID_EMAIL)
            .password(UserDataProvider.VALID_PASSWORD)
            .build();
    LoginResponse loginResponse = authService.loginUser(loginRequest);
    token = loginResponse.getToken();
  }
}
