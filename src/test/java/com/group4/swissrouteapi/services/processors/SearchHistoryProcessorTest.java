package com.group4.swissrouteapi.services.processors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.group4.swissrouteapi.UserDataProvider;
import com.group4.swissrouteapi.exceptions.NotFoundException;
import com.group4.swissrouteapi.models.SearchHistoryEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.repositories.SearchHistoryRepository;
import com.group4.swissrouteapi.repositories.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link SearchHistoryProcessor}.
 *
 * <p>Verifies that {@code saveSearchHistory} correctly looks up the user, builds the {@link
 * SearchHistoryEntity} with all provided fields, delegates persistence to the repository, and
 * throws {@link NotFoundException} when the user does not exist.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SearchHistoryProcessor")
class SearchHistoryProcessorTest {

  @Mock private UserRepository userRepository;
  @Mock private SearchHistoryRepository searchHistoryRepository;

  @InjectMocks private SearchHistoryProcessor searchHistoryProcessor;

  // ---------------------------------------------------------------------------
  // Shared fixtures
  // ---------------------------------------------------------------------------

  private static final UUID USER_ID = UUID.randomUUID();
  private static final String FROM = "Lausanne";
  private static final String TO = "Genève";
  private static final Integer RESULT_COUNT = 4;

  private final UserEntity buildUser = UserDataProvider.createMockUserLogin();

  // ===========================================================================
  // saveSearchHistory — successful save
  // ===========================================================================

  @Nested
  @DisplayName("saveSearchHistory() - successful save")
  class SuccessfulSaveTest {

    @Test
    @DisplayName("should look up the user by the provided userId")
    void shouldLookUpUserByUserId() {
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(buildUser));

      searchHistoryProcessor.saveSearchHistory(FROM, TO, RESULT_COUNT, USER_ID);

      verify(userRepository).findById(USER_ID);
    }

    @Test
    @DisplayName("should save the search history entity exactly once")
    void shouldSaveSearchHistoryOnce() {
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(buildUser));

      searchHistoryProcessor.saveSearchHistory(FROM, TO, RESULT_COUNT, USER_ID);

      verify(searchHistoryRepository).save(any(SearchHistoryEntity.class));
    }

    @Test
    @DisplayName("should save the entity with the correct origin")
    void shouldSaveEntityWithCorrectOrigin() {
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(buildUser));

      ArgumentCaptor<SearchHistoryEntity> captor = forClass(SearchHistoryEntity.class);
      searchHistoryProcessor.saveSearchHistory(FROM, TO, RESULT_COUNT, USER_ID);
      verify(searchHistoryRepository).save(captor.capture());

      assertThat(captor.getValue().getOrigin()).isEqualTo(FROM);
    }

    @Test
    @DisplayName("should save the entity with the correct destination")
    void shouldSaveEntityWithCorrectDestination() {
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(buildUser));

      ArgumentCaptor<SearchHistoryEntity> captor = forClass(SearchHistoryEntity.class);
      searchHistoryProcessor.saveSearchHistory(FROM, TO, RESULT_COUNT, USER_ID);
      verify(searchHistoryRepository).save(captor.capture());

      assertThat(captor.getValue().getDestination()).isEqualTo(TO);
    }

    @Test
    @DisplayName("should save the entity with the correct result count")
    void shouldSaveEntityWithCorrectResultCount() {
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(buildUser));

      ArgumentCaptor<SearchHistoryEntity> captor = forClass(SearchHistoryEntity.class);
      searchHistoryProcessor.saveSearchHistory(FROM, TO, RESULT_COUNT, USER_ID);
      verify(searchHistoryRepository).save(captor.capture());

      assertThat(captor.getValue().getResultCount()).isEqualTo(RESULT_COUNT);
    }

    @Test
    @DisplayName("should save the entity associated with the looked-up user")
    void shouldSaveEntityWithCorrectUser() {
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(buildUser));

      ArgumentCaptor<SearchHistoryEntity> captor = forClass(SearchHistoryEntity.class);
      searchHistoryProcessor.saveSearchHistory(FROM, TO, RESULT_COUNT, USER_ID);
      verify(searchHistoryRepository).save(captor.capture());

      assertThat(captor.getValue().getUser()).isSameAs(buildUser);
    }

    @Test
    @DisplayName("should pass zero as result count when no results were found")
    void shouldSaveEntityWithZeroResultCount() {
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(buildUser));

      ArgumentCaptor<SearchHistoryEntity> captor = forClass(SearchHistoryEntity.class);
      searchHistoryProcessor.saveSearchHistory(FROM, TO, 0, USER_ID);
      verify(searchHistoryRepository).save(captor.capture());

      assertThat(captor.getValue().getResultCount()).isZero();
    }
  }

  // ===========================================================================
  // saveSearchHistory — user not found
  // ===========================================================================

  @Nested
  @DisplayName("saveSearchHistory() - user not found")
  class UserNotFoundTest {

    @Test
    @DisplayName("should throw NotFoundException when the user does not exist")
    void shouldThrowNotFoundWhenUserDoesNotExist() {
      when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(
              () -> searchHistoryProcessor.saveSearchHistory(FROM, TO, RESULT_COUNT, USER_ID))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("User not found");
    }

    @Test
    @DisplayName("should not save anything when the user does not exist")
    void shouldNotSaveWhenUserDoesNotExist() {
      when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(
              () -> searchHistoryProcessor.saveSearchHistory(FROM, TO, RESULT_COUNT, USER_ID))
          .isInstanceOf(NotFoundException.class);

      verify(searchHistoryRepository, never()).save(any());
    }
  }
}
