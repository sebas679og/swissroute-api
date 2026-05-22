package com.group4.swissrouteapi.services.components;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.group4.swissrouteapi.config.JwtKeyProvider;
import com.group4.swissrouteapi.exceptions.JsonWriter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Unit tests for {@link BearerAuthenticationFilter}.
 *
 * <p>Uses {@link MockHttpServletRequest} and {@link MockHttpServletResponse} to avoid a running
 * servlet container. The {@link SecurityContextHolder} is cleared before every test to guarantee
 * isolation between cases.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BearerAuthenticationFilter")
class BearerAuthenticationFilterTest {

  @Mock private JwtKeyProvider jwtKeyProvider;
  @Mock private JsonWriter jsonWriter;
  @Mock private FilterChain filterChain;

  @InjectMocks private BearerAuthenticationFilter filter;

  /**
   * A 256-bit Base64-encoded secret used exclusively in tests. Never use a test secret in
   * production configuration.
   */
  private static final String TEST_SECRET =
      "dGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2gtZm9yLUhTMjU2";

  private static final String USER_ID = "user-42";
  private static final String EMAIL = "john.doe@example.com";

  private SecretKey signingKey;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
    signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
  }

  /**
   * Stubs {@link JwtKeyProvider#getSigningKey()} only in tests that present a Bearer token.
   * Absent/non-Bearer header tests return early and never reach token parsing.
   */
  private void stubSigningKey() {
    when(jwtKeyProvider.getSigningKey()).thenReturn(signingKey);
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  /** Builds a signed JWT with the test key, valid for 60 seconds. */
  private String buildValidToken() {
    return Jwts.builder()
        .subject(USER_ID)
        .claim("email", EMAIL)
        .issuedAt(Date.from(Instant.now()))
        .expiration(Date.from(Instant.now().plusSeconds(60)))
        .signWith(signingKey)
        .compact();
  }

  /** Builds an expired JWT with the test key. */
  private String buildExpiredToken() {
    return Jwts.builder()
        .subject(USER_ID)
        .claim("email", EMAIL)
        .issuedAt(Date.from(Instant.now().minusSeconds(120)))
        .expiration(Date.from(Instant.now().minusSeconds(60)))
        .signWith(signingKey)
        .compact();
  }

  /** Builds a JWT signed with a different key — valid structure but wrong signature. */
  private String buildTamperedToken() {
    SecretKey wrongKey =
        Keys.hmacShaKeyFor(
            Decoders.BASE64.decode("d3Jvbmcta2V5LXRoYXQtaXMtYWxzby1sb25nLWVub3VnaC1IUzI1Ng=="));
    return Jwts.builder()
        .subject(USER_ID)
        .claim("email", EMAIL)
        .issuedAt(Date.from(Instant.now()))
        .expiration(Date.from(Instant.now().plusSeconds(60)))
        .signWith(wrongKey)
        .compact();
  }

  private MockHttpServletRequest requestWithBearer(String token) {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    return request;
  }

  // ---------------------------------------------------------------------------
  // No Authorization header
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("when Authorization header is absent")
  class NoAuthorizationHeaderTest {

    @Test
    @DisplayName("should pass the request down the filter chain without setting authentication")
    void shouldContinueChainWithoutAuthentication() throws Exception {
      MockHttpServletRequest request = new MockHttpServletRequest();
      MockHttpServletResponse response = new MockHttpServletResponse();

      filter.doFilterInternal(request, response, filterChain);

      verify(filterChain).doFilter(request, response);
      assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("should not write any error response when header is absent")
    void shouldNotWriteErrorWhenHeaderAbsent() throws Exception {
      MockHttpServletRequest request = new MockHttpServletRequest();
      MockHttpServletResponse response = new MockHttpServletResponse();

      filter.doFilterInternal(request, response, filterChain);

      verify(jsonWriter, never()).sendError(any(), any(), any());
    }
  }

  // ---------------------------------------------------------------------------
  // Authorization header without "Bearer " prefix
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("when Authorization header does not start with 'Bearer '")
  class NonBearerAuthorizationHeaderTest {

    @Test
    @DisplayName("should pass the request down the chain without setting authentication")
    void shouldContinueChainWithoutAuthentication() throws Exception {
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.addHeader(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz");
      MockHttpServletResponse response = new MockHttpServletResponse();

      filter.doFilterInternal(request, response, filterChain);

      verify(filterChain).doFilter(request, response);
      assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("should not write any error response for non-Bearer schemes")
    void shouldNotWriteErrorForNonBearerScheme() throws Exception {
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.addHeader(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz");
      MockHttpServletResponse response = new MockHttpServletResponse();

      filter.doFilterInternal(request, response, filterChain);

      verify(jsonWriter, never()).sendError(any(), any(), any());
    }
  }

  // ---------------------------------------------------------------------------
  // Valid JWT
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("when a valid Bearer token is provided")
  class ValidTokenTest {

    @Test
    @DisplayName("should set authentication in the SecurityContext")
    void shouldSetAuthenticationInSecurityContext() throws Exception {
      stubSigningKey();
      MockHttpServletRequest request = requestWithBearer(buildValidToken());
      MockHttpServletResponse response = new MockHttpServletResponse();

      filter.doFilterInternal(request, response, filterChain);

      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      assertThat(authentication).isNotNull();
      assertThat(authentication.isAuthenticated()).isTrue();
    }

    @Test
    @DisplayName("should set the email as the authentication principal")
    void shouldSetEmailAsPrincipal() throws Exception {
      stubSigningKey();
      MockHttpServletRequest request = requestWithBearer(buildValidToken());
      MockHttpServletResponse response = new MockHttpServletResponse();

      filter.doFilterInternal(request, response, filterChain);

      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      assertThat(authentication.getPrincipal()).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("should grant the AUTH_JWT authority")
    void shouldGrantAuthJwtAuthority() throws Exception {
      stubSigningKey();
      MockHttpServletRequest request = requestWithBearer(buildValidToken());
      MockHttpServletResponse response = new MockHttpServletResponse();

      filter.doFilterInternal(request, response, filterChain);

      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      assertThat(authentication.getAuthorities())
          .extracting("authority")
          .containsExactly("AUTH_JWT");
    }

    @Test
    @DisplayName("should continue the filter chain after setting authentication")
    void shouldContinueChainAfterSettingAuthentication() throws Exception {
      stubSigningKey();
      MockHttpServletRequest request = requestWithBearer(buildValidToken());
      MockHttpServletResponse response = new MockHttpServletResponse();

      filter.doFilterInternal(request, response, filterChain);

      verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("should not write any error response for a valid token")
    void shouldNotWriteErrorForValidToken() throws Exception {
      stubSigningKey();
      MockHttpServletRequest request = requestWithBearer(buildValidToken());
      MockHttpServletResponse response = new MockHttpServletResponse();

      filter.doFilterInternal(request, response, filterChain);

      verify(jsonWriter, never()).sendError(any(), any(), any());
    }

    @Test
    @DisplayName("should not overwrite an existing authentication in the SecurityContext")
    void shouldNotOverwriteExistingAuthentication() throws Exception {
      stubSigningKey();
      // Pre-populate the SecurityContext as if a previous filter already authenticated
      Authentication existing =
          new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
              "other@example.com", null, java.util.List.of());
      SecurityContextHolder.getContext().setAuthentication(existing);

      MockHttpServletRequest request = requestWithBearer(buildValidToken());
      MockHttpServletResponse response = new MockHttpServletResponse();

      filter.doFilterInternal(request, response, filterChain);

      // The original authentication must be preserved
      assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
          .isEqualTo("other@example.com");
    }
  }

  // ---------------------------------------------------------------------------
  // Invalid / expired JWT
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("when the Bearer token is invalid or expired")
  class InvalidTokenTest {

    @Test
    @DisplayName("should send a 401 Unauthorized error response for an expired token")
    void shouldSendUnauthorizedForExpiredToken() throws Exception {
      stubSigningKey();
      MockHttpServletRequest request = requestWithBearer(buildExpiredToken());
      MockHttpServletResponse response = new MockHttpServletResponse();

      filter.doFilterInternal(request, response, filterChain);

      verify(jsonWriter)
          .sendError(eq(response), eq(HttpStatus.UNAUTHORIZED), eq("Invalid or expired token"));
    }

    @Test
    @DisplayName("should send a 401 Unauthorized error response for a tampered token")
    void shouldSendUnauthorizedForTamperedToken() throws Exception {
      stubSigningKey();
      MockHttpServletRequest request = requestWithBearer(buildTamperedToken());
      MockHttpServletResponse response = new MockHttpServletResponse();

      filter.doFilterInternal(request, response, filterChain);

      verify(jsonWriter)
          .sendError(eq(response), eq(HttpStatus.UNAUTHORIZED), eq("Invalid or expired token"));
    }

    @Test
    @DisplayName("should send a 401 Unauthorized error response for a malformed token")
    void shouldSendUnauthorizedForMalformedToken() throws Exception {
      stubSigningKey();
      MockHttpServletRequest request = requestWithBearer("this.is.not.a.jwt");
      MockHttpServletResponse response = new MockHttpServletResponse();

      filter.doFilterInternal(request, response, filterChain);

      verify(jsonWriter)
          .sendError(eq(response), eq(HttpStatus.UNAUTHORIZED), eq("Invalid or expired token"));
    }

    @Test
    @DisplayName("should not set authentication in the SecurityContext for an invalid token")
    void shouldNotSetAuthenticationForInvalidToken() throws Exception {
      stubSigningKey();
      MockHttpServletRequest request = requestWithBearer(buildExpiredToken());
      MockHttpServletResponse response = new MockHttpServletResponse();

      filter.doFilterInternal(request, response, filterChain);

      assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
  }
}
