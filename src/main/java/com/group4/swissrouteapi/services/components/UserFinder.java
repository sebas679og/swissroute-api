package com.group4.swissrouteapi.services.components;

import com.group4.swissrouteapi.exceptions.NotFoundException;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.repositories.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserFinder
 *
 * <p>Spring component responsible for retrieving {@link UserEntity} instances from the persistence
 * layer.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Provides a centralized utility for user lookups by ID.
 *   <li>Delegates persistence operations to {@link UserRepository}.
 *   <li>Ensures transactional consistency with read-only semantics.
 *   <li>Throws {@link NotFoundException} when a user cannot be found.
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class UserFinder {

  private final UserRepository userRepository;

  /**
   * Retrieves a {@link UserEntity} by its unique identifier.
   *
   * <p>Executes a read-only transactional query to ensure consistency without modifying the
   * persistence context. If no user is found, a {@link NotFoundException} is thrown.
   *
   * @param userId unique identifier of the user to retrieve
   * @return the {@link UserEntity} associated with the given ID
   * @throws NotFoundException if no user exists with the provided ID
   */
  @Transactional(readOnly = true)
  public UserEntity findById(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));
  }
}
