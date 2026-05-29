package com.group4.swissrouteapi.config;

import com.group4.swissrouteapi.config.properties.ApiTransportProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * Configuration class for creating and managing WebClient instances.
 *
 * <p>This class defines a Spring Bean for the {@link WebClient} used to interact with the
 * SwissRoute Transport API. It leverages {@link ApiTransportProperties} to configure the base URL,
 * ensuring centralized and maintainable API integration.
 *
 * <p>Designed to provide a reusable and properly configured HTTP client for external service
 * communication within the application.
 */
@Configuration
public class WebClientConfig {

  /**
   * WebClient for the Transport API.
   *
   * <p>Adds connect + read/write timeouts, a 2MB codec buffer cap, and a default Accept header. All
   * values are driven by {@link ApiTransportProperties} so they can be tuned per-environment via
   * application.properties without touching code.
   */
  @Bean
  public WebClient apiTransport(ApiTransportProperties properties) {
    HttpClient httpClient =
        HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getConnectTimeoutMs())
            .doOnConnected(
                conn ->
                    conn.addHandlerLast(
                            new ReadTimeoutHandler(
                                properties.getReadTimeoutMs(), TimeUnit.MILLISECONDS))
                        .addHandlerLast(
                            new WriteTimeoutHandler(
                                properties.getWriteTimeoutMs(), TimeUnit.MILLISECONDS)));
    ExchangeStrategies strategies =
        ExchangeStrategies.builder()
            .codecs(config -> config.defaultCodecs().maxInMemorySize(properties.getMemorySize() * 1024 * 1024))
            .build();

    return WebClient.builder()
        .baseUrl(properties.getBaseUrl())
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .exchangeStrategies(strategies)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }
}
