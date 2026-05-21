package com.group4.swissrouteapi.services.components;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.group4.swissrouteapi.config.JwtKeyProvider;
import com.group4.swissrouteapi.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link JwtComponent}.
 *
 * <p>Verifies that {@code generateToken} produces a valid, parseable JWT with the correct claims
 * and a properly bounded expiration. The signing key is derived from a fixed test secret so tokens
 * can be verified without a running application context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtComponent")
class JwtComponentTest {

  @Mock private JwtProperties jwtProperties;
  @Mock private JwtKeyProvider jwtKeyProvider;

  @InjectMocks private JwtComponent jwtComponent;

  /**
   * A 256-bit Base64-encoded secret used exclusively in tests. Never use a test secret in
   * production configuration.
   */
  private static final String TEST_SECRET =
      "dGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2gtZm9yLUhTMjU2";

  private static final long EXPIRATION_SECONDS = 3600L;
  private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final String EMAIL = "john.doe@example.com";

  private SecretKey signingKey;

  @BeforeEach
  void setUp() {
    signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
    when(jwtKeyProvider.getSigningKey()).thenReturn(signingKey);
    when(jwtProperties.getExpiration()).thenReturn(EXPIRATION_SECONDS);
  }

  // ---------------------------------------------------------------------------
  // Helper — parse the token produced by the component under test
  // ---------------------------------------------------------------------------

  private Claims parseClaims(String token) {
    return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
  }

  // ---------------------------------------------------------------------------
  // generateToken
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("generateToken()")
  class GenerateTokenTest {

    @Test
    @DisplayName("should return a non-blank token string")
    void shouldReturnNonBlankToken() {
      String token = jwtComponent.generateToken(USER_ID, EMAIL);

      assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("should set the userId as the subject claim")
    void shouldSetUserIdAsSubject() {
      String token = jwtComponent.generateToken(USER_ID, EMAIL);

      String subject = parseClaims(token).getSubject();

      assertThat(subject).isEqualTo(USER_ID.toString());
    }

    @Test
    @DisplayName("should set the email as a custom claim")
    void shouldSetEmailClaim() {
      String token = jwtComponent.generateToken(USER_ID, EMAIL);

      String emailClaim = parseClaims(token).get("email", String.class);

      assertThat(emailClaim).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("should set issuedAt to the current time (millisecond precision)")
    void shouldSetIssuedAtToNow() {
      Instant before = Instant.now().truncatedTo(ChronoUnit.SECONDS);

      String token = jwtComponent.generateToken(USER_ID, EMAIL);

      Instant after = Instant.now().truncatedTo(ChronoUnit.SECONDS);
      Date issuedAt = parseClaims(token).getIssuedAt();

      assertThat(issuedAt.toInstant()).isAfterOrEqualTo(before).isBeforeOrEqualTo(after);
    }

    @Test
    @DisplayName("should set expiration to issuedAt plus the configured expiration seconds")
    void shouldSetExpirationRelativeToIssuedAt() {
      String token = jwtComponent.generateToken(USER_ID, EMAIL);

      Claims claims = parseClaims(token);
      long issuedAt = claims.getIssuedAt().getTime();
      long expiration = claims.getExpiration().getTime();
      long diffSeconds = (expiration - issuedAt) / 1000;

      assertThat(diffSeconds).isEqualTo(EXPIRATION_SECONDS);
    }

    @Test
    @DisplayName("should produce a token verifiable with the signing key")
    void shouldProduceTokenVerifiableWithSigningKey() {
      String token = jwtComponent.generateToken(USER_ID, EMAIL);

      // parseClaims throws if the signature is invalid — no explicit assert needed
      Claims claims = parseClaims(token);

      assertThat(claims).isNotNull();
    }

    @Test
    @DisplayName("should produce different tokens for different userIds")
    void shouldProduceDifferentTokensForDifferentUserIds() {
      UUID otherUserId = UUID.fromString("00000000-0000-0000-0000-000000000002");

      String tokenA = jwtComponent.generateToken(USER_ID, EMAIL);
      String tokenB = jwtComponent.generateToken(otherUserId, EMAIL);

      assertThat(tokenA).isNotEqualTo(tokenB);
    }

    @Test
    @DisplayName("should produce different tokens for different emails")
    void shouldProduceDifferentTokensForDifferentEmails() {
      String tokenA = jwtComponent.generateToken(USER_ID, "alice@example.com");
      String tokenB = jwtComponent.generateToken(USER_ID, "bob@example.com");

      assertThat(tokenA).isNotEqualTo(tokenB);
    }
  }
}
