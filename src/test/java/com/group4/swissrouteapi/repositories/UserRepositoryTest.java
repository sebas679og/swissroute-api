package com.group4.swissrouteapi.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.group4.swissrouteapi.AbstractIntegrationTest;
import com.group4.swissrouteapi.UserDataProvider;
import com.group4.swissrouteapi.models.UserEntity;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UserRepositoryTest extends AbstractIntegrationTest {

  @Autowired UserRepository userRepository;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
  }

  @Test
  public void shouldReturnTrueWhenEmailAlreadyExists() {
    UserEntity savedUser = userRepository.save(UserDataProvider.createMockUser());
    assertTrue(userRepository.existsByEmail(savedUser.getEmail()));
  }

  @Test
  public void shouldReturnFalseWhenEmailIsNotRegistered() {
    userRepository.save(UserDataProvider.createMockUser());
    assertFalse(userRepository.existsByEmail("email@not-registred.test"));
  }

  @Test
  public void shouldReturnUserWhenEmailExists() {
    UserEntity saved = userRepository.save(UserDataProvider.createMockUser());

    Optional<UserEntity> result = userRepository.findByEmail(saved.getEmail());

    assertTrue(result.isPresent());
    assertEquals(saved.getEmail(), result.get().getEmail());
    assertEquals(saved.getName(), result.get().getName());
    assertEquals(saved.getBaseCity(), result.get().getBaseCity());
  }

  @Test
  public void shouldReturnEmptyWhenEmailDoesNotExist() {
    userRepository.save(UserDataProvider.createMockUser());

    Optional<UserEntity> result = userRepository.findByEmail("notfound@example.com");

    assertTrue(result.isEmpty());
  }

  @Test
  public void shouldReturnEmptyWhenRepositoryIsEmpty() {
    Optional<UserEntity> result = userRepository.findByEmail("any@example.com");

    assertTrue(result.isEmpty());
  }

  @Test
  public void shouldReturnOnlyTheUserMatchingTheEmail() {
    UserEntity userA = userRepository.save(UserDataProvider.createMockUser());
    UserEntity userB = userRepository.save(UserDataProvider.createAnotherMockUser());

    Optional<UserEntity> result = userRepository.findByEmail(userA.getEmail());

    assertTrue(result.isPresent());
    assertEquals(userA.getId(), result.get().getId());
    assertNotEquals(userB.getId(), result.get().getId());
  }
}
