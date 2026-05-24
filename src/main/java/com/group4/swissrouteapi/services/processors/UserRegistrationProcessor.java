package com.group4.swissrouteapi.services.processors;

import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ProcessorRegisterUser
 *
 * <p>Service component responsible for handling user registration logic.
 *
 * <p>Provides transactional operations to persist new {@link UserEntity} instances into the system
 * using the {@link UserRepository}.
 *
 * <p>Annotated with {@link Service} to be managed by Spring's dependency injection container and
 * {@link RequiredArgsConstructor} to enforce constructor-based injection of dependencies.
 */
@Service
@RequiredArgsConstructor
public class UserRegistrationProcessor {

  private final UserRepository userRepository;

  /**
   * Registers a new user in the system.
   *
   * <p>Builds a {@link UserEntity} with the provided details and persists it to the database. The
   * operation is transactional to ensure consistency and rollback in case of errors.
   *
   * @param name the full name of the user
   * @param email the email address of the user
   * @param password the password for the user account
   * @param baseCity the base city associated with the user
   * @return the persisted {@link UserEntity} instance
   */
  @Transactional
  public UserEntity userRegister(String name, String email, String password, String baseCity) {
    UserEntity userEntity =
        UserEntity.builder().name(name).email(email).password(password).baseCity(baseCity).build();
    return userRepository.save(userEntity);
  }
}
