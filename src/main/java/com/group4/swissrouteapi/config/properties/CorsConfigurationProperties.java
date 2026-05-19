package com.group4.swissrouteapi.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration properties class for Cross-Origin Resource Sharing (CORS) settings of the
 * application.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "swissroute.app.cors")
public class CorsConfigurationProperties {
    List<String> allowedOrigins;
    List<String> allowedMethods;
    long maxAge;
}
