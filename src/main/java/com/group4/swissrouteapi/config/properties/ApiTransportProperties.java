package com.group4.swissrouteapi.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for accessing the SwissRoute Transport API.
 *
 * <p>This component binds to the {@code swissroute.transport.api} prefix and provides the base URL
 * required for integration with external transport services. It is managed as a Spring Bean to
 * enable dependency injection across the application.
 *
 * <p>Designed to centralize API configuration, ensuring maintainability and flexibility when
 * connecting to external transport providers.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "swissroute.app.transport.api")
public class ApiTransportProperties {
  private String baseUrl;
  private Integer connectTimeoutMs;
  private Integer readTimeoutMs;
  private Integer writeTimeoutMs;
}
