package com.group4.swissrouteapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.group4.swissrouteapi.UserDataProvider;
import com.group4.swissrouteapi.dtos.requests.RegisterRequest;
import com.group4.swissrouteapi.dtos.responses.RegisterResponse;
import com.group4.swissrouteapi.exceptions.ResourceConflictException;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.repositories.UserRepository;
import com.group4.swissrouteapi.services.processors.UserRegistrationProcessor;
import com.group4.swissrouteapi.utils.mappers.AuthMapper;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Unit tests for {@link AuthServiceImpl}.
 *
 * <p>Verifies the full registration flow: duplicate-email guard, password encoding, delegation to
 * {@link UserRegistrationProcessor}, and mapping to {@link RegisterResponse}. All collaborators are
 * mocked so no Spring context or database is required.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl")
class AuthServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private UserRegistrationProcessor userRegistrationProcessor;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private AuthMapper authMapper;

  @InjectMocks private AuthServiceImpl authServiceImpl;

  private RegisterRequest buildRequest() {
    return RegisterRequest.builder()
        .name(UserDataProvider.VALID_NAME)
        .email(UserDataProvider.VALID_EMAIL)
        .password(UserDataProvider.VALID_PASSWORD)
        .baseCity(UserDataProvider.VALID_BASE_CITY)
        .build();
  }

  private UserEntity buildUserEntity() {
    return UserEntity.builder()
        .name(UserDataProvider.VALID_NAME)
        .email(UserDataProvider.VALID_EMAIL)
        .password(UserDataProvider.ENCODED_PASSWORD)
        .baseCity(UserDataProvider.VALID_BASE_CITY)
        .build();
  }

  private RegisterResponse buildResponse() {
    return RegisterResponse.builder()
        .name(UserDataProvider.VALID_NAME)
        .email(UserDataProvider.VALID_EMAIL)
        .baseCity(UserDataProvider.VALID_BASE_CITY)
        .createdAt(Instant.parse("2025-01-15T10:00:00Z"))
        .build();
  }

  // ---------------------------------------------------------------------------
  // registerUser — happy path
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("registerUser() - successful registration")
  class SuccessfulRegistrationTest {

    @Test
    @DisplayName("should return the response produced by the mapper")
    void shouldReturnMappedResponse() {
      RegisterRequest request = buildRequest();
      UserEntity savedUser = buildUserEntity();
      RegisterResponse expectedResponse = buildResponse();

      when(userRepository.existsByEmail(UserDataProvider.VALID_EMAIL)).thenReturn(false);
      when(passwordEncoder.encode(UserDataProvider.VALID_PASSWORD))
          .thenReturn(UserDataProvider.ENCODED_PASSWORD);
      when(userRegistrationProcessor.userRegister(
              UserDataProvider.VALID_NAME,
              UserDataProvider.VALID_EMAIL,
              UserDataProvider.ENCODED_PASSWORD,
              UserDataProvider.VALID_BASE_CITY))
          .thenReturn(savedUser);
      when(authMapper.toRegisterResponse(savedUser)).thenReturn(expectedResponse);

      RegisterResponse result = authServiceImpl.registerUser(request);

      assertThat(result).isSameAs(expectedResponse);
    }

    @Test
    @DisplayName("should encode the raw password before persisting")
    void shouldEncodePasswordBeforePersisting() {
      final RegisterRequest request = buildRequest();
      UserEntity savedUser = buildUserEntity();

      when(userRepository.existsByEmail(UserDataProvider.VALID_EMAIL)).thenReturn(false);

      when(passwordEncoder.encode(UserDataProvider.VALID_PASSWORD))
          .thenReturn(UserDataProvider.ENCODED_PASSWORD);

      when(userRegistrationProcessor.userRegister(
              UserDataProvider.VALID_NAME,
              UserDataProvider.VALID_EMAIL,
              UserDataProvider.ENCODED_PASSWORD,
              UserDataProvider.VALID_BASE_CITY))
          .thenReturn(savedUser);

      when(authMapper.toRegisterResponse(savedUser)).thenReturn(buildResponse());

      authServiceImpl.registerUser(request);

      verify(passwordEncoder).encode(UserDataProvider.VALID_PASSWORD);
    }

    @Test
    @DisplayName("should pass the encoded password — not the raw one — to the processor")
    void shouldPassEncodedPasswordToProcessor() {
      final RegisterRequest request = buildRequest();
      UserEntity savedUser = buildUserEntity();

      when(userRepository.existsByEmail(UserDataProvider.VALID_EMAIL)).thenReturn(false);
      when(passwordEncoder.encode(UserDataProvider.VALID_PASSWORD))
          .thenReturn(UserDataProvider.ENCODED_PASSWORD);
      when(userRegistrationProcessor.userRegister(
              UserDataProvider.VALID_NAME,
              UserDataProvider.VALID_EMAIL,
              UserDataProvider.ENCODED_PASSWORD,
              UserDataProvider.VALID_BASE_CITY))
          .thenReturn(savedUser);
      when(authMapper.toRegisterResponse(savedUser)).thenReturn(buildResponse());

      authServiceImpl.registerUser(request);

      verify(userRegistrationProcessor)
          .userRegister(
              UserDataProvider.VALID_NAME,
              UserDataProvider.VALID_EMAIL,
              UserDataProvider.ENCODED_PASSWORD,
              UserDataProvider.VALID_BASE_CITY);
    }

    @Test
    @DisplayName("should delegate mapping to the auth mapper with the persisted entity")
    void shouldDelegateMappingWithPersistedEntity() {
      final RegisterRequest request = buildRequest();
      UserEntity savedUser = buildUserEntity();

      when(userRepository.existsByEmail(UserDataProvider.VALID_EMAIL)).thenReturn(false);
      when(passwordEncoder.encode(UserDataProvider.VALID_PASSWORD))
          .thenReturn(UserDataProvider.ENCODED_PASSWORD);
      when(userRegistrationProcessor.userRegister(
              UserDataProvider.VALID_NAME,
              UserDataProvider.VALID_EMAIL,
              UserDataProvider.ENCODED_PASSWORD,
              UserDataProvider.VALID_BASE_CITY))
          .thenReturn(savedUser);
      when(authMapper.toRegisterResponse(savedUser)).thenReturn(buildResponse());

      authServiceImpl.registerUser(request);

      verify(authMapper).toRegisterResponse(savedUser);
    }

    @Test
    @DisplayName("should check email existence exactly once")
    void shouldCheckEmailExistenceOnce() {
      final RegisterRequest request = buildRequest();
      UserEntity savedUser = buildUserEntity();

      when(userRepository.existsByEmail(UserDataProvider.VALID_EMAIL)).thenReturn(false);
      when(passwordEncoder.encode(UserDataProvider.VALID_PASSWORD))
          .thenReturn(UserDataProvider.ENCODED_PASSWORD);
      when(userRegistrationProcessor.userRegister(
              UserDataProvider.VALID_NAME,
              UserDataProvider.VALID_EMAIL,
              UserDataProvider.ENCODED_PASSWORD,
              UserDataProvider.VALID_BASE_CITY))
          .thenReturn(savedUser);
      when(authMapper.toRegisterResponse(savedUser)).thenReturn(buildResponse());

      authServiceImpl.registerUser(request);

      verify(userRepository).existsByEmail(UserDataProvider.VALID_EMAIL);
    }
  }

  // ---------------------------------------------------------------------------
  // registerUser — duplicate email guard
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("registerUser() - duplicate email guard")
  class DuplicateEmailGuardTest {

    @Test
    @DisplayName("should throw ResourceConflictException when email is already in use")
    void shouldThrowWhenEmailAlreadyExists() {
      RegisterRequest request = buildRequest();
      when(userRepository.existsByEmail(UserDataProvider.VALID_EMAIL)).thenReturn(true);

      assertThatThrownBy(() -> authServiceImpl.registerUser(request))
          .isInstanceOf(ResourceConflictException.class)
          .hasMessage("Email is already in use.");
    }

    @Test
    @DisplayName("should not encode the password when the email is already taken")
    void shouldNotEncodePasswordWhenEmailTaken() {
      RegisterRequest request = buildRequest();
      when(userRepository.existsByEmail(UserDataProvider.VALID_EMAIL)).thenReturn(true);

      assertThatThrownBy(() -> authServiceImpl.registerUser(request))
          .isInstanceOf(ResourceConflictException.class);

      verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("should not call the processor when the email is already taken")
    void shouldNotCallProcessorWhenEmailTaken() {
      RegisterRequest request = buildRequest();
      when(userRepository.existsByEmail(UserDataProvider.VALID_EMAIL)).thenReturn(true);

      assertThatThrownBy(() -> authServiceImpl.registerUser(request))
          .isInstanceOf(ResourceConflictException.class);

      verify(userRegistrationProcessor, never())
          .userRegister(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("should not call the mapper when the email is already taken")
    void shouldNotCallMapperWhenEmailTaken() {
      RegisterRequest request = buildRequest();
      when(userRepository.existsByEmail(UserDataProvider.VALID_EMAIL)).thenReturn(true);

      assertThatThrownBy(() -> authServiceImpl.registerUser(request))
          .isInstanceOf(ResourceConflictException.class);

      verify(authMapper, never()).toRegisterResponse(org.mockito.ArgumentMatchers.any());
    }
  }
}
