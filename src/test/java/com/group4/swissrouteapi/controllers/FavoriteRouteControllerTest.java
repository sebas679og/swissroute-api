package com.group4.swissrouteapi.controllers;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.swissrouteapi.AbstractIntegrationTest;
import com.group4.swissrouteapi.config.JwtKeyProvider;
import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.dtos.requests.FavoriteRouteRequest;
import com.group4.swissrouteapi.dtos.requests.RouteUpdateRequest;
import com.group4.swissrouteapi.models.FavoriteRouteEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.providers.FavoriteRouteMockFactory;
import com.group4.swissrouteapi.providers.UserDataProvider;
import com.group4.swissrouteapi.repositories.FavoriteRouteRepository;
import com.group4.swissrouteapi.repositories.UserRepository;
import com.group4.swissrouteapi.utils.enums.TransportType;
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
        .transportType(TransportType.TRAIN)
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
            .andExpect(jsonPath("$.transportType").value(request.getTransportType().name()));
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
                                  "transportType": "TRAIN"
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
                                  "transportType": "TRAIN"
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
                                  "transportType": "TRAIN"
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
                                  "transportType": "PLANE"
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

  @Nested
  @DisplayName("Get user's favorite routes")
  class GetUserFavoriteRoutes {

    private static final Integer DEFAULT_NUMBER_OF_ROUTES = 100;

    @BeforeEach
    void setUp() {
      favoriteRouteRepository.deleteAll();

      List<FavoriteRouteEntity> rawFavoritesRoutes =
          FavoriteRouteMockFactory.createMockFavoriteRoutes(user, DEFAULT_NUMBER_OF_ROUTES);
      favoriteRouteRepository.saveAll(rawFavoritesRoutes);
    }

    @Nested
    @DisplayName("Success cases")
    class SuccessCases {

      @Test
      void shouldReturn200OkWhenGetUserFavoriteRoutes() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.favoriteRoutes").isArray())
            .andExpect(jsonPath("$.favoriteRoutes", hasSize(greaterThan(0))));
      }

      @Test
      void shouldReturn200OkAndAllRecords() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.favoriteRoutes").isArray())
            .andExpect(jsonPath("$.favoriteRoutes", hasSize(DEFAULT_NUMBER_OF_ROUTES)));
      }

      @Test
      void shouldReturn200OkAndValidateRequiredFieldsPresence() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.favoriteRoutes").isArray())
            .andExpect(jsonPath("$.favoriteRoutes[0].id").exists())
            .andExpect(jsonPath("$.favoriteRoutes[0].name").exists())
            .andExpect(jsonPath("$.favoriteRoutes[0].origin").exists())
            .andExpect(jsonPath("$.favoriteRoutes[0].destination").exists())
            .andExpect(jsonPath("$.favoriteRoutes[0].transportType").exists())
            .andExpect(jsonPath("$.favoriteRoutes[0].createdAt").exists());
      }

      @Test
      void shouldReturn200OkWithEmptyRoutesWhenNoRecordsExist() throws Exception {
        favoriteRouteRepository.deleteAll();
        mockMvc
            .perform(
                get(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.favoriteRoutes").isArray())
            .andExpect(jsonPath("$.favoriteRoutes").isEmpty());
      }

      @Test
      void shouldReturnUserRoutesAndEmptyListForDifferentUser() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.favoriteRoutes").isArray())
            .andExpect(jsonPath("$.favoriteRoutes", hasSize(DEFAULT_NUMBER_OF_ROUTES)));

        UserEntity newUser = userRepository.save(UserDataProvider.createAnotherMockUser());
        mockMvc
            .perform(
                get(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + generateToken(newUser))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.favoriteRoutes").isArray())
            .andExpect(jsonPath("$.favoriteRoutes").isEmpty());
      }
    }

    @Nested
    @DisplayName("Security")
    class Security {

      @Test
      void shouldReturn401UnauthorizedWhenTokenIsExpired() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + generateExpiredToken())
                    .contentType(MediaType.APPLICATION_JSON))
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
                get(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + "malformed.token.here")
                    .contentType(MediaType.APPLICATION_JSON))
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
                get(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
                    .contentType(MediaType.APPLICATION_JSON))
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
                get(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + generateTokenWithOtherUser())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.NOT_FOUND.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }
    }
  }

  @Nested
  @DisplayName("Update favorite route")
  class UpdateFavoriteRoute {

    private static final Integer DEFAULT_NUMBER_OF_ROUTES = 100;
    private List<FavoriteRouteEntity> favoriteRoutes;

    final RouteUpdateRequest routeUpdateRequest =
        RouteUpdateRequest.builder()
            .name("Test Route Favorite")
            .origin("Zurich")
            .destination("Basel")
            .transportType(TransportType.CABLEWAY)
            .build();

    @BeforeEach
    void setUp() {
      favoriteRouteRepository.deleteAll();

      List<FavoriteRouteEntity> rawFavoritesRoutes =
          FavoriteRouteMockFactory.createMockFavoriteRoutes(user, DEFAULT_NUMBER_OF_ROUTES);
      favoriteRoutes = favoriteRouteRepository.saveAll(rawFavoritesRoutes);
    }

    @Nested
    @DisplayName("Success cases")
    class SuccessCases {

      @Test
      void shouldReturn201WhenRouteIsUpdatedWithAllParams() throws Exception {
        FavoriteRouteEntity favoriteRoute = favoriteRoutes.getFirst();

        mockMvc
            .perform(
                put(ApiPaths.FavoriteRoutes.FAVORITE_ROUTE, favoriteRoute.getId().toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(routeUpdateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(favoriteRoute.getId().toString()))
            .andExpect(jsonPath("$.name").value(routeUpdateRequest.getName()))
            .andExpect(jsonPath("$.origin").value(routeUpdateRequest.getOrigin()))
            .andExpect(jsonPath("$.destination").value(routeUpdateRequest.getDestination()))
            .andExpect(
                jsonPath("$.transportType").value(routeUpdateRequest.getTransportType().name()))
            .andExpect(jsonPath("$.createdAt").value(favoriteRoute.getCreatedAt().toString()));
      }

      @Test
      void shouldReturn200OkWhenNameIsOmitted() throws Exception {
        FavoriteRouteEntity favoriteRoute = favoriteRoutes.getFirst();

        RouteUpdateRequest routeUpdateRequest =
            RouteUpdateRequest.builder()
                .origin("Zurich")
                .destination("Basel")
                .transportType(TransportType.CABLEWAY)
                .build();

        mockMvc
            .perform(
                put(ApiPaths.FavoriteRoutes.FAVORITE_ROUTE, favoriteRoute.getId().toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(routeUpdateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(favoriteRoute.getId().toString()))
            .andExpect(jsonPath("$.name").value(favoriteRoute.getName()))
            .andExpect(jsonPath("$.origin").value(routeUpdateRequest.getOrigin()))
            .andExpect(jsonPath("$.destination").value(routeUpdateRequest.getDestination()))
            .andExpect(
                jsonPath("$.transportType").value(routeUpdateRequest.getTransportType().name()))
            .andExpect(jsonPath("$.createdAt").value(favoriteRoute.getCreatedAt().toString()));
      }

      @Test
      void shouldReturn200OkWhenOriginIsOmitted() throws Exception {
        FavoriteRouteEntity favoriteRoute = favoriteRoutes.getFirst();

        RouteUpdateRequest routeUpdateRequest =
            RouteUpdateRequest.builder()
                .name("Test Route Favorite")
                .destination("Basel")
                .transportType(TransportType.CABLEWAY)
                .build();

        mockMvc
            .perform(
                put(ApiPaths.FavoriteRoutes.FAVORITE_ROUTE, favoriteRoute.getId().toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(routeUpdateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(favoriteRoute.getId().toString()))
            .andExpect(jsonPath("$.name").value(routeUpdateRequest.getName()))
            .andExpect(jsonPath("$.origin").value(favoriteRoute.getOrigin()))
            .andExpect(jsonPath("$.destination").value(routeUpdateRequest.getDestination()))
            .andExpect(
                jsonPath("$.transportType").value(routeUpdateRequest.getTransportType().name()))
            .andExpect(jsonPath("$.createdAt").value(favoriteRoute.getCreatedAt().toString()));
      }

      @Test
      void shouldReturn200OkWhenDestinationIsOmitted() throws Exception {
        FavoriteRouteEntity favoriteRoute = favoriteRoutes.getFirst();

        RouteUpdateRequest routeUpdateRequest =
            RouteUpdateRequest.builder()
                .name("Test Route Favorite")
                .origin("Zurich")
                .transportType(TransportType.CABLEWAY)
                .build();

        mockMvc
            .perform(
                put(ApiPaths.FavoriteRoutes.FAVORITE_ROUTE, favoriteRoute.getId().toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(routeUpdateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(favoriteRoute.getId().toString()))
            .andExpect(jsonPath("$.name").value(routeUpdateRequest.getName()))
            .andExpect(jsonPath("$.origin").value(routeUpdateRequest.getOrigin()))
            .andExpect(jsonPath("$.destination").value(favoriteRoute.getDestination()))
            .andExpect(
                jsonPath("$.transportType").value(routeUpdateRequest.getTransportType().name()))
            .andExpect(jsonPath("$.createdAt").value(favoriteRoute.getCreatedAt().toString()));
      }

      @Test
      void shouldReturn200OkWhenTransportTypeIsOmitted() throws Exception {
        FavoriteRouteEntity favoriteRoute = favoriteRoutes.getFirst();

        RouteUpdateRequest routeUpdateRequest =
            RouteUpdateRequest.builder()
                .name("Test Route Favorite")
                .origin("Zurich")
                .destination("Basel")
                .build();

        mockMvc
            .perform(
                put(ApiPaths.FavoriteRoutes.FAVORITE_ROUTE, favoriteRoute.getId().toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(routeUpdateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(favoriteRoute.getId().toString()))
            .andExpect(jsonPath("$.name").value(routeUpdateRequest.getName()))
            .andExpect(jsonPath("$.origin").value(routeUpdateRequest.getOrigin()))
            .andExpect(jsonPath("$.destination").value(routeUpdateRequest.getDestination()))
            .andExpect(jsonPath("$.transportType").value(favoriteRoute.getTransportType().name()))
            .andExpect(jsonPath("$.createdAt").value(favoriteRoute.getCreatedAt().toString()));
      }

      @Test
      void shouldReturn200OkWhenNameIsEqualToCurrentValue() throws Exception {
        FavoriteRouteEntity favoriteRoute = favoriteRoutes.getFirst();

        RouteUpdateRequest routeUpdateRequest =
            RouteUpdateRequest.builder()
                .name(favoriteRoute.getName())
                .origin("Zurich")
                .destination("Basel")
                .transportType(TransportType.CABLEWAY)
                .build();

        mockMvc
            .perform(
                put(ApiPaths.FavoriteRoutes.FAVORITE_ROUTE, favoriteRoute.getId().toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(routeUpdateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(favoriteRoute.getId().toString()))
            .andExpect(jsonPath("$.name").value(routeUpdateRequest.getName()))
            .andExpect(jsonPath("$.origin").value(routeUpdateRequest.getOrigin()))
            .andExpect(jsonPath("$.destination").value(routeUpdateRequest.getDestination()))
            .andExpect(
                jsonPath("$.transportType").value(routeUpdateRequest.getTransportType().name()))
            .andExpect(jsonPath("$.createdAt").value(favoriteRoute.getCreatedAt().toString()));
      }

      @Test
      void shouldReturn200OkWhenAllFieldsAreEqualToRegisteredData() throws Exception {
        FavoriteRouteEntity favoriteRoute = favoriteRoutes.getFirst();

        RouteUpdateRequest routeUpdateRequest =
            RouteUpdateRequest.builder()
                .name(favoriteRoute.getName())
                .origin(favoriteRoute.getOrigin())
                .destination(favoriteRoute.getDestination())
                .transportType(favoriteRoute.getTransportType())
                .build();

        mockMvc
            .perform(
                put(ApiPaths.FavoriteRoutes.FAVORITE_ROUTE, favoriteRoute.getId().toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(routeUpdateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(favoriteRoute.getId().toString()))
            .andExpect(jsonPath("$.name").value(routeUpdateRequest.getName()))
            .andExpect(jsonPath("$.origin").value(routeUpdateRequest.getOrigin()))
            .andExpect(jsonPath("$.destination").value(routeUpdateRequest.getDestination()))
            .andExpect(
                jsonPath("$.transportType").value(routeUpdateRequest.getTransportType().name()))
            .andExpect(jsonPath("$.createdAt").value(favoriteRoute.getCreatedAt().toString()));
      }
    }

    @Nested
    @DisplayName("Security")
    class Security {

      @Test
      void shouldReturn401UnauthorizedWhenTokenIsExpired() throws Exception {
        mockMvc
            .perform(
                put(
                        ApiPaths.FavoriteRoutes.FAVORITE_ROUTE,
                        favoriteRoutes.getFirst().getId().toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + generateExpiredToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(routeUpdateRequest)))
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
                put(
                        ApiPaths.FavoriteRoutes.FAVORITE_ROUTE,
                        favoriteRoutes.getFirst().getId().toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + "malformed.token.here")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(routeUpdateRequest)))
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
                put(
                        ApiPaths.FavoriteRoutes.FAVORITE_ROUTE,
                        favoriteRoutes.getFirst().getId().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(routeUpdateRequest)))
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
                put(
                        ApiPaths.FavoriteRoutes.FAVORITE_ROUTE,
                        favoriteRoutes.getFirst().getId().toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + generateTokenWithOtherUser())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(routeUpdateRequest)))
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
      void shouldReturn400BadRequestWhenRouteIdIsNotValidUuid() throws Exception {
        mockMvc
            .perform(
                put(ApiPaths.FavoriteRoutes.FAVORITE_ROUTE, "invalid-uuid")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(routeUpdateRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenRouteIdContainsSpecialCharacters() throws Exception {
        mockMvc
            .perform(
                put(ApiPaths.FavoriteRoutes.FAVORITE_ROUTE, "@#$%")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(routeUpdateRequest)))
            .andExpect(status().isBadRequest());
      }

      @Test
      void shouldReturn400BadRequestWhenRouteIdContainsOnlyNumbers() throws Exception {
        mockMvc
            .perform(
                put(ApiPaths.FavoriteRoutes.FAVORITE_ROUTE, "123456")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(routeUpdateRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenRouteIdHasInvalidUuidFormat() throws Exception {
        mockMvc
            .perform(
                put(ApiPaths.FavoriteRoutes.FAVORITE_ROUTE, "550e8400-e29b-41d4-a716")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(routeUpdateRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenRouteIdContainsSpaces() throws Exception {
        mockMvc
            .perform(
                put(ApiPaths.FavoriteRoutes.FAVORITE_ROUTE, "550e8400 e29b 41d4 a716")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(routeUpdateRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn404NotFoundWhenHistoryItemDoesNotExist() throws Exception {
        mockMvc
            .perform(
                put(
                        ApiPaths.FavoriteRoutes.FAVORITE_ROUTE,
                        favoriteRoutes.getFirst().getId().toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + generateTokenWithOtherUser())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(routeUpdateRequest)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.NOT_FOUND.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenRequestBodyIsNull() throws Exception {
        RouteUpdateRequest requestNull = RouteUpdateRequest.builder().build();
        mockMvc
            .perform(
                put(
                        ApiPaths.FavoriteRoutes.FAVORITE_ROUTE,
                        favoriteRoutes.getFirst().getId().toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + generateTokenWithOtherUser())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestNull)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenRequestBodyIsBlank() throws Exception {
        String requestBlank =
            """
                                {
                                  "name": "",
                                  "origin": "",
                                  "destination": "",
                                  "transportType": ""
                                }
                                """;
        mockMvc
            .perform(
                put(
                        ApiPaths.FavoriteRoutes.FAVORITE_ROUTE,
                        favoriteRoutes.getFirst().getId().toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + generateTokenWithOtherUser())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestBlank)))
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
                                  "transportType": "PLANE"
                                }
                                """;

        mockMvc
            .perform(
                put(
                        ApiPaths.FavoriteRoutes.FAVORITE_ROUTE,
                        favoriteRoutes.getFirst().getId().toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + generateTokenWithOtherUser())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }
    }
  }

  @Nested
  @DisplayName("delete favorite route")
  class DeleteRoute {

    private static final Integer DEFAULT_NUMBER_OF_ROUTES = 5;
    private List<FavoriteRouteEntity> favoriteRoutes;

    @BeforeEach
    void setUp() {
      favoriteRouteRepository.deleteAll();

      List<FavoriteRouteEntity> rawFavoritesRoutes =
          FavoriteRouteMockFactory.createMockFavoriteRoutes(user, DEFAULT_NUMBER_OF_ROUTES);
      favoriteRoutes = favoriteRouteRepository.saveAll(rawFavoritesRoutes);
    }

    @Nested
    @DisplayName("Success cases")
    class SuccessCases {

      @Test
      void shouldReturn204WhenSearchIsDeletedFavoriteRoute() throws Exception {
        mockMvc
            .perform(
                delete(
                        ApiPaths.FavoriteRoutes.FAVORITE_ROUTE,
                        favoriteRoutes.getFirst().getId().toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isNoContent());
      }

      @Test
      void shouldVerifyRouteIsRemovedFromDatabase() throws Exception {
        UUID searchIdToDelete = favoriteRoutes.getFirst().getId();

        mockMvc
            .perform(
                delete(ApiPaths.FavoriteRoutes.FAVORITE_ROUTE, searchIdToDelete.toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isNoContent());

        mockMvc
            .perform(
                get(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.favoriteRoutes", hasSize(DEFAULT_NUMBER_OF_ROUTES - 1)));
      }

      @Test
      void shouldDeleteMultipleFavoriteRouteSequentially() throws Exception {

        UUID firstId = favoriteRoutes.getFirst().getId();
        UUID secondId = favoriteRoutes.get(1).getId();

        mockMvc
            .perform(
                delete(ApiPaths.FavoriteRoutes.FAVORITE_ROUTE, firstId.toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isNoContent());

        mockMvc
            .perform(
                delete(ApiPaths.FavoriteRoutes.FAVORITE_ROUTE, secondId.toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isNoContent());

        mockMvc
            .perform(
                get(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.favoriteRoutes", hasSize(DEFAULT_NUMBER_OF_ROUTES - 2)));
      }

      @Test
      void shouldReturnEmptyBodyWhenFavoriteRouteIsDeleted() throws Exception {

        UUID favoriteRoute = favoriteRoutes.getFirst().getId();

        mockMvc
            .perform(
                delete(ApiPaths.FavoriteRoutes.FAVORITE_ROUTE, favoriteRoute.toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));
      }

      @Test
      void shouldDeleteFavoriteRoutesAndIdsShouldNotExistInResponse() throws Exception {

        UUID firstId = favoriteRoutes.getFirst().getId();
        UUID secondId = favoriteRoutes.get(1).getId();

        mockMvc
            .perform(
                delete(ApiPaths.FavoriteRoutes.FAVORITE_ROUTE, firstId.toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isNoContent());

        mockMvc
            .perform(
                delete(ApiPaths.FavoriteRoutes.FAVORITE_ROUTE, secondId.toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isNoContent());

        mockMvc
            .perform(
                get(ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.favoriteRoutes", hasSize(DEFAULT_NUMBER_OF_ROUTES - 2)))
            .andExpect(jsonPath("$.favoriteRoutes[*].id", not(hasItem(firstId.toString()))))
            .andExpect(jsonPath("$.favoriteRoutes[*].id", not(hasItem(secondId.toString()))));
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
                        ApiPaths.FavoriteRoutes.FAVORITE_ROUTE,
                        favoriteRoutes.getFirst().getId().toString())
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
                        ApiPaths.FavoriteRoutes.FAVORITE_ROUTE,
                        favoriteRoutes.getFirst().getId().toString())
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
                    ApiPaths.FavoriteRoutes.FAVORITE_ROUTE,
                    favoriteRoutes.getFirst().getId().toString()))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.UNAUTHORIZED.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn401UnauthorizedWhenUrlPathIsMissing() throws Exception {
        mockMvc
            .perform(delete(ApiPaths.FavoriteRoutes.FAVORITE_ROUTE, ""))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.UNAUTHORIZED.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }
    }

    @Nested
    @DisplayName("request validation")
    class RequestValidation {

      @Test
      void shouldReturn400BadRequestWhenRouteIdIsNotValidUuid() throws Exception {
        mockMvc
            .perform(
                delete(ApiPaths.FavoriteRoutes.FAVORITE_ROUTE, "invalid-uuid")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenRouteIdContainsSpecialCharacters() throws Exception {
        mockMvc
            .perform(
                delete(ApiPaths.FavoriteRoutes.FAVORITE_ROUTE, "@#$%")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest());
      }

      @Test
      void shouldReturn400BadRequestWhenRouteIdContainsOnlyNumbers() throws Exception {
        mockMvc
            .perform(
                delete(ApiPaths.FavoriteRoutes.FAVORITE_ROUTE, "123456")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenRouteIdHasInvalidUuidFormat() throws Exception {
        mockMvc
            .perform(
                delete(ApiPaths.FavoriteRoutes.FAVORITE_ROUTE, "550e8400-e29b-41d4-a716")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenRouteIdContainsSpaces() throws Exception {
        mockMvc
            .perform(
                delete(ApiPaths.FavoriteRoutes.FAVORITE_ROUTE, "550e8400 e29b 41d4 a716")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn404NotFoundWhenFavoriteRouteDoesNotExist() throws Exception {
        mockMvc
            .perform(
                delete(
                        ApiPaths.FavoriteRoutes.FAVORITE_ROUTE,
                        favoriteRoutes.getFirst().getId().toString())
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
