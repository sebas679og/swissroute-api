package com.group4.swissrouteapi.services;

import com.group4.swissrouteapi.dtos.requests.LoginRequest;
import com.group4.swissrouteapi.dtos.requests.RegisterRequest;
import com.group4.swissrouteapi.dtos.responses.auth.LoginResponse;
import com.group4.swissrouteapi.dtos.responses.auth.RegisterResponse;

/**
 * AuthService
 *
 * <p>Interface defining authentication-related operations for the application.
 *
 * <p>Provides a contract for user registration functionality, ensuring that implementations handle
 * incoming {@link RegisterRequest} objects and return a corresponding {@link RegisterResponse}.
 */
public interface AuthService {

  RegisterResponse registerUser(RegisterRequest request);

  LoginResponse loginUser(LoginRequest request);
}
