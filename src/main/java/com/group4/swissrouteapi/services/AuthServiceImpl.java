package com.group4.swissrouteapi.services;

import com.group4.swissrouteapi.dtos.requests.LoginRequest;
import com.group4.swissrouteapi.dtos.requests.RegisterRequest;
import com.group4.swissrouteapi.dtos.responses.LoginResponse;
import com.group4.swissrouteapi.dtos.responses.RegisterResponse;
import com.group4.swissrouteapi.exceptions.ResourceConflictException;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.repositories.UserRepository;
import com.group4.swissrouteapi.services.processors.UserRegistrationProcessor;
import com.group4.swissrouteapi.utils.mappers.AuthMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AuthServiceImpl
 *
 * <p>Concrete implementation of the {@link AuthService} interface.
 *
 * <p>Provides user registration functionality by coordinating validation, persistence, password
 * encoding, and response mapping.
 *
 * <p>Annotated with {@link Service} for Spring component scanning and {@link
 * RequiredArgsConstructor} to enable constructor-based dependency injection.
 *
 * <p>Relies on {@link UserRepository} for persistence, {@link UserRegistrationProcessor} for user
 * creation logic, {@link PasswordEncoder} for secure password handling, and {@link AuthMapper} for
 * mapping entities to response DTOs.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final UserRegistrationProcessor userRegistrationProcessor;
  private final PasswordEncoder passwordEncoder;
  private final AuthMapper authMapper;

  @Override
  public RegisterResponse registerUser(RegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new ResourceConflictException("Email is already in use.");
    }
    UserEntity user =
        userRegistrationProcessor.userRegister(
            request.getName(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            request.getBaseCity());
    return authMapper.toRegisterResponse(user);
  }

  @Override
  public LoginResponse loginUser(LoginRequest request) {
    return null;
  }
}
