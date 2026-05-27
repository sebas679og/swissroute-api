package com.group4.swissrouteapi.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group4.swissrouteapi.AbstractIntegrationTest;
import com.group4.swissrouteapi.config.JwtKeyProvider;
import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.dtos.requests.LoginRequest;
import com.group4.swissrouteapi.dtos.responses.auth.LoginResponse;
import com.group4.swissrouteapi.models.SearchHistoryEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.providers.SearchHistoryMockFactory;
import com.group4.swissrouteapi.providers.UserDataProvider;
import com.group4.swissrouteapi.repositories.SearchHistoryRepository;
import com.group4.swissrouteapi.repositories.UserRepository;
import com.group4.swissrouteapi.services.AuthService;
import io.jsonwebtoken.Jwts;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** HistoryController Integration Tests. */
@DisplayName("HistoryController Integration Tests")
public class HistoryControllerTest extends AbstractIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private UserRepository userRepository;
  @Autowired private SearchHistoryRepository searchHistoryRepository;
  @Autowired private AuthService authService;
  @Autowired private JwtKeyProvider provider;

  private static final String TYPE_TOKEN = "Bearer ";
  private static final Integer DEFAULT_NUMBER_OF_SEARCHES = 100;
  private static final Integer DEFAULT_PAGE_SIZE = 20;
  private static final Integer DEFAULT_PAGE_NUMBER = 1;

  private String token;
  private UserEntity user;
  private List<SearchHistoryEntity> searchHistory;

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

  private String generateToken(UserEntity anotherUser) {
    Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    return Jwts.builder()
        .subject(anotherUser.getId().toString())
        .claim("email", anotherUser.getEmail())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(20)))
        .signWith(provider.getSigningKey())
        .compact();
  }

  @BeforeEach
  void setUp() {
    searchHistoryRepository.deleteAll();
    userRepository.deleteAll();
    UserEntity rawUser =
        UserEntity.builder()
            .name(UserDataProvider.VALID_NAME)
            .email(UserDataProvider.VALID_EMAIL)
            .password(passwordEncoder.encode(UserDataProvider.VALID_PASSWORD))
            .baseCity(UserDataProvider.VALID_BASE_CITY)
            .build();
    user = userRepository.save(rawUser);

    List<SearchHistoryEntity> rawSearchHistory =
        SearchHistoryMockFactory.createMockHistories(user, DEFAULT_NUMBER_OF_SEARCHES);

    searchHistory = searchHistoryRepository.saveAll(rawSearchHistory);

    LoginRequest loginRequest =
        LoginRequest.builder()
            .email(UserDataProvider.VALID_EMAIL)
            .password(UserDataProvider.VALID_PASSWORD)
            .build();
    LoginResponse loginResponse = authService.loginUser(loginRequest);
    token = loginResponse.getToken();
  }

  @Nested
  @DisplayName("Get user history")
  class GetUserHistory {

    @Nested
    @DisplayName("Success cases")
    class SuccessCases {

      @Test
      void shouldReturn200OkWhenPaginationQueryParamsAreMissing() throws Exception {
        int totalPages = (int) Math.ceil((double) DEFAULT_NUMBER_OF_SEARCHES / DEFAULT_PAGE_SIZE);
        mockMvc
            .perform(
                get(ApiPaths.History.HISTORY).header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.history").isArray())
            .andExpect(jsonPath("$.history", hasSize(DEFAULT_PAGE_SIZE)))
            .andExpect(jsonPath("$.page").value(DEFAULT_PAGE_NUMBER))
            .andExpect(jsonPath("$.size").value(DEFAULT_PAGE_SIZE))
            .andExpect(jsonPath("$.totalElements").value(DEFAULT_NUMBER_OF_SEARCHES))
            .andExpect(jsonPath("$.totalPages").value(totalPages));
      }

      @Test
      void shouldReturn200OkWhenCustomPageAndSizeAreProvided() throws Exception {
        int page = 2;
        int size = 10;

        mockMvc
            .perform(
                get(ApiPaths.History.HISTORY)
                    .param("page", String.valueOf(page))
                    .param("size", String.valueOf(size))
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.history").isArray())
            .andExpect(jsonPath("$.history", hasSize(size)))
            .andExpect(jsonPath("$.page").value(page))
            .andExpect(jsonPath("$.size").value(size))
            .andExpect(jsonPath("$.totalElements").value(DEFAULT_NUMBER_OF_SEARCHES))
            .andExpect(
                jsonPath("$.totalPages")
                    .value((int) Math.ceil((double) DEFAULT_NUMBER_OF_SEARCHES / size)));
      }

      @Test
      void shouldReturn200OkWhenPageIsOne() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.History.HISTORY)
                    .param("page", "1")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page").value(1))
            .andExpect(jsonPath("$.size").value(DEFAULT_PAGE_SIZE))
            .andExpect(jsonPath("$.history", hasSize(DEFAULT_PAGE_SIZE)));
      }

      @Test
      void shouldReturn200OkWhenSizeIsMinimumAllowed() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.History.HISTORY)
                    .param("size", "1")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page").value(DEFAULT_PAGE_NUMBER))
            .andExpect(jsonPath("$.size").value(1))
            .andExpect(jsonPath("$.history", hasSize(1)))
            .andExpect(jsonPath("$.totalPages").value(DEFAULT_NUMBER_OF_SEARCHES));
      }

      @Test
      void shouldReturn200OkWhenSizeIsMaximumAllowed() throws Exception {
        int size = 50;

        mockMvc
            .perform(
                get(ApiPaths.History.HISTORY)
                    .param("size", String.valueOf(size))
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page").value(DEFAULT_PAGE_NUMBER))
            .andExpect(jsonPath("$.size").value(size))
            .andExpect(jsonPath("$.history", hasSize(size)))
            .andExpect(
                jsonPath("$.totalPages")
                    .value((int) Math.ceil((double) DEFAULT_NUMBER_OF_SEARCHES / size)));
      }

      @Test
      void shouldReturnRemainingElementsOnLastPage() throws Exception {
        int size = 30;
        int page = 4;

        mockMvc
            .perform(
                get(ApiPaths.History.HISTORY)
                    .param("page", String.valueOf(page))
                    .param("size", String.valueOf(size))
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page").value(page))
            .andExpect(jsonPath("$.size").value(size))
            .andExpect(jsonPath("$.history", hasSize(10)))
            .andExpect(jsonPath("$.totalElements").value(DEFAULT_NUMBER_OF_SEARCHES))
            .andExpect(jsonPath("$.totalPages").value(4));
      }

      @Test
      void shouldReturnAllElementsAcrossPaginationWithoutDuplicates() throws Exception {

        int size = 10;
        int totalPages = (int) Math.ceil((double) DEFAULT_NUMBER_OF_SEARCHES / size);

        ObjectMapper objectMapper = new ObjectMapper();

        Set<String> uniqueIds = new HashSet<>();
        int totalFetchedElements = 0;

        for (int page = 1; page <= totalPages; page++) {

          MvcResult result =
              mockMvc
                  .perform(
                      get(ApiPaths.History.HISTORY)
                          .param("page", String.valueOf(page))
                          .param("size", String.valueOf(size))
                          .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
                  .andExpect(status().isOk())
                  .andExpect(jsonPath("$.page").value(page))
                  .andExpect(jsonPath("$.size").value(size))
                  .andReturn();

          String responseContent = result.getResponse().getContentAsString();

          JsonNode responseJson = objectMapper.readTree(responseContent);

          JsonNode history = responseJson.get("history");

          totalFetchedElements += history.size();

          for (JsonNode item : history) {
            uniqueIds.add(item.get("id").asText());
          }
        }

        assertEquals(DEFAULT_NUMBER_OF_SEARCHES, totalFetchedElements);

        assertEquals(DEFAULT_NUMBER_OF_SEARCHES, uniqueIds.size());
      }
    }

    @Nested
    @DisplayName("Security")
    class Security {

      @Test
      void shouldReturn401UnauthorizedWhenTokenIsExpired() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.History.HISTORY)
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
                get(ApiPaths.History.HISTORY)
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
            .perform(get(ApiPaths.History.HISTORY))
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
                get(ApiPaths.History.HISTORY)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + generateTokenWithOtherUser()))
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
      void shouldReturn400BadRequestWhenPageIsLessThanOne() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.History.HISTORY)
                    .param("page", "0")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenPageIsNegative() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.History.HISTORY)
                    .param("page", "-1")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenSizeIsLessThanOne() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.History.HISTORY)
                    .param("size", "0")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenSizeIsNegative() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.History.HISTORY)
                    .param("size", "-10")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenSizeExceedsMaximumAllowed() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.History.HISTORY)
                    .param("size", "51")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenPageContainsLetters() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.History.HISTORY)
                    .param("page", "abc")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenSizeContainsLetters() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.History.HISTORY)
                    .param("size", "abc")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenPageContainsDecimalNumber() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.History.HISTORY)
                    .param("page", "1.5")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenSizeContainsDecimalNumber() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.History.HISTORY)
                    .param("size", "10.5")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenPageContainsSpecialCharacters() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.History.HISTORY)
                    .param("page", "@#$")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenSizeContainsSpecialCharacters() throws Exception {
        mockMvc
            .perform(
                get(ApiPaths.History.HISTORY)
                    .param("size", "!*?")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }
    }
  }

  @Nested
  @DisplayName("Deletion of a search from the user's history")
  class DeleteSearchFromHistory {

    @Nested
    @DisplayName("Success cases")
    class SuccessCases {

      @Test
      void shouldReturn204WhenSearchIsDeletedFromHistory() throws Exception {
        mockMvc
            .perform(
                delete(ApiPaths.History.HISTORY_ITEM, searchHistory.getFirst().getId().toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isNoContent());
      }

      @Test
      void shouldVerifyItemIsRemovedFromDatabase() throws Exception {
        UUID searchIdToDelete = searchHistory.getFirst().getId();

        mockMvc
            .perform(
                delete(ApiPaths.History.HISTORY_ITEM, searchIdToDelete.toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isNoContent());

        mockMvc
            .perform(
                get(ApiPaths.History.HISTORY).header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(DEFAULT_NUMBER_OF_SEARCHES - 1));
      }

      @Test
      void shouldDeleteMultipleHistoryItemsSequentially() throws Exception {

        UUID firstId = searchHistory.getFirst().getId();
        UUID secondId = searchHistory.get(1).getId();

        mockMvc
            .perform(
                delete(ApiPaths.History.HISTORY_ITEM, firstId.toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isNoContent());

        mockMvc
            .perform(
                delete(ApiPaths.History.HISTORY_ITEM, secondId.toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isNoContent());

        mockMvc
            .perform(
                get(ApiPaths.History.HISTORY).header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(DEFAULT_NUMBER_OF_SEARCHES - 2));
      }

      @Test
      void shouldReturnEmptyBodyWhenHistoryItemIsDeleted() throws Exception {

        UUID searchIdToDelete = searchHistory.getFirst().getId();

        mockMvc
            .perform(
                delete(ApiPaths.History.HISTORY_ITEM, searchIdToDelete.toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));
      }

      @Test
      void shouldRemoveOnlyRequestedHistoryItem() throws Exception {

        UUID deletedId = searchHistory.getFirst().getId();

        Set<String> expectedRemainingIds =
            searchHistory.stream()
                .map(SearchHistoryEntity::getId)
                .map(UUID::toString)
                .filter(id -> !id.equals(deletedId.toString()))
                .collect(Collectors.toSet());

        mockMvc
            .perform(
                delete(ApiPaths.History.HISTORY_ITEM, deletedId.toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isNoContent());

        int size = 20;
        int totalPages = (int) Math.ceil((double) expectedRemainingIds.size() / size);

        ObjectMapper objectMapper = new ObjectMapper();

        Set<String> returnedIds = new HashSet<>();

        for (int page = 1; page <= totalPages; page++) {

          MvcResult result =
              mockMvc
                  .perform(
                      get(ApiPaths.History.HISTORY)
                          .param("page", String.valueOf(page))
                          .param("size", String.valueOf(size))
                          .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
                  .andExpect(status().isOk())
                  .andReturn();

          JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());

          for (JsonNode item : response.get("history")) {
            returnedIds.add(item.get("id").asText());
          }
        }

        assertFalse(returnedIds.contains(deletedId.toString()));

        assertEquals(expectedRemainingIds, returnedIds);
      }
    }

    @Nested
    @DisplayName("Security")
    class Security {

      @Test
      void shouldReturn401UnauthorizedWhenTokenIsExpired() throws Exception {
        mockMvc
            .perform(
                delete(ApiPaths.History.HISTORY_ITEM, searchHistory.getFirst().getId().toString())
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
                delete(ApiPaths.History.HISTORY_ITEM, searchHistory.getFirst().getId().toString())
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
                delete(ApiPaths.History.HISTORY_ITEM, searchHistory.getFirst().getId().toString()))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(HttpStatus.UNAUTHORIZED.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.UNAUTHORIZED.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn401UnauthorizedWhenUrlPathIsMissing() throws Exception {
        mockMvc
            .perform(delete(ApiPaths.History.HISTORY_ITEM, ""))
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
      void shouldReturn400BadRequestWhenHistoryIdIsNotValidUuid() throws Exception {
        mockMvc
            .perform(
                delete(ApiPaths.History.HISTORY_ITEM, "invalid-uuid")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenHistoryIdContainsSpecialCharacters() throws Exception {
        mockMvc
            .perform(
                delete(ApiPaths.History.HISTORY_ITEM, "@#$%")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest());
      }

      @Test
      void shouldReturn400BadRequestWhenHistoryIdContainsOnlyNumbers() throws Exception {
        mockMvc
            .perform(
                delete(ApiPaths.History.HISTORY_ITEM, "123456")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenHistoryIdHasInvalidUuidFormat() throws Exception {
        mockMvc
            .perform(
                delete(ApiPaths.History.HISTORY_ITEM, "550e8400-e29b-41d4-a716")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.BAD_REQUEST.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }

      @Test
      void shouldReturn400BadRequestWhenHistoryIdContainsSpaces() throws Exception {
        mockMvc
            .perform(
                delete(ApiPaths.History.HISTORY_ITEM, "550e8400 e29b 41d4 a716")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
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
                delete(ApiPaths.History.HISTORY_ITEM, searchHistory.getFirst().getId().toString())
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + generateTokenWithOtherUser()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
            .andExpect(jsonPath("$.name").value(HttpStatus.NOT_FOUND.name()))
            .andExpect(jsonPath("$.description").exists())
            .andExpect(jsonPath("$.timestamp").exists());
      }
    }
  }

  @Nested
  @DisplayName("Total history cleanup")
  class TotalHistoryCleanup {

    @Nested
    @DisplayName("Success cases")
    class SuccessCases {

      @Test
      void shouldReturn204WhenHistoryIsCompletelyCleared() throws Exception {
        mockMvc
            .perform(
                delete(ApiPaths.History.HISTORY)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isNoContent());
      }

      @Test
      void shouldVerifySearchHistoryIsCompletelyCleared() throws Exception {
        mockMvc
            .perform(
                delete(ApiPaths.History.HISTORY)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isNoContent());

        mockMvc
            .perform(
                get(ApiPaths.History.HISTORY).header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.history").isArray())
            .andExpect(jsonPath("$.history", hasSize(0)))
            .andExpect(jsonPath("$.totalElements").value(0));
      }

      @Test
      void shouldOnlyClearAuthenticatedUserHistory() throws Exception {

        UserEntity anotherUser = userRepository.save(UserDataProvider.createAnotherMockUser());

        searchHistoryRepository.saveAll(
            SearchHistoryMockFactory.createMockHistories(anotherUser, 10));

        mockMvc
            .perform(
                delete(ApiPaths.History.HISTORY)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isNoContent());

        String anotherUserToken = generateToken(anotherUser);

        mockMvc
            .perform(
                get(ApiPaths.History.HISTORY)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + anotherUserToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.history").isArray())
            .andExpect(jsonPath("$.history", hasSize(10)))
            .andExpect(jsonPath("$.totalElements").value(10));
      }

      @Test
      void shouldReturn204WhenHistoryIsAlreadyEmpty() throws Exception {

        mockMvc
            .perform(
                delete(ApiPaths.History.HISTORY)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isNoContent());

        mockMvc
            .perform(
                delete(ApiPaths.History.HISTORY)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isNoContent());

        mockMvc
            .perform(
                get(ApiPaths.History.HISTORY).header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.history", hasSize(0)));
      }

      @Test
      void shouldReturnEmptyBodyWhenHistoryIsCleared() throws Exception {

        mockMvc
            .perform(
                delete(ApiPaths.History.HISTORY)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));
      }

      @Test
      void shouldReturnEmptyPagedResultAfterHistoryIsCleared() throws Exception {

        mockMvc
            .perform(
                delete(ApiPaths.History.HISTORY)
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isNoContent());

        mockMvc
            .perform(
                get(ApiPaths.History.HISTORY)
                    .param("page", "1")
                    .param("size", "10")
                    .header(HttpHeaders.AUTHORIZATION, TYPE_TOKEN + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.history", hasSize(0)))
            .andExpect(jsonPath("$.page").value(1))
            .andExpect(jsonPath("$.size").value(10))
            .andExpect(jsonPath("$.totalElements").value(0))
            .andExpect(jsonPath("$.totalPages").value(0));
      }
    }

    @Nested
    @DisplayName("Security")
    class Security {

      @Test
      void shouldReturn401UnauthorizedWhenTokenIsExpired() throws Exception {
        mockMvc
            .perform(
                delete(ApiPaths.History.HISTORY_ITEM, searchHistory.getFirst().getId().toString())
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
                delete(ApiPaths.History.HISTORY_ITEM, searchHistory.getFirst().getId().toString())
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
                delete(ApiPaths.History.HISTORY_ITEM, searchHistory.getFirst().getId().toString()))
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
                delete(ApiPaths.History.HISTORY)
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
