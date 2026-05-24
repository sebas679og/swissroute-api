package com.group4.swissrouteapi.controllers;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.group4.swissrouteapi.AbstractIntegrationTest;
import com.group4.swissrouteapi.UserDataProvider;
import com.group4.swissrouteapi.config.JwtKeyProvider;
import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.dtos.requests.LoginRequest;
import com.group4.swissrouteapi.dtos.responses.auth.LoginResponse;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.repositories.UserRepository;
import com.group4.swissrouteapi.services.AuthService;
import com.group4.swissrouteapi.utils.enums.TransportationType;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

/** Integration tests for ConnectionController. */
@DisplayName("Integration tests for ConnectionController")
public class ConnectionControllerTest extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private UserRepository userRepository;
  @Autowired private AuthService authService;
  @Autowired private JwtKeyProvider provider;

  private static final String TYPE_TOKEN = "Bearer ";
  private static final String FROM = "Lausanne";
  private static final String TO = "Genève";

  private String token;
  private UserEntity user;

  private String generateExpiredToken() {
    Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    return Jwts.builder()
        .subject(user.getId().toString())
        .claim("email", user.getEmail())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.minus(1, ChronoUnit.MINUTES)))
        .signWith(provider.getSigningKey())
        .compact();
  }

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
    user = userRepository.save(rawUser);

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

    @Test
    void shouldReturn200OkWhenOptionalDateFieldIsProvided() throws Exception {
      String date = LocalDate.now().toString();
      connectionsStub.stubConnectionsByDate(FROM, TO, date);

      mockMvc
          .perform(
              get(ApiPaths.Connection.CONNECTIONS)
                  .param("from", FROM)
                  .param("to", TO)
                  .param("date", date)
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.connections").isArray())
          .andExpect(jsonPath("$.connections", hasSize(greaterThan(0))));
    }

    @Test
    void shouldReturn200OkWhenTimeFieldsAreProvided() throws Exception {
      String time = LocalTime.now().toString();
      connectionsStub.stubConnectionsByTime(FROM, TO, time);

      mockMvc
          .perform(
              get(ApiPaths.Connection.CONNECTIONS)
                  .param("from", FROM)
                  .param("to", TO)
                  .param("time", time)
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.connections").isArray())
          .andExpect(jsonPath("$.connections", hasSize(greaterThan(0))));
    }

    @Test
    void shouldReturn200OkWhenDateAndTimeFieldsAreProvided() throws Exception {
      String date = LocalDate.now().toString();
      String time = LocalTime.now().toString();
      connectionsStub.stubConnectionsByDateAndTime(FROM, TO, date, time);

      mockMvc
          .perform(
              get(ApiPaths.Connection.CONNECTIONS)
                  .param("from", FROM)
                  .param("to", TO)
                  .param("date", date)
                  .param("time", time)
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.connections").isArray())
          .andExpect(jsonPath("$.connections", hasSize(greaterThan(0))));
    }

    @Test
    void shouldGetConnectionsSuccessfullyWithFullQueryParams() throws Exception {
      String date = LocalDate.now().toString();
      String time = LocalTime.now().toString();
      String transportations = TransportationType.TRAIN.name().toLowerCase(Locale.ROOT);
      connectionsStub.stubConnectionsByDateAndTimeAndTransportations(
          FROM, TO, date, time, transportations);

      mockMvc
          .perform(
              get(ApiPaths.Connection.CONNECTIONS)
                  .param("from", FROM)
                  .param("to", TO)
                  .param("date", date)
                  .param("time", time)
                  .param("transportations", transportations)
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.connections").isArray())
          .andExpect(jsonPath("$.connections", hasSize(greaterThan(0))));
    }

    @Test
    void shouldReturn200OkWhenMultipleTransportationTypesAreProvided() throws Exception {
      String date = LocalDate.now().toString();
      String time = LocalTime.now().toString();
      String transportations1 = TransportationType.TRAIN.name().toLowerCase(Locale.ROOT);
      String transportations2 = TransportationType.BUS.name().toLowerCase(Locale.ROOT);
      connectionsStub.stubConnectionsByDateAndTimeAndTransportations(
          FROM, TO, date, time, List.of(transportations1, transportations2));

      mockMvc
          .perform(
              get(ApiPaths.Connection.CONNECTIONS)
                  .param("from", FROM)
                  .param("to", TO)
                  .param("date", date)
                  .param("time", time)
                  .param("transportations", transportations1)
                  .param("transportations", transportations2)
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.connections").isArray())
          .andExpect(jsonPath("$.connections", hasSize(greaterThan(0))));
    }

    @Test
    void shouldFailWith404WhenTransportClientReturnsNoConnections() throws Exception {
      connectionsStub.stubConnectionsNotFound(FROM, TO);

      mockMvc
          .perform(
              get(ApiPaths.Connection.CONNECTIONS)
                  .param("from", FROM)
                  .param("to", TO)
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
          .andExpect(jsonPath("$.name").value(HttpStatus.NOT_FOUND.name()))
          .andExpect(jsonPath("$.description").exists())
          .andExpect(jsonPath("$.timestamp").exists());
    }
  }

  @Nested
  @DisplayName("security")
  class SecurityTest {

    @Test
    void shouldReturn401UnauthorizedWhenTokenIsExpired() throws Exception {
      mockMvc
          .perform(
              get(ApiPaths.Connection.CONNECTIONS)
                  .param("from", FROM)
                  .param("to", TO)
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + generateExpiredToken()))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
          .andExpect(jsonPath("$.name").value(HttpStatus.UNAUTHORIZED.name()))
          .andExpect(jsonPath("$.description").exists())
          .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn401UnauthorizedWhenTokenIsMalformed() throws Exception {
      mockMvc
          .perform(
              get(ApiPaths.Connection.CONNECTIONS)
                  .param("from", FROM)
                  .param("to", TO)
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + "malformed.token.here"))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
          .andExpect(jsonPath("$.name").value(HttpStatus.UNAUTHORIZED.name()))
          .andExpect(jsonPath("$.description").exists())
          .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn401UnauthorizedWhenAuthorizationHeaderIsMissing() throws Exception {
      mockMvc
          .perform(get(ApiPaths.Connection.CONNECTIONS).param("from", FROM).param("to", TO))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
          .andExpect(jsonPath("$.name").value(HttpStatus.UNAUTHORIZED.name()))
          .andExpect(jsonPath("$.description").exists())
          .andExpect(jsonPath("$.timestamp").exists());
    }
  }

  @Nested
  @DisplayName("request validation")
  class RequestValidationTest {

    @Test
    void shouldReturn400BadRequestWhenRequiredFieldsAreMissing() throws Exception {
      mockMvc
          .perform(
              get(ApiPaths.Connection.CONNECTIONS)
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
          .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
          .andExpect(jsonPath("$.description").exists())
          .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn400BadRequestWhenToParamIsBlank() throws Exception {
      mockMvc
          .perform(
              get(ApiPaths.Connection.CONNECTIONS)
                  .param("from", FROM)
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
          .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
          .andExpect(jsonPath("$.description").exists())
          .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn400BadRequestWhenFromParamIsBlank() throws Exception {
      mockMvc
          .perform(
              get(ApiPaths.Connection.CONNECTIONS)
                  .param("to", TO)
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
          .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
          .andExpect(jsonPath("$.description").exists())
          .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn400BadRequestWhenDateFieldIsMalformed() throws Exception {
      mockMvc
          .perform(
              get(ApiPaths.Connection.CONNECTIONS)
                  .param("from", FROM)
                  .param("to", TO)
                  .param("date", "25-05-2026") // wait format (YYYY-MM-DD)
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
          .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
          .andExpect(jsonPath("$.description").exists())
          .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn400BadRequestWhenTimeFieldIsMalformed() throws Exception {
      mockMvc
          .perform(
              get(ApiPaths.Connection.CONNECTIONS)
                  .param("from", FROM)
                  .param("to", TO)
                  .param("time", "23-18") // wait format (HH:MM)
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
          .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
          .andExpect(jsonPath("$.description").exists())
          .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn400BadRequestWhenTransportationTypeIsInvalid() throws Exception {
      mockMvc
          .perform(
              get(ApiPaths.Connection.CONNECTIONS)
                  .param("from", FROM)
                  .param("to", TO)
                  .param("transportations", "rocket") // "rocket" is not a valid transportation type
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
          .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
          .andExpect(jsonPath("$.description").exists())
          .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn400BadRequestWhenOneTransportationTypeIsValidAndAnotherIsInvalid()
        throws Exception {
      mockMvc
          .perform(
              get(ApiPaths.Connection.CONNECTIONS)
                  .param("from", FROM)
                  .param("to", TO)
                  // "train" is valid, but "rocket" is not, so the whole request should be rejected
                  // with 400 Bad Request
                  .param(
                      "transportations", TransportationType.TRAIN.name().toLowerCase(Locale.ROOT))
                  .param("transportations", "rocket")
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
          .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
          .andExpect(jsonPath("$.description").exists())
          .andExpect(jsonPath("$.timestamp").exists());
    }
  }
}
