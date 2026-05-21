package com.group4.swissrouteapi.services.processors;

import com.group4.swissrouteapi.exceptions.UnauthorizedException;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** ProcessorLoginUser Handles user authentication by validating credentials. */
@Service
@RequiredArgsConstructor
public class UserLoginProcessor {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  /**
   * Authenticates a user by email and password.
   *
   * @param email the user's email address
   * @param rawPassword the plain text password provided
   * @return the authenticated UserEntity
   */
  @Transactional(readOnly = true)
  public UserEntity authenticate(String email, String rawPassword) {
    UserEntity user = findUserByEmail(email);
    validatePasswordAndUser(user, rawPassword);
    return user;
  }

  private UserEntity findUserByEmail(String email) {
    return userRepository.findByEmail(email).orElse(null);
  }

  private void validatePasswordAndUser(UserEntity user, String rawPassword) {
    if (user == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
      throw new UnauthorizedException("Invalid credentials");
    }
  }
}
