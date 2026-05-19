package com.group4.swissrouteapi.services;

import com.group4.swissrouteapi.dtos.requests.RegisterRequest;
import com.group4.swissrouteapi.dtos.responses.RegisterResponse;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService{

    @Override
    public RegisterResponse registerUser(RegisterRequest request) {
        return null;
    }
}
