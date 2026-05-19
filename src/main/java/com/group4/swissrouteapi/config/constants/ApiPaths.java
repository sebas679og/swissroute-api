package com.group4.swissrouteapi.config.constants;

public final class ApiPaths {

    private ApiPaths() {}

    public static final class Auth {
        public static final String REGISTER = "/api/users/register";
    }

    /** API documentation endpoints. */
    public static final class Docs {
        public static final String SWAGGER_UI = "/swagger-ui/**";
        public static final String API_DOCS = "/v3/api-docs/**";
    }
}
