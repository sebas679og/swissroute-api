package com.group4.swissrouteapi.services;

import com.group4.swissrouteapi.dtos.requests.RegisterRequest;
import com.group4.swissrouteapi.dtos.responses.RegisterResponse;
import com.group4.swissrouteapi.exceptions.ResourceConflictException;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.repositories.UserRepository;
import com.group4.swissrouteapi.services.processors.ProcessorRegisterUser;
import com.group4.swissrouteapi.utils.mappers.AuthMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final ProcessorRegisterUser processorRegisterUser;
  private final PasswordEncoder passwordEncoder;
  private final AuthMapper authMapper;

  @Override
  public RegisterResponse registerUser(RegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new ResourceConflictException("Email is already in use.");
    }
    UserEntity user =
        processorRegisterUser.userRegister(
            request.getName(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            request.getBaseCity());
    return authMapper.toRegisterResponse(user);
  }
}
