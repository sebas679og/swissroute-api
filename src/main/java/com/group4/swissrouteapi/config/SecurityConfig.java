package com.group4.swissrouteapi.config;

import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.config.constants.InternalHeaders;
import com.group4.swissrouteapi.config.properties.CorsConfigurationProperties;
import java.util.List;

import com.group4.swissrouteapi.config.properties.JwtProperties;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;

/**
 * SecurityConfig
 *
 * <p>Spring Security configuration class responsible for defining application-wide security
 * policies.
 *
 * <p>Annotated with {@link Configuration} to indicate that it provides Spring-managed beans, {@link
 * EnableMethodSecurity} to enable method-level security annotations, and {@link
 * RequiredArgsConstructor} to support constructor-based dependency injection.
 *
 * <p>Configures the security filter chain, CORS policies, and password encoding strategy.
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtProperties jwtProperties;

  /**
   * Configures the application's security filter chain.
   *
   * <p>Enables CORS with the provided configuration source, disables CSRF, enforces stateless
   * session management, and defines authorization rules. Permits access to API documentation and
   * registration endpoints while requiring authentication for all other requests.
   *
   * @param http the {@link HttpSecurity} to configure
   * @param corsConfigurationSource the source of CORS configuration
   * @return the built {@link SecurityFilterChain} enforcing application security rules
   * @throws Exception if an error occurs during configuration
   */
  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
    return http.cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(ApiPaths.Docs.SWAGGER_UI, ApiPaths.Docs.API_DOCS)
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, ApiPaths.Auth.REGISTER)
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, ApiPaths.Auth.LOGIN)
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .build();
  }

  /**
   * Provides a CORS configuration source bean.
   *
   * <p>Builds a {@link CorsConfiguration} using allowed origins, methods, and headers defined in
   * {@link CorsConfigurationProperties}. Registers the configuration for all paths.
   *
   * @param corsProperties the custom CORS properties
   * @return the configured {@link CorsConfigurationSource}
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource(
      CorsConfigurationProperties corsProperties) {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(corsProperties.getAllowedOrigins());
    config.setAllowedMethods(corsProperties.getAllowedMethods());
    config.setAllowedHeaders(List.of(InternalHeaders.AUTHORIZATION, InternalHeaders.CONTENT_TYPE));
    config.setMaxAge(corsProperties.getMaxAge());
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  /**
   * Provides a password encoder bean.
   *
   * <p>Uses {@link BCryptPasswordEncoder} to securely hash and verify user passwords.
   *
   * @return a {@link PasswordEncoder} instance
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
