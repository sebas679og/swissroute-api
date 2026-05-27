package com.group4.swissrouteapi.providers;

import com.group4.swissrouteapi.models.UserEntity;
import java.util.UUID;

/** Utility class for creating mock user data for testing purposes. */
public class UserDataProvider {

  public static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  public static final String VALID_NAME = "John Doe";
  public static final String VALID_EMAIL = "john.doe@example.com";
  public static final String VALID_PASSWORD = "Secure@123";
  public static final String ENCODED_PASSWORD = "$2a$10$encodedHash";
  public static final String VALID_BASE_CITY = "Madrid";

  /** Static mock of fake user data. */
  public static UserEntity createMockUser() {
    return UserEntity.builder()
        .name(VALID_NAME)
        .email(VALID_EMAIL)
        .password(VALID_PASSWORD)
        .baseCity(VALID_BASE_CITY)
        .build();
  }

  /**
   * Creates a mock {@link UserEntity} representing a user registration request.
   *
   * <p>Initializes the entity with valid name, email, encoded password, and base city. Useful for
   * testing registration flows without requiring real input data.
   *
   * @return a mock {@link UserEntity} with registration attributes
   */
  public static UserEntity createMockUserRegister() {
    return UserEntity.builder()
        .name(VALID_NAME)
        .email(VALID_EMAIL)
        .password(ENCODED_PASSWORD)
        .baseCity(VALID_BASE_CITY)
        .build();
  }

  /**
   * Creates a mock {@link UserEntity} representing a login scenario.
   *
   * <p>Initializes the entity with a predefined ID, valid credentials, and base city. Useful for
   * testing authentication and login processes with consistent mock data.
   *
   * @return a mock {@link UserEntity} with login attributes
   */
  public static UserEntity createMockUserLogin() {
    return UserEntity.builder()
        .id(USER_ID)
        .name(VALID_NAME)
        .email(VALID_EMAIL)
        .password(ENCODED_PASSWORD)
        .baseCity(VALID_BASE_CITY)
        .build();
  }

  /**
   * Creates another mock {@link UserEntity} with different attributes.
   *
   * <p>Provides variation in test data by using a different name, email, password, and base city.
   * Useful for scenarios requiring multiple distinct users.
   *
   * @return a mock {@link UserEntity} with alternative attributes
   */
  public static UserEntity createAnotherMockUser() {
    return UserEntity.builder()
        .name("Juan Perez")
        .email("juan.perez@example.com")
        .password("AnotherSecure@123")
        .baseCity("Barcelona")
        .build();
  }
}
