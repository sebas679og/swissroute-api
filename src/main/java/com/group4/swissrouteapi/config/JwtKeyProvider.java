package com.group4.swissrouteapi.config;

import com.group4.swissrouteapi.config.properties.JwtProperties;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * JwtKeyProvider
 *
 * <p>Component responsible for providing the signing key used to validate and parse JWT tokens.
 *
 * <p>Annotated with {@link org.springframework.stereotype.Component} for Spring component scanning
 * and {@link lombok.RequiredArgsConstructor} to enable constructor-based dependency injection.
 *
 * <p>Relies on {@link JwtProperties} to retrieve the secret key, which is decoded from Base64 and
 * converted into a {@link javax.crypto.SecretKey} suitable for HMAC signing.
 */
@Component
@RequiredArgsConstructor
public class JwtKeyProvider {

  private final JwtProperties jwtProperties;

  public SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
