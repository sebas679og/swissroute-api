package com.group4.swissrouteapi.services.processors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.group4.swissrouteapi.exceptions.UnauthorizedException;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.repositories.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Unit tests for {@link UserLoginProcessor}.
 *
 * <p>Verifies that {@code authenticate} returns the correct {@link UserEntity} on success, and
 * throws {@link UnauthorizedException} when the user is not found or the password does not match.
 * All collaborators are mocked — no Spring context or database required.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserLoginProcessor")
class UserLoginProcessorTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private UserLoginProcessor userLoginProcessor;

  // ---------------------------------------------------------------------------
  // Shared fixtures
  // ---------------------------------------------------------------------------

  private static final String EMAIL = "john.doe@example.com";
  private static final String RAW_PASSWORD = "Secure@123";
  private static final String HASH = "$2a$10$someHashedPassword";

  private UserEntity buildUser() {
    return UserEntity.builder().email(EMAIL).password(HASH).build();
  }

  // ---------------------------------------------------------------------------
  // authenticate — success
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("authenticate() - successful login")
  class SuccessfulAuthenticationTest {

    @Test
    @DisplayName("should return the user when credentials are valid")
    void shouldReturnUserWhenCredentialsAreValid() {
      UserEntity user = buildUser();
      when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
      when(passwordEncoder.matches(RAW_PASSWORD, HASH)).thenReturn(true);

      UserEntity result = userLoginProcessor.authenticate(EMAIL, RAW_PASSWORD);

      assertThat(result).isSameAs(user);
    }

    @Test
    @DisplayName("should look up the user by the provided email")
    void shouldLookUpUserByEmail() {
      UserEntity user = buildUser();
      when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
      when(passwordEncoder.matches(RAW_PASSWORD, HASH)).thenReturn(true);

      userLoginProcessor.authenticate(EMAIL, RAW_PASSWORD);

      verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("should validate the raw password against the stored hash")
    void shouldValidatePasswordAgainstStoredHash() {
      UserEntity user = buildUser();
      when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
      when(passwordEncoder.matches(RAW_PASSWORD, HASH)).thenReturn(true);

      userLoginProcessor.authenticate(EMAIL, RAW_PASSWORD);

      verify(passwordEncoder).matches(RAW_PASSWORD, HASH);
    }
  }

  // ---------------------------------------------------------------------------
  // authenticate — user not found
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("authenticate() - user not found")
  class UserNotFoundTest {

    @Test
    @DisplayName("should throw UnauthorizedException when no user exists for the email")
    void shouldThrowWhenUserNotFound() {
      when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> userLoginProcessor.authenticate(EMAIL, RAW_PASSWORD))
          .isInstanceOf(UnauthorizedException.class)
          .hasMessage("Invalid credentials");
    }

    @Test
    @DisplayName("should not check the password when the user is not found")
    void shouldNotCheckPasswordWhenUserNotFound() {
      when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> userLoginProcessor.authenticate(EMAIL, RAW_PASSWORD))
          .isInstanceOf(UnauthorizedException.class);

      verify(passwordEncoder, never()).matches(RAW_PASSWORD, HASH);
    }
  }

  // ---------------------------------------------------------------------------
  // authenticate — wrong password
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("authenticate() - wrong password")
  class WrongPasswordTest {

    @Test
    @DisplayName("should throw UnauthorizedException when password does not match")
    void shouldThrowWhenPasswordDoesNotMatch() {
      UserEntity user = buildUser();
      when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
      when(passwordEncoder.matches(RAW_PASSWORD, HASH)).thenReturn(false);

      assertThatThrownBy(() -> userLoginProcessor.authenticate(EMAIL, RAW_PASSWORD))
          .isInstanceOf(UnauthorizedException.class)
          .hasMessage("Invalid credentials");
    }

    @Test
    @DisplayName("should return the same exception message for wrong password as for missing user")
    void shouldReturnSameMessageForWrongPasswordAndMissingUser() {
      UserEntity user = buildUser();
      when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
      when(passwordEncoder.matches(RAW_PASSWORD, HASH)).thenReturn(false);

      assertThatThrownBy(() -> userLoginProcessor.authenticate(EMAIL, RAW_PASSWORD))
          .isInstanceOf(UnauthorizedException.class)
          .hasMessage("Invalid credentials");
    }
  }
}
