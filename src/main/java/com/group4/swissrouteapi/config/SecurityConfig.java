package com.group4.swissrouteapi.config;

import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.config.properties.CorsConfigurationProperties;
import com.group4.swissrouteapi.exceptions.JsonWriter;
import com.group4.swissrouteapi.services.components.BearerAuthenticationFilter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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

  private final JsonWriter jsonWriter;

  private static final String AUTHORITY = "AUTH_JWT";

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
      HttpSecurity http,
      CorsConfigurationSource corsConfigurationSource,
      BearerAuthenticationFilter bearerAuthenticationFilter)
      throws Exception {
    return http.cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            exception ->
                exception.authenticationEntryPoint(
                    (request, response, authException) ->
                        jsonWriter.sendError(
                            response, HttpStatus.UNAUTHORIZED, "Authentication required")))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(ApiPaths.Docs.SWAGGER_UI, ApiPaths.Docs.API_DOCS)
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, ApiPaths.Auth.REGISTER)
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, ApiPaths.Auth.LOGIN)
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, ApiPaths.Station.STATIONS)
                    .hasAuthority(AUTHORITY)
                    .requestMatchers(HttpMethod.GET, ApiPaths.Connection.CONNECTIONS)
                    .hasAuthority(AUTHORITY)
                    .requestMatchers(HttpMethod.GET, ApiPaths.History.HISTORY)
                    .hasAuthority(AUTHORITY)
                    .requestMatchers(HttpMethod.DELETE, ApiPaths.History.HISTORY_ITEM)
                    .hasAuthority(AUTHORITY)
                    .requestMatchers(HttpMethod.DELETE, ApiPaths.History.HISTORY)
                    .hasAuthority(AUTHORITY)
                    .requestMatchers(HttpMethod.POST, ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
                    .hasAuthority(AUTHORITY)
                    .requestMatchers(HttpMethod.GET, ApiPaths.FavoriteRoutes.FAVORITE_ROUTES)
                    .hasAuthority(AUTHORITY)
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(bearerAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
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
    config.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE));
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
}
