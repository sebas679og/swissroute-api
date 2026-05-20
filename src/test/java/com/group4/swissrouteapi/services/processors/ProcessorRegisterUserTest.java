package com.group4.swissrouteapi.services.processors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link ProcessorRegisterUser}.
 *
 * <p>Verifies that {@code userRegister} correctly builds a {@link UserEntity} from the provided
 * arguments and delegates persistence to {@link UserRepository#save}. The repository is mocked so
 * no database is required.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessorRegisterUser")
class ProcessorRegisterUserTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private ProcessorRegisterUser processorRegisterUser;

  // ---------------------------------------------------------------------------
  // Shared fixtures
  // ---------------------------------------------------------------------------

  private static final String NAME = "John Doe";
  private static final String EMAIL = "john.doe@example.com";
  private static final String PASSWORD = "Secure@123";
  private static final String BASE_CITY = "Bogotá";

  private UserEntity savedEntity() {
    return UserEntity.builder()
        .name(NAME)
        .email(EMAIL)
        .password(PASSWORD)
        .baseCity(BASE_CITY)
        .build();
  }

  // ---------------------------------------------------------------------------
  // userRegister — happy path
  // ---------------------------------------------------------------------------

  @Nested
  @DisplayName("userRegister()")
  class UserRegisterTest {

    @Test
    @DisplayName("should return the entity returned by the repository")
    void shouldReturnEntityFromRepository() {
      UserEntity persisted = savedEntity();
      when(userRepository.save(org.mockito.ArgumentMatchers.any(UserEntity.class)))
          .thenReturn(persisted);

      UserEntity result = processorRegisterUser.userRegister(NAME, EMAIL, PASSWORD, BASE_CITY);

      assertThat(result).isSameAs(persisted);
    }

    @Test
    @DisplayName("should delegate persistence to the repository exactly once")
    void shouldCallRepositorySaveExactlyOnce() {
      when(userRepository.save(org.mockito.ArgumentMatchers.any(UserEntity.class)))
          .thenReturn(savedEntity());

      processorRegisterUser.userRegister(NAME, EMAIL, PASSWORD, BASE_CITY);

      verify(userRepository).save(org.mockito.ArgumentMatchers.any(UserEntity.class));
    }

    @Test
    @DisplayName("should build the entity with the provided name")
    void shouldBuildEntityWithCorrectName() {
      when(userRepository.save(org.mockito.ArgumentMatchers.any(UserEntity.class)))
          .thenReturn(savedEntity());

      ArgumentCaptor<UserEntity> captor = forClass(UserEntity.class);

      processorRegisterUser.userRegister(NAME, EMAIL, PASSWORD, BASE_CITY);
      verify(userRepository).save(captor.capture());

      assertThat(captor.getValue().getName()).isEqualTo(NAME);
    }

    @Test
    @DisplayName("should build the entity with the provided email")
    void shouldBuildEntityWithCorrectEmail() {
      when(userRepository.save(org.mockito.ArgumentMatchers.any(UserEntity.class)))
          .thenReturn(savedEntity());

      ArgumentCaptor<UserEntity> captor = forClass(UserEntity.class);

      processorRegisterUser.userRegister(NAME, EMAIL, PASSWORD, BASE_CITY);
      verify(userRepository).save(captor.capture());

      assertThat(captor.getValue().getEmail()).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("should build the entity with the provided password")
    void shouldBuildEntityWithCorrectPassword() {
      when(userRepository.save(org.mockito.ArgumentMatchers.any(UserEntity.class)))
          .thenReturn(savedEntity());

      ArgumentCaptor<UserEntity> captor = forClass(UserEntity.class);

      processorRegisterUser.userRegister(NAME, EMAIL, PASSWORD, BASE_CITY);
      verify(userRepository).save(captor.capture());

      assertThat(captor.getValue().getPassword()).isEqualTo(PASSWORD);
    }

    @Test
    @DisplayName("should build the entity with the provided base city")
    void shouldBuildEntityWithCorrectBaseCity() {
      when(userRepository.save(org.mockito.ArgumentMatchers.any(UserEntity.class)))
          .thenReturn(savedEntity());

      ArgumentCaptor<UserEntity> captor = forClass(UserEntity.class);

      processorRegisterUser.userRegister(NAME, EMAIL, PASSWORD, BASE_CITY);
      verify(userRepository).save(captor.capture());

      assertThat(captor.getValue().getBaseCity()).isEqualTo(BASE_CITY);
    }

    @Test
    @DisplayName("should propagate exceptions thrown by the repository")
    void shouldPropagateRepositoryException() {
      when(userRepository.save(org.mockito.ArgumentMatchers.any(UserEntity.class)))
          .thenThrow(new RuntimeException("DB unavailable"));

      assertThatThrownBy(() -> processorRegisterUser.userRegister(NAME, EMAIL, PASSWORD, BASE_CITY))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("DB unavailable");
    }
  }
}
