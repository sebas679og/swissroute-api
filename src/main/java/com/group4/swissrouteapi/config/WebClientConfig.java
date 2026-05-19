package com.group4.swissrouteapi.config;

import com.group4.swissrouteapi.config.properties.ApiTransportProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

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

  @Bean
  public WebClient apiTransport(ApiTransportProperties properties) {
    return WebClient.builder().baseUrl(properties.getBaseUrl()).build();
  }
}
