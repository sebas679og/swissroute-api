package com.group4.swissrouteapi.controllers;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.group4.swissrouteapi.AbstractIntegrationTest;
import com.group4.swissrouteapi.UserDataProvider;
import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.dtos.requests.LoginRequest;
import com.group4.swissrouteapi.dtos.responses.auth.LoginResponse;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.repositories.UserRepository;
import com.group4.swissrouteapi.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

/** Integration tests for ConnectionController. */
@DisplayName("Integration tests for ConnectionController")
public class ConnectionControllerTest extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private UserRepository userRepository;
  @Autowired private AuthService authService;

  private static final String TYPE_TOKEN = "Bearer ";
  private static final String FROM = "Lausanne";
  private static final String TO = "Geneve";

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

  @Nested
  @DisplayName("Succesful Response")
  class SuccessfulResponse {

    @Test
    void shouldReturn200OkWhenOnlyRequiredFieldsAreProvided() throws Exception {
      connectionsStub.stubConnectionsByFromAndTo(FROM, TO);

      mockMvc
          .perform(
              get(ApiPaths.Connection.CONNECTIONS)
                  .param("from", FROM)
                  .param("to", TO)
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.connections").isArray())
          .andExpect(jsonPath("$.connections", hasSize(greaterThan(0))));
    }
  }
}
