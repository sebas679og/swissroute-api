package com.group4.swissrouteapi.services.components;

import com.group4.swissrouteapi.config.SecurityConfig;
import com.group4.swissrouteapi.config.properties.JwtProperties;
import com.group4.swissrouteapi.dtos.responses.TokenValidationResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

/** JwtService Provides operations for generating and validating JWT tokens. */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecurityConfig config;

    /**
     * Generates a JWT token for a given user.
     *
     * @param userId unique identifier of the user
     * @param email email of the user
     * @return a signed JWT token string
     */
    public String generateToken(UUID userId, String email) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(jwtProperties.getExpiration())))
                .signWith(config.getSigningKey())
                .compact();
    }

    /**
     * Validates a JWT token and extracts its claims.
     *
     * @param token the JWT token to validate
     * @return the claims contained in the token
     */
    public Claims validateAndExtract(String token) {
        return Jwts.parser()
                .verifyWith(config.getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts user information from a JWT token.
     *
     * @param token the JWT token to parse
     * @return a TokenValidationResponse containing user details and validation result
     */
    public TokenValidationResponse extractUserInfo(String token) {
        Claims claims = validateAndExtract(token);
        return TokenValidationResponse.builder()
                .valid(true)
                .userId(UUID.fromString(claims.getSubject()))
                .email(claims.get("email", String.class))
                .build();
    }
}
