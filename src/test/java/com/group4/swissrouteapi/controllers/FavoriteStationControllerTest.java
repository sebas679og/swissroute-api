package com.group4.swissrouteapi.controllers;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.swissrouteapi.AbstractIntegrationTest;
import com.group4.swissrouteapi.config.JwtKeyProvider;
import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.dtos.requests.StationRequest;
import com.group4.swissrouteapi.models.FavoriteStationEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.providers.FavoriteStationMockFactory;
import com.group4.swissrouteapi.providers.UserDataProvider;
import com.group4.swissrouteapi.repositories.FavoriteStationsRepository;
import com.group4.swissrouteapi.repositories.UserRepository;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

/** Web integration tests for favorite stations controller. */
@DisplayName("Web integration tests for favorite stations controller.")
public class FavoriteStationControllerTest extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private UserRepository userRepository;
  @Autowired private FavoriteStationsRepository favoriteStationRepository;
  @Autowired private JwtKeyProvider provider;

  private static final String TYPE_TOKEN = "Bearer ";

  private String token;
  private UserEntity user;

  private String generateToken(UserEntity user) {
    Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    return Jwts.builder()
        .subject(user.getId().toString())
        .claim("email", user.getEmail())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(20)))
        .signWith(provider.getSigningKey())
        .compact();
  }

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

  private String generateTokenWithOtherUser() {
    Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    return Jwts.builder()
        .subject(UUID.randomUUID().toString())
        .claim("email", user.getEmail())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(20)))
        .signWith(provider.getSigningKey())
        .compact();
  }

  private StationRequest requestDefault() {
    return StationRequest.builder().externalStationId("8503000").stationName("Zürich HB").build();
  }

  @BeforeEach
  void setUp() {
    favoriteStationRepository.deleteAll();
    userRepository.deleteAll();
    UserEntity rawUser =
        UserEntity.builder()
            .name(UserDataProvider.VALID_NAME)
            .email(UserDataProvider.VALID_EMAIL)
            .password(passwordEncoder.encode(UserDataProvider.VALID_PASSWORD))
            .baseCity(UserDataProvider.VALID_BASE_CITY)
            .build();
    user = userRepository.save(rawUser);
    token = generateToken(user);
  }

  // ──────────────────────────────────────────────────────────────────────────
  // POST  – Add favorite station
  // ──────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Add favorite station")
  class AddFavoriteStation {

    @Nested
    @DisplayName("Success cases")
    class SuccessCases {

      @Test
      void shouldReturn201CreatedWhenAddingFavoriteStation() throws Exception {
        StationRequest request = requestDefault();
        mockMvc
            .perform(
                post(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.externalStationId").value(request.getExternalStationId()))
            .andExpect(jsonPath("$.stationName").value(request.getStationName()))
            .andExpect(jsonPath("$.createdAt").exists());
      }

      @Test
      void shouldReturn201WhenTwoUsersRegisterTheSameStation() throws Exception {
        StationRequest request = requestDefault();

        mockMvc
            .perform(
                post(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.externalStationId").value(request.getExternalStationId()));

        UserEntity anotherUser = userRepository.save(UserDataProvider.createAnotherMockUser());

        mockMvc
            .perform(
                post(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + generateToken(anotherUser))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.externalStationId").value(request.getExternalStationId()));
      }
    }

    @Nested
    @DisplayName("Security")
    class Security {

      @Test
      void shouldReturn401UnauthorizedWhenTokenIsExpired() throws Exception {
        mockMvc
            .perform(
                post(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + generateExpiredToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDefault())))
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
                post(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + "malformed.token.here")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDefault())))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.UNAUTHORIZED.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn401UnauthorizedWhenAuthorizationHeaderIsMissing() throws Exception {
        mockMvc
            .perform(
                post(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDefault())))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.UNAUTHORIZED.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn404NotFoundWhenUserFromTamperedJwtDoesNotExist() throws Exception {
        mockMvc
            .perform(
                post(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + generateTokenWithOtherUser())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDefault())))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.NOT_FOUND.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }
    }

    @Nested
    @DisplayName("Request validation")
    class RequestValidation {

      @Test
      void shouldReturn400BadRequestWhenRequestBodyIsMissing() throws Exception {
        mockMvc
            .perform(
                post(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenExternalStationIdIsMissing() throws Exception {
        String request =
            """
                        {
                          "stationName": "Zürich HB"
                        }
                        """;
        mockMvc
            .perform(
                post(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenExternalStationIdIsBlank() throws Exception {
        String request =
            """
                        {
                          "externalStationId": "   ",
                          "stationName": "Zürich HB"
                        }
                        """;
        mockMvc
            .perform(
                post(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenStationNameIsMissing() throws Exception {
        String request =
            """
                        {
                          "externalStationId": "8503000"
                        }
                        """;
        mockMvc
            .perform(
                post(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenStationNameIsBlank() throws Exception {
        String request =
            """
                        {
                          "externalStationId": "8503000",
                          "stationName": "   "
                        }
                        """;
        mockMvc
            .perform(
                post(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn409ConflictWhenSameUserRegistersTheSameStationTwice() throws Exception {
        StationRequest request = requestDefault();

        mockMvc
            .perform(
                post(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc
            .perform(
                post(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(HttpStatus.CONFLICT.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.CONFLICT.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }
    }
  }

  // ──────────────────────────────────────────────────────────────────────────
  // GET  – Get favorite stations
  // ──────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Get favorite stations")
  class GetFavoriteStations {

    private static final Integer DEFAULT_NUMBER_OF_STATIONS = 5;

    @BeforeEach
    void setUp() {
      favoriteStationRepository.deleteAll();
      List<FavoriteStationEntity> rawStations =
          FavoriteStationMockFactory.createMockFavoriteStations(user, DEFAULT_NUMBER_OF_STATIONS);
      favoriteStationRepository.saveAll(rawStations);
    }

    @Nested
    @DisplayName("Success cases")
    class SuccessCases {

      @Test
      void shouldReturn200OkWithAllFavoriteStations() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.favoriteStations").isArray())
            .andExpect(jsonPath("$.favoriteStations", hasSize(DEFAULT_NUMBER_OF_STATIONS)));
      }

      @Test
      void shouldReturn200OkAndValidateRequiredFieldsPresence() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.favoriteStations").isArray())
            .andExpect(jsonPath("$.favoriteStations[0].externalStationId").exists())
            .andExpect(jsonPath("$.favoriteStations[0].stationName").exists())
            .andExpect(jsonPath("$.favoriteStations[0].createdAt").exists());
      }

      @Test
      void shouldReturn200OkWithEmptyListWhenUserHasNoFavoriteStations() throws Exception {
        favoriteStationRepository.deleteAll();
        mockMvc
            .perform(
                get(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.favoriteStations").isArray())
            .andExpect(jsonPath("$.favoriteStations").isEmpty());
      }

      @Test
      void shouldReturnUserStationsAndEmptyListForDifferentUser() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.favoriteStations", hasSize(DEFAULT_NUMBER_OF_STATIONS)));

        UserEntity newUser = userRepository.save(UserDataProvider.createAnotherMockUser());
        mockMvc
            .perform(
                get(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + generateToken(newUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.favoriteStations").isArray())
            .andExpect(jsonPath("$.favoriteStations").isEmpty());
      }
    }

    @Nested
    @DisplayName("Security")
    class Security {

      @Test
      void shouldReturn401UnauthorizedWhenTokenIsExpired() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
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
                get(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
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
            .perform(get(ApiPaths.FavoriteStations.FAVORITE_STATIONS))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.UNAUTHORIZED.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn404NotFoundWhenUserFromTamperedJwtDoesNotExist() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + generateTokenWithOtherUser()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.NOT_FOUND.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }
    }
  }

  // ──────────────────────────────────────────────────────────────────────────
  // DELETE  – Remove favorite station
  // ──────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Delete favorite station")
  class DeleteFavoriteStation {

    private static final Integer DEFAULT_NUMBER_OF_STATIONS = 5;
    private List<FavoriteStationEntity> favoriteStations;

    @BeforeEach
    void setUp() {
      favoriteStationRepository.deleteAll();
      List<FavoriteStationEntity> rawStations =
          FavoriteStationMockFactory.createMockFavoriteStations(user, DEFAULT_NUMBER_OF_STATIONS);
      favoriteStations = favoriteStationRepository.saveAll(rawStations);
    }

    @Nested
    @DisplayName("Success cases")
    class SuccessCases {

      @Test
      void shouldReturn204NoContentWhenFavoriteStationIsDeleted() throws Exception {
        String externalStationId = favoriteStations.getFirst().getExternalStationId();

        mockMvc
            .perform(
                delete(ApiPaths.FavoriteStations.FAVORITE_STATION, externalStationId)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));
      }

      @Test
      void shouldDeleteStationsAndIdsShouldNotExistInResponse() throws Exception {
        String firstId = favoriteStations.getFirst().getExternalStationId();
        String secondId = favoriteStations.get(1).getExternalStationId();

        mockMvc
            .perform(
                delete(ApiPaths.FavoriteStations.FAVORITE_STATION, firstId)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isNoContent());

        mockMvc
            .perform(
                delete(ApiPaths.FavoriteStations.FAVORITE_STATION, secondId)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isNoContent());

        mockMvc
            .perform(
                get(ApiPaths.FavoriteStations.FAVORITE_STATIONS)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.favoriteStations", hasSize(DEFAULT_NUMBER_OF_STATIONS - 2)))
            .andExpect(jsonPath("$.favoriteStations[*].externalStationId", not(hasItem(firstId))))
            .andExpect(jsonPath("$.favoriteStations[*].externalStationId", not(hasItem(secondId))));
      }
    }

    @Nested
    @DisplayName("Security")
    class Security {

      @Test
      void shouldReturn401UnauthorizedWhenTokenIsExpired() throws Exception {
        mockMvc
            .perform(
                delete(
                        ApiPaths.FavoriteStations.FAVORITE_STATION,
                        favoriteStations.getFirst().getExternalStationId())
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
                delete(
                        ApiPaths.FavoriteStations.FAVORITE_STATION,
                        favoriteStations.getFirst().getExternalStationId())
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
            .perform(
                delete(
                    ApiPaths.FavoriteStations.FAVORITE_STATION,
                    favoriteStations.getFirst().getExternalStationId()))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.UNAUTHORIZED.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn404NotFoundWhenUserFromTamperedJwtDoesNotExist() throws Exception {
        mockMvc
            .perform(
                delete(
                        ApiPaths.FavoriteStations.FAVORITE_STATION,
                        favoriteStations.getFirst().getExternalStationId())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + generateTokenWithOtherUser()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.NOT_FOUND.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }
    }

    @Nested
    @DisplayName("Request validation")
    class RequestValidation {

      @Test
      void shouldReturn404NotFoundWhenFavoriteStationDoesNotExist() throws Exception {
        mockMvc
            .perform(
                delete(ApiPaths.FavoriteStations.FAVORITE_STATION, "nonexistent-station-id")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.NOT_FOUND.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn404NotFoundWhenStationBelongsToAnotherUser() throws Exception {
        // The station exists in DB but belongs to `user`, not to the random subject in the token.
        mockMvc
            .perform(
                delete(
                        ApiPaths.FavoriteStations.FAVORITE_STATION,
                        favoriteStations.getFirst().getExternalStationId())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + generateTokenWithOtherUser()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.NOT_FOUND.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }
    }
  }
}
