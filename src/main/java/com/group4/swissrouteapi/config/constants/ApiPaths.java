package com.group4.swissrouteapi.config.constants;

public final class ApiPaths {

    private ApiPaths() {}

    public static final class AuthPaths {
        public static final String PATH = "api/users";
        public static final String REGISTER = String.join(PATH, "/register");
    }
}
