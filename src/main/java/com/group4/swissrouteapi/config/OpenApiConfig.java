package com.group4.swissrouteapi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/** Configuration class for OpenAPI (Swagger) documentation. */
@OpenAPIDefinition(
    info =
        @Info(
            title = "SwissRoute API",
            description = "REST API for route planning and navigation in Switzerland",
            version = "1.0.0"))
@SecurityScheme(
    name = "BearerAuth",
    description = "JWT Authorization header using Bearer scheme",
    type = SecuritySchemeType.HTTP,
    scheme = "Bearer",
    bearerFormat = "JWT")
public class OpenApiConfig {}
