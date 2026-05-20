package com.group4.swissrouteapi.repositories;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.group4.swissrouteapi.AbstractIntegrationTest;
import com.group4.swissrouteapi.UserDataProvider;
import com.group4.swissrouteapi.models.UserEntity;
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
}
