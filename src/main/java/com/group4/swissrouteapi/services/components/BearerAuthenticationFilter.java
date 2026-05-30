package com.group4.swissrouteapi.services.components;

import com.group4.swissrouteapi.config.JwtKeyProvider;
import com.group4.swissrouteapi.exceptions.JsonWriter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * BearerAuthenticationFilter
 *
 * <p>Custom security filter that processes incoming HTTP requests to validate and authenticate JWT
 * bearer tokens.
 *
 * <p>Extends {@link org.springframework.web.filter.OncePerRequestFilter} to ensure execution once
 * per request within the filter chain.
 *
 * <p>Annotated with {@link org.springframework.stereotype.Component} for Spring component scanning
 * and {@link lombok.RequiredArgsConstructor} to enable constructor-based dependency injection.
 *
 * <p>Relies on {@link JwtKeyProvider} to obtain the signing key for token verification and {@link
 * JsonWriter} to send standardized error responses when authentication fails.
 */
@Component
@RequiredArgsConstructor
public class BearerAuthenticationFilter extends OncePerRequestFilter {

  private final JwtKeyProvider provider;
  private final JsonWriter jsonWriter;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(7);

    try {
      Claims claims =
          Jwts.parser()
              .verifyWith(provider.getSigningKey())
              .build()
              .parseSignedClaims(token)
              .getPayload();

      String userId = claims.getSubject();

      if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("AUTH_JWT"));

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userId, null, authorities);

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
      }

      filterChain.doFilter(request, response);

    } catch (JwtException ex) {
      jsonWriter.sendError(response, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
    }
  }
}
