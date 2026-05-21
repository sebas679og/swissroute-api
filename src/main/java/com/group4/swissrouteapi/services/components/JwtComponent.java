package com.group4.swissrouteapi.services.components;

import com.group4.swissrouteapi.config.JwtKeyProvider;
import com.group4.swissrouteapi.config.properties.JwtProperties;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

/** JwtService Provides operations for generating and validating JWT tokens. */
@Component
@RequiredArgsConstructor
public class JwtComponent {

    private final JwtProperties jwtProperties;
    private final JwtKeyProvider provider;
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
                .signWith(provider.getSigningKey())
                .compact();
    }
}
