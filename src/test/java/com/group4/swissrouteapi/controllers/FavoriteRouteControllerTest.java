package com.group4.swissrouteapi.controllers;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.swissrouteapi.AbstractIntegrationTest;
import com.group4.swissrouteapi.config.JwtKeyProvider;
import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.dtos.requests.FavoriteRouteRequest;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.providers.UserDataProvider;
import com.group4.swissrouteapi.repositories.FavoriteRouteRepository;
import com.group4.swissrouteapi.repositories.UserRepository;
import com.group4.swissrouteapi.utils.enums.TransportationType;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
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

/** Web integration tests for favorite routes controller. */
@DisplayName("Web integration tests for favorite routes controller.")
public class FavoriteRouteControllerTest extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private UserRepository userRepository;
  @Autowired private FavoriteRouteRepository favoriteRouteRepository;
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

  private FavoriteRouteRequest requestDefault() {
    return FavoriteRouteRequest.builder()
        .name("Home to work")
        .origin("Geneve")
        .destination("Zurich")
        .transportationType(TransportationType.TRAIN)
        .build();
  }

  @BeforeEach
  void setUp() {
    favoriteRouteRepository.deleteAll();
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

  @Nested
  @DisplayName("Save favorite routes")
  class SaveFavoriteRoutes {

    @Nested
    @DisplayName("Success cases")
    class SuccessCases {

      @Test
      void shouldReturn200OkWhenSavingFavoriteRoute() throws Exception {
        FavoriteRouteRequest request = requestDefault();
        mockMvc
            .perform(
                post(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value(request.getName()))
            .andExpect(jsonPath("$.origin").value(request.getOrigin()))
            .andExpect(jsonPath("$.destination").value(request.getDestination()))
            .andExpect(jsonPath("$.transportType").value(request.getTransportationType().name()));
      }

      @Test
      void shouldReturn200OkWhenTransportTypeIsOmitted() throws Exception {
        FavoriteRouteRequest request =
            FavoriteRouteRequest.builder()
                .name("Home to work")
                .origin("Geneve")
                .destination("Zurich")
                .build();
        mockMvc
            .perform(
                post(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value(request.getName()))
            .andExpect(jsonPath("$.origin").value(request.getOrigin()))
            .andExpect(jsonPath("$.destination").value(request.getDestination()))
            .andExpect(jsonPath("$.transportType", nullValue()));
      }

      @Test
      void shouldReturn200OkWhenTwoUsersRegisterTheSameRoute() throws Exception {
        FavoriteRouteRequest request = requestDefault();

        mockMvc
            .perform(
                post(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value(request.getName()));

        UserEntity anotherUser = userRepository.save(UserDataProvider.createAnotherMockUser());

        mockMvc
            .perform(
                post(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + generateToken(anotherUser))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value(request.getName()));
      }
    }

    @Nested
    @DisplayName("Security")
    class Security {

      @Test
      void shouldReturn401UnauthorizedWhenTokenIsExpired() throws Exception {
        mockMvc
            .perform(
                post(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
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
                post(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
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
                post(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
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
                post(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
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
    @DisplayName("request validation")
    class RequestValidation {

      @Test
      void shouldReturn400BadRequestWhenRequestBodyIsMissing() throws Exception {
        mockMvc
            .perform(
                post(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenNameIsMissing() throws Exception {

        String request =
            """
                        {
                          "origin": "Geneve",
                          "destination": "Zurich",
                          "transportationType": "TRAIN"
                        }
                        """;

        mockMvc
            .perform(
                post(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
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
      void shouldReturn400BadRequestWhenOriginIsMissing() throws Exception {

        String request =
            """
                        {
                          "name": "Home to work",
                          "destination": "Zurich",
                          "transportationType": "TRAIN"
                        }
                        """;

        mockMvc
            .perform(
                post(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
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
      void shouldReturn400BadRequestWhenDestinationIsMissing() throws Exception {

        String request =
            """
                        {
                          "name": "Home to work",
                          "origin": "Geneve",
                          "transportationType": "TRAIN"
                        }
                        """;

        mockMvc
            .perform(
                post(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
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
      void shouldReturn400BadRequestWhenTransportationTypeIsInvalid() throws Exception {

        String request =
            """
                        {
                          "name": "Home to work",
                          "origin": "Geneve",
                          "destination": "Zurich",
                          "transportationType": "PLANE"
                        }
                        """;

        mockMvc
            .perform(
                post(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }
    }
  }
}
