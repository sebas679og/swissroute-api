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
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@DisplayName("StationController Integration Tests")
class StationControllerTest extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private UserRepository userRepository;
  @Autowired private AuthService authService;
  @Autowired private JwtKeyProvider provider;

  private static final String TYPE_TOKEN = "Bearer ";
  private static final double LATITUDE = 47.377921;
  private static final double LONGITUDE = 8.540192;

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
  @DisplayName("successful responses")
  class SuccessTest {

    // -------------------------------------------------------------------------
    // Successful responses by Query
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Get stations using query parameter")
    class GetStationsUsingQueryParameter {

      @Test
      void shouldReturnStationsWhenSearchingByName() throws Exception {
        transportsStub.stubLocationsByQuery("Basel");
        mockMvc
            .perform(
                get(ApiPaths.Station.STATIONS)
                    .param("query", "Basel")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stations").isArray())
            .andExpect(jsonPath("$.stations", hasSize(greaterThan(0))));
      }

      @Test
      void shouldReturnEmptyListWhenStationNotFound() throws Exception {
        transportsStub.stubLocationsByQueryNotFound("sagmade");
        mockMvc
            .perform(
                get(ApiPaths.Station.STATIONS)
                    .param("query", "sagmade")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.NOT_FOUND.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }
    }

    // -------------------------------------------------------------------------
    // Successful responses by Query
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Get Stations using coordinates query")
    class GetStationsUsingCoordinatesQuery {

      @Test
      void shouldReturnStationsWhenSearchingByCoordinates() throws Exception {
        transportsStub.stubLocationsByCoordinates(LATITUDE, LONGITUDE);
        mockMvc
            .perform(
                get(ApiPaths.Station.STATIONS)
                    .param("latitude", String.valueOf(LATITUDE))
                    .param("longitude", String.valueOf(LONGITUDE))
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.stations").isArray())
            .andExpect(jsonPath("$.stations", hasSize(greaterThan(0))))
            .andExpect(jsonPath("$.stations[0].distance").exists());
      }
    }

    // -------------------------------------------------------------------------
    // Transport API errors
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("transport API errors")
    class TransportApiErrorTest {

      @Test
      void shouldThrowExceptionWhenApiReturns502InternalServerError() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.Station.STATIONS)
                    .param("query", "sagmade")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadGateway())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_GATEWAY.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_GATEWAY.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldThrowBadGatewayExceptionWhenApiReturns4xxClientError() throws Exception {
        transportsStub.stubLocationResponse4xx("Basel");
        mockMvc
            .perform(
                get(ApiPaths.Station.STATIONS)
                    .param("query", "Basel")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadGateway())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_GATEWAY.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_GATEWAY.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldThrowServiceUnavailableExceptionWhenApiReturns5xxServerError() throws Exception {
        transportsStub.stubLocationResponse5xx("Basel");
        mockMvc
            .perform(
                get(ApiPaths.Station.STATIONS)
                    .param("query", "Basel")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.code").value(HttpStatus.SERVICE_UNAVAILABLE.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.SERVICE_UNAVAILABLE.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }
    }

    // -------------------------------------------------------------------------
    // Security
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("security")
    class SecurityTest {

      @Test
      void shouldReturn401UnauthorizedWhenTokenIsExpired() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.Station.STATIONS)
                    .param("query", "Basel")
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
                get(ApiPaths.Station.STATIONS)
                    .param("query", "Basel")
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
            .perform(get(ApiPaths.Station.STATIONS).param("query", "Basel"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.UNAUTHORIZED.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }
    }

    // -------------------------------------------------------------------------
    // Request validation
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("request validation")
    class RequestValidationTest {

      @Test
      void shouldReturn400BadRequestWhenParameterIsMissing() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.Station.STATIONS)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenQueryParameterIsEmpty() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.Station.STATIONS)
                    .param("query", "")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenLatitudeParameterIsEmpty() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.Station.STATIONS)
                    .param("latitude", "")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenLongitudeParameterIsEmpty() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.Station.STATIONS)
                    .param("longitude", "")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenLongitudeIsMissing() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.Station.STATIONS)
                    .param("latitude", String.valueOf(LATITUDE))
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenLatitudeIsMissing() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.Station.STATIONS)
                    .param("longitude", String.valueOf(LONGITUDE))
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenLongitudeIsLessThanMinimum() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.Station.STATIONS)
                    .param("latitude", String.valueOf(LATITUDE))
                    .param("longitude", String.valueOf(-190.562))
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenLatitudeIsLessThanMinimum() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.Station.STATIONS)
                    .param("latitude", String.valueOf(-100.562))
                    .param("longitude", String.valueOf(LONGITUDE))
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenLongitudeExceedsMaximum() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.Station.STATIONS)
                    .param("latitude", String.valueOf(LATITUDE))
                    .param("longitude", String.valueOf(190.562))
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenLatitudeExceedsMaximum() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.Station.STATIONS)
                    .param("latitude", String.valueOf(100.562))
                    .param("longitude", String.valueOf(LONGITUDE))
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenQueryIsSentAlongsideLatitudeAndLongitude()
          throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.Station.STATIONS)
                    .param("query", "Basel")
                    .param("latitude", String.valueOf(LONGITUDE))
                    .param("longitude", String.valueOf(LONGITUDE))
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }
    }
  }
}
