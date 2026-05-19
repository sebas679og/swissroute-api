package com.group4.swissrouteapi.services;

import com.group4.swissrouteapi.dtos.requests.RegisterRequest;
import com.group4.swissrouteapi.dtos.responses.RegisterResponse;

public interface AuthService {

    RegisterResponse registerUser(RegisterRequest request);
}
