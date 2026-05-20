package com.group4.swissrouteapi;

import com.group4.swissrouteapi.models.UserEntity;

/** Utility class for creating mock user data for testing purposes. */
public class UserDataProvider {

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
}
