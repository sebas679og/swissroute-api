package com.group4.swissrouteapi.config;

import com.group4.swissrouteapi.utils.enums.TransportationType;
import java.util.Arrays;
import java.util.Locale;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig
 *
 * <p>Spring MVC configuration class implementing {@link WebMvcConfigurer}. Registers custom
 * formatters and converters for request parameter binding.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(
        String.class,
        TransportationType.class,
        source -> {
          try {
            return TransportationType.valueOf(source.toUpperCase(Locale.ROOT));
          } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid transport type: '%s'. Valid values: %s"
                    .formatted(source, Arrays.toString(TransportationType.values())));
          }
        });
  }
}
