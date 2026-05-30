package com.group4.swissrouteapi.controllers;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.group4.swissrouteapi.AbstractIntegrationTest;
import com.group4.swissrouteapi.config.JwtKeyProvider;
import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.providers.UserDataProvider;
import com.group4.swissrouteapi.repositories.UserRepository;
import com.group4.swissrouteapi.utils.enums.TransportType;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
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

/** Integration tests for StationBoardController. */
@DisplayName("Integration tests for StationBoardController")
public class StationBoardControllerTest extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private UserRepository userRepository;
  @Autowired private JwtKeyProvider provider;

  private static final String TYPE_TOKEN = "Bearer ";
  private static final String STATION = "Aarau";
  private static final String STATION_ID = "8502113";

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

    token =
        Jwts.builder()
            .subject(user.getId().toString())
            .claim("email", user.getEmail())
            .issuedAt(Date.from(Instant.now().truncatedTo(ChronoUnit.MILLIS)))
            .expiration(Date.from(Instant.now().plusSeconds(20)))
            .signWith(provider.getSigningKey())
            .compact();
  }

  // ──────────────────────────────────────────────────────────────────────────
  // Success cases
  // ──────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Successful response")
  class SuccessfulResponse {

    @Test
    void shouldReturn200OkWhenOnlyRequiredFieldIsProvided() throws Exception {
      stationsBoardStub.stubStationsBoardByStation(STATION);

      mockMvc
          .perform(
              get(ApiPaths.StationBoard.STATION_BOARD)
                  .param("station", STATION)
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.stationBoards").isArray())
          .andExpect(jsonPath("$.stationBoards", hasSize(greaterThan(0))));
    }

    @Test
    void shouldReturn200OkWhenOptionalIdIsProvided() throws Exception {
      stationsBoardStub.stubStationsBoardById(STATION, STATION_ID);

      mockMvc
          .perform(
              get(ApiPaths.StationBoard.STATION_BOARD)
                  .param("station", STATION)
                  .param("id", STATION_ID)
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.stationBoards").isArray())
          .andExpect(jsonPath("$.stationBoards", hasSize(greaterThan(0))));
    }

    @Test
    void shouldReturn200OkWhenOptionalLimitIsProvided() throws Exception {
      String limit = "15";
      stationsBoardStub.stubStationsBoardByLimit(STATION, limit);

      mockMvc
          .perform(
              get(ApiPaths.StationBoard.STATION_BOARD)
                  .param("station", STATION)
                  .param("limit", limit)
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.stationBoards").isArray())
          .andExpect(jsonPath("$.stationBoards", hasSize(greaterThan(0))));
    }

    @Test
    void shouldReturn200OkWhenOneTransportationTypeIsProvided() throws Exception {
      TransportType transportation = TransportType.TRAIN;
      stationsBoardStub.stubStationsBoardByTransportations(
          STATION, transportation.name().toLowerCase(Locale.ROOT));

      mockMvc
          .perform(
              get(ApiPaths.StationBoard.STATION_BOARD)
                  .param("station", STATION)
                  .param("transportType", transportation.name().toLowerCase(Locale.ROOT))
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.stationBoards").isArray())
          .andExpect(jsonPath("$.stationBoards", hasSize(greaterThan(0))));
    }

    @Test
    void shouldReturn200OkWhenTwoTransportationTypesAreProvided() throws Exception {
      TransportType transportation1 = TransportType.TRAIN;
      TransportType transportation2 = TransportType.BUS;
      List<String> transportations =
          List.of(
              transportation1.name().toLowerCase(Locale.ROOT),
              transportation2.name().toLowerCase(Locale.ROOT));
      stationsBoardStub.stubStationsBoardByTwoTransportations(STATION, transportations);

      mockMvc
          .perform(
              get(ApiPaths.StationBoard.STATION_BOARD)
                  .param("station", STATION)
                  .param("transportType", transportations.get(0))
                  .param("transportType", transportations.get(1))
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.stationBoards").isArray())
          .andExpect(jsonPath("$.stationBoards", hasSize(greaterThan(0))));
    }

    @Test
    void shouldReturn200OkWhenAllFiltersAreProvided() throws Exception {
      String limit = "10";
      TransportType transportation1 = TransportType.TRAIN;
      TransportType transportation2 = TransportType.BUS;
      List<String> transportations =
          List.of(
              transportation1.name().toLowerCase(Locale.ROOT),
              transportation2.name().toLowerCase(Locale.ROOT));
      stationsBoardStub.stubStationsBoardAllFilters(STATION, STATION_ID, limit, transportations);

      mockMvc
          .perform(
              get(ApiPaths.StationBoard.STATION_BOARD)
                  .param("station", STATION)
                  .param("id", STATION_ID)
                  .param("limit", limit)
                  .param("transportType", transportations.get(0))
                  .param("transportType", transportations.get(1))
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.stationBoards").isArray())
          .andExpect(jsonPath("$.stationBoards", hasSize(greaterThan(0))));
    }

    @Test
    void shouldReturn200OkWhenLimitIsZero() throws Exception {
      String limit = "0";
      stationsBoardStub.stubStationsBoardByLimitIsZero(STATION, limit);

      mockMvc
          .perform(
              get(ApiPaths.StationBoard.STATION_BOARD)
                  .param("station", STATION)
                  .param("limit", limit)
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.stationBoards").isArray())
          .andExpect(jsonPath("$.stationBoards", hasSize(greaterThan(0))));
    }
  }

  // ──────────────────────────────────────────────────────────────────────────
  // External API – not found responses
  // ──────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("External API not found responses")
  class ExternalApiNotFound {

    @Test
    void shouldReturn404NotFoundWhenExternalApiReturnsNoResultsForStation() throws Exception {
      stationsBoardStub.stubStationsBoardByNotFound(STATION, STATION_ID);

      mockMvc
          .perform(
              get(ApiPaths.StationBoard.STATION_BOARD)
                  .param("station", STATION)
                  .param("id", STATION_ID)
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
          .andExpect(jsonPath("$.name").value(HttpStatus.NOT_FOUND.name()))
          .andExpect(jsonPath("$.description").exists())
          .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn404NotFoundWhenExternalApiReturnsNoResultsForTransportationType()
        throws Exception {
      TransportType transportation = TransportType.SHIP;
      stationsBoardStub.stubStationsBoardByTransportationsNotFound(
          STATION, transportation.name().toLowerCase(Locale.ROOT));

      mockMvc
          .perform(
              get(ApiPaths.StationBoard.STATION_BOARD)
                  .param("station", STATION)
                  .param("transportType", transportation.name().toLowerCase(Locale.ROOT))
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
          .andExpect(jsonPath("$.name").value(HttpStatus.NOT_FOUND.name()))
          .andExpect(jsonPath("$.description").exists())
          .andExpect(jsonPath("$.timestamp").exists());
    }
  }

  // ──────────────────────────────────────────────────────────────────────────
  // Security
  // ──────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Security")
  class Security {

    @Test
    void shouldReturn401UnauthorizedWhenTokenIsExpired() throws Exception {
      mockMvc
          .perform(
              get(ApiPaths.StationBoard.STATION_BOARD)
                  .param("station", STATION)
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
              get(ApiPaths.StationBoard.STATION_BOARD)
                  .param("station", STATION)
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
          .perform(get(ApiPaths.StationBoard.STATION_BOARD).param("station", STATION))
          .andExpect(status().isUnauthorized())
          .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
          .andExpect(jsonPath("$.name").value(HttpStatus.UNAUTHORIZED.name()))
          .andExpect(jsonPath("$.description").exists())
          .andExpect(jsonPath("$.timestamp").exists());
    }
  }

  // ──────────────────────────────────────────────────────────────────────────
  // Request validation
  // ──────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Request validation")
  class RequestValidation {

    @Test
    void shouldReturn400BadRequestWhenRequiredStationParamIsMissing() throws Exception {
      mockMvc
          .perform(
              get(ApiPaths.StationBoard.STATION_BOARD)
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
          .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
          .andExpect(jsonPath("$.description").exists())
          .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn400BadRequestWhenStationParamIsBlank() throws Exception {
      mockMvc
          .perform(
              get(ApiPaths.StationBoard.STATION_BOARD)
                  .param("station", "   ")
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
              get(ApiPaths.StationBoard.STATION_BOARD)
                  .param("station", STATION)
                  .param("transportType", "rocket") // not a valid TransportType
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
          .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
          .andExpect(jsonPath("$.description").exists())
          .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturn400BadRequestWhenOneTransportationIsValidAndAnotherIsInvalid()
        throws Exception {
      mockMvc
          .perform(
              get(ApiPaths.StationBoard.STATION_BOARD)
                  .param("station", STATION)
                  .param("transportType", "train") // valid
                  .param("transportType", "rocket") // invalid – whole request rejected
                  .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
          .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
          .andExpect(jsonPath("$.description").exists())
          .andExpect(jsonPath("$.timestamp").exists());
    }
  }
}
