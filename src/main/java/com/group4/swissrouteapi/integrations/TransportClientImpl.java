package com.group4.swissrouteapi.integrations;

import com.group4.swissrouteapi.config.constants.ApiPaths;
import com.group4.swissrouteapi.exceptions.BadGatewayException;
import com.group4.swissrouteapi.exceptions.ServiceUnavailableException;
import com.group4.swissrouteapi.integrations.dto.responses.locations.ApiLocationsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

/**
 * TransportClientImpl
 *
 * <p>Concrete implementation of the {@link TransportClient} interface.
 *
 * <p>Provides integration with the external transport API using Spring's {@link
 * org.springframework.web.reactive.function.client.WebClient}.
 *
 * <p>Annotated with {@link org.springframework.stereotype.Component} for Spring component scanning,
 * {@link lombok.RequiredArgsConstructor} to enable constructor-based dependency injection, and
 * {@link lombok.extern.slf4j.Slf4j} to support structured logging.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransportClientImpl implements TransportClient {

  private final WebClient transportWebClient;

  @Override
  public ApiLocationsResponse getLocationsByQuery(String query) {
    WebClient.RequestHeadersSpec<?> request =
        transportWebClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path(ApiPaths.TransportApi.LOCATIONS)
                        .queryParam("query", query)
                        .build());
    return executeRequest(
        request,
        ApiLocationsResponse.class,
        String.join("", ApiPaths.TransportApi.LOCATIONS, "?query=", query));
  }

  @Override
  public ApiLocationsResponse getLocationsByCoordinates(double latitude, double longitude) {
    WebClient.RequestHeadersSpec<?> request =
        transportWebClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path(ApiPaths.TransportApi.LOCATIONS)
                        .queryParam("x", latitude)
                        .queryParam("y", longitude)
                        .build());
    return executeRequest(
        request,
        ApiLocationsResponse.class,
        String.join("", ApiPaths.TransportApi.LOCATIONS, "?x=", latitude + "&y=" + longitude));
  }

  private <T> T executeRequest(
      WebClient.RequestHeadersSpec<?> request, Class<T> responseType, String uri) {
    return request
        .retrieve()
        .onStatus(
            HttpStatusCode::is4xxClientError,
            response ->
                response
                    .bodyToMono(String.class)
                    .defaultIfEmpty("No body")
                    .flatMap(
                        body -> {
                          if (log.isErrorEnabled()) {
                            log.error(
                                "Api Transport 4xx error. Uri: {}, Status: {}, Body: {}",
                                uri,
                                response.statusCode(),
                                body);
                          }
                          return Mono.error(
                              new BadGatewayException("Api Transport rejected the request"));
                        }))
        .onStatus(
            HttpStatusCode::is5xxServerError,
            response ->
                response
                    .bodyToMono(String.class)
                    .defaultIfEmpty("No body")
                    .flatMap(
                        body -> {
                          if (log.isErrorEnabled()) {
                            log.error(
                                "Api Transport 5xx error. Uri: {}, Status: {}, Body: {}",
                                uri,
                                response.statusCode(),
                                body);
                          }
                          return Mono.error(
                              new ServiceUnavailableException("Api Transport is unavailable"));
                        }))
        .bodyToMono(responseType)
        .onErrorMap(
            WebClientRequestException.class,
            ex -> {
              if (log.isErrorEnabled()) {
                log.error(
                    "Api Transport is unreachable. Cause: {} - {}",
                    ex.getClass().getSimpleName(),
                    ex.getMessage());
              }
              return new BadGatewayException("Api Transport is unreachable");
            })
        .blockOptional()
        .orElseThrow(
            () -> {
              log.error("Api Transport returned empty body. ID: {}", uri);
              return new BadGatewayException("Empty response from Api Transport");
            });
  }
}
