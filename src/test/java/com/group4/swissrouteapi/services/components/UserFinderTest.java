package com.group4.swissrouteapi.services.components;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.group4.swissrouteapi.exceptions.NotFoundException;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.repositories.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserFinder")
class UserFinderTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private UserFinder userFinder;

  // -------------------------------------------------------------------------
  // Fixtures
  // -------------------------------------------------------------------------

  private static final UUID USER_ID = UUID.randomUUID();

  private UserEntity buildUser() {
    UserEntity user = UserEntity.builder().build();
    user.setId(USER_ID);
    return user;
  }

  // -------------------------------------------------------------------------
  // findById — happy path
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("findById — user exists")
  class FindByIdUserExists {

    @Test
    @DisplayName("returns the UserEntity when the user exists")
    void returnsUserEntity_whenUserExists() {
      UserEntity expected = buildUser();
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(expected));

      UserEntity result = userFinder.findById(USER_ID);

      assertThat(result).isSameAs(expected);
    }

    @Test
    @DisplayName("delegates to repository with the exact userId provided")
    void delegatesToRepository_withExactUserId() {
      UserEntity user = buildUser();
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

      userFinder.findById(USER_ID);

      verify(userRepository).findById(USER_ID);
    }

    @Test
    @DisplayName("calls repository exactly once per invocation")
    void callsRepository_exactlyOnce() {
      UserEntity user = buildUser();
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

      userFinder.findById(USER_ID);

      verify(userRepository, times(1)).findById(any());
    }
  }

  // -------------------------------------------------------------------------
  // findById — user not found
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("findById — user not found")
  class FindByIdUserNotFound {

    @Test
    @DisplayName("throws NotFoundException when no user exists for the given id")
    void throwsNotFoundException_whenNoUserExists() {
      when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> userFinder.findById(USER_ID)).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("exception message is 'User not found'")
    void exceptionMessage_isUserNotFound() {
      when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> userFinder.findById(USER_ID))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("User not found");
    }

    @Test
    @DisplayName("still delegates to repository before throwing")
    void stillDelegatesToRepository_beforeThrowing() {
      when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> userFinder.findById(USER_ID)).isInstanceOf(NotFoundException.class);

      verify(userRepository).findById(USER_ID);
    }

    @Test
    @DisplayName("throws NotFoundException for every distinct userId that does not exist")
    void throwsNotFoundException_forEveryDistinctMissingUserId() {
      UUID anotherId = UUID.randomUUID();
      when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
      when(userRepository.findById(anotherId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> userFinder.findById(USER_ID)).isInstanceOf(NotFoundException.class);
      assertThatThrownBy(() -> userFinder.findById(anotherId))
          .isInstanceOf(NotFoundException.class);
    }
  }

  // -------------------------------------------------------------------------
  // findById — no extra interactions
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("findById — side-effect isolation")
  class FindByIdSideEffectIsolation {

    @Test
    @DisplayName("has no interactions beyond findById on the repository")
    void hasNoExtraInteractions_onRepository() {
      UserEntity user = buildUser();
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

      userFinder.findById(USER_ID);

      verifyNoMoreInteractions(userRepository);
    }
  }
}
