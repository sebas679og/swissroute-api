package com.group4.swissrouteapi.services.processors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.group4.swissrouteapi.UserDataProvider;
import com.group4.swissrouteapi.exceptions.NotFoundException;
import com.group4.swissrouteapi.models.SearchHistoryEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.repositories.SearchHistoryRepository;
import com.group4.swissrouteapi.repositories.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * Unit tests for {@link HistoryProcessor}.
 *
 * <p>Verifies that {@code saveHistory} correctly looks up the user, builds the {@link
 * SearchHistoryEntity} with all provided fields, delegates persistence to the repository, and
 * throws {@link NotFoundException} when the user does not exist.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SearchHistoryProcessor")
class HistoryProcessorTest {

  @Mock private UserRepository userRepository;
  @Mock private SearchHistoryRepository searchHistoryRepository;

  @InjectMocks private HistoryProcessor historyProcessor;

  // ---------------------------------------------------------------------------
  // Shared fixtures
  // ---------------------------------------------------------------------------

  private static final UUID USER_ID = UUID.randomUUID();
  private static final String FROM = "Lausanne";
  private static final String TO = "Genève";
  private static final Integer RESULT_COUNT = 4;

  private final UserEntity buildUser = UserDataProvider.createMockUserLogin();
  private static final UUID ITEM_ID = UUID.randomUUID();

  private UserEntity buildUser(UUID id) {
    UserEntity user = new UserEntity();
    user.setId(id);
    return user;
  }

  private SearchHistoryEntity buildEntity(UserEntity user) {
    return SearchHistoryEntity.builder()
        .id(ITEM_ID)
        .user(user)
        .origin("Zurich")
        .destination("Bern")
        .resultCount(3)
        .searchedAt(Instant.now().truncatedTo(ChronoUnit.MILLIS))
        .build();
  }

  // ===========================================================================
  // saveHistory — successful save
  // ===========================================================================

  @Nested
  @DisplayName("saveHistory() - successful save")
  class SuccessfulSaveTest {

    @Test
    @DisplayName("should look up the user by the provided userId")
    void shouldLookUpUserByUserId() {
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(buildUser));

      historyProcessor.saveHistory(FROM, TO, RESULT_COUNT, USER_ID);

      verify(userRepository).findById(USER_ID);
    }

    @Test
    @DisplayName("should save the search history entity exactly once")
    void shouldSaveHistoryOnce() {
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(buildUser));

      historyProcessor.saveHistory(FROM, TO, RESULT_COUNT, USER_ID);

      verify(searchHistoryRepository).save(any(SearchHistoryEntity.class));
    }

    @Test
    @DisplayName("should save the entity with the correct origin")
    void shouldSaveEntityWithCorrectOrigin() {
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(buildUser));

      ArgumentCaptor<SearchHistoryEntity> captor = forClass(SearchHistoryEntity.class);
      historyProcessor.saveHistory(FROM, TO, RESULT_COUNT, USER_ID);
      verify(searchHistoryRepository).save(captor.capture());

      assertThat(captor.getValue().getOrigin()).isEqualTo(FROM);
    }

    @Test
    @DisplayName("should save the entity with the correct destination")
    void shouldSaveEntityWithCorrectDestination() {
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(buildUser));

      ArgumentCaptor<SearchHistoryEntity> captor = forClass(SearchHistoryEntity.class);
      historyProcessor.saveHistory(FROM, TO, RESULT_COUNT, USER_ID);
      verify(searchHistoryRepository).save(captor.capture());

      assertThat(captor.getValue().getDestination()).isEqualTo(TO);
    }

    @Test
    @DisplayName("should save the entity with the correct result count")
    void shouldSaveEntityWithCorrectResultCount() {
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(buildUser));

      ArgumentCaptor<SearchHistoryEntity> captor = forClass(SearchHistoryEntity.class);
      historyProcessor.saveHistory(FROM, TO, RESULT_COUNT, USER_ID);
      verify(searchHistoryRepository).save(captor.capture());

      assertThat(captor.getValue().getResultCount()).isEqualTo(RESULT_COUNT);
    }

    @Test
    @DisplayName("should save the entity associated with the looked-up user")
    void shouldSaveEntityWithCorrectUser() {
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(buildUser));

      ArgumentCaptor<SearchHistoryEntity> captor = forClass(SearchHistoryEntity.class);
      historyProcessor.saveHistory(FROM, TO, RESULT_COUNT, USER_ID);
      verify(searchHistoryRepository).save(captor.capture());

      assertThat(captor.getValue().getUser()).isSameAs(buildUser);
    }

    @Test
    @DisplayName("should pass zero as result count when no results were found")
    void shouldSaveEntityWithZeroResultCount() {
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(buildUser));

      ArgumentCaptor<SearchHistoryEntity> captor = forClass(SearchHistoryEntity.class);
      historyProcessor.saveHistory(FROM, TO, 0, USER_ID);
      verify(searchHistoryRepository).save(captor.capture());

      assertThat(captor.getValue().getResultCount()).isZero();
    }
  }

  // ===========================================================================
  // saveHistory — user not found
  // ===========================================================================

  @Nested
  @DisplayName("saveHistory() - user not found")
  class UserNotFoundTest {

    @Test
    @DisplayName("should throw NotFoundException when the user does not exist")
    void shouldThrowNotFoundWhenUserDoesNotExist() {
      when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> historyProcessor.saveHistory(FROM, TO, RESULT_COUNT, USER_ID))
          .isInstanceOf(NotFoundException.class)
          .hasMessage("User not found");
    }

    @Test
    @DisplayName("should not save anything when the user does not exist")
    void shouldNotSaveWhenUserDoesNotExist() {
      when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> historyProcessor.saveHistory(FROM, TO, RESULT_COUNT, USER_ID))
          .isInstanceOf(NotFoundException.class);

      verify(searchHistoryRepository, never()).save(any());
    }
  }

  // -------------------------------------------------------------------------
  // getAllHistoryByUserId
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getAllHistoryByUserId")
  class GetAllHistoryByUserId {

    @Test
    @DisplayName("returns page from repository for valid user")
    void returnsPageFromRepository_forValidUser() {
      UserEntity user = buildUser(USER_ID);
      SearchHistoryEntity entity = buildEntity(user);
      Page<SearchHistoryEntity> expectedPage = new PageImpl<>(List.of(entity));

      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
      when(searchHistoryRepository.findByUserId(eq(USER_ID), any(Pageable.class)))
          .thenReturn(expectedPage);

      Page<SearchHistoryEntity> result = historyProcessor.getAllHistoryByUserId(USER_ID, 1, 20);

      assertThat(result).isSameAs(expectedPage);
    }

    @Test
    @DisplayName("builds pageable with descending searchedAt sort and 0-based page index")
    void buildsPageable_withDescendingSort_andZeroBasedIndex() {
      UserEntity user = buildUser(USER_ID);
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
      when(searchHistoryRepository.findByUserId(eq(USER_ID), any(Pageable.class)))
          .thenReturn(Page.empty());

      historyProcessor.getAllHistoryByUserId(USER_ID, 3, 10);

      ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
      verify(searchHistoryRepository).findByUserId(eq(USER_ID), pageableCaptor.capture());

      Pageable captured = pageableCaptor.getValue();
      assertThat(captured.getPageNumber()).isEqualTo(2); // 1-based input → 0-based
      assertThat(captured.getPageSize()).isEqualTo(10);
      assertThat(captured.getSort().getOrderFor("searchedAt")).isNotNull();
      assertThat(
              Objects.requireNonNull(captured.getSort().getOrderFor("searchedAt")).getDirection())
          .isEqualTo(org.springframework.data.domain.Sort.Direction.DESC);
    }

    @Test
    @DisplayName("throws NotFoundException when user does not exist")
    void throwsNotFoundException_whenUserDoesNotExist() {
      when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> historyProcessor.getAllHistoryByUserId(USER_ID, 1, 20))
          .isInstanceOf(NotFoundException.class)
          .hasMessageContaining("User not found");

      verifyNoInteractions(searchHistoryRepository);
    }

    @Test
    @DisplayName("passes user's id (not the UUID argument) to repository query")
    void passesUserEntityId_toRepositoryQuery() {
      UUID persistedId = UUID.randomUUID();
      UserEntity user = buildUser(persistedId);
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
      when(searchHistoryRepository.findByUserId(eq(persistedId), any(Pageable.class)))
          .thenReturn(Page.empty());

      historyProcessor.getAllHistoryByUserId(USER_ID, 1, 20);

      verify(searchHistoryRepository).findByUserId(eq(persistedId), any(Pageable.class));
    }
  }

  // -------------------------------------------------------------------------
  // deleteHistoryItem
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("deleteHistoryItem")
  class DeleteHistoryItem {

    @Test
    @DisplayName("deletes the entity when it exists and belongs to the user")
    void deletesEntity_whenItExistsAndBelongsToUser() {
      UserEntity user = buildUser(USER_ID);
      SearchHistoryEntity entity = buildEntity(user);

      when(searchHistoryRepository.findByIdAndUserId(ITEM_ID, USER_ID))
          .thenReturn(Optional.of(entity));

      historyProcessor.deleteHistoryItem(ITEM_ID, USER_ID);

      verify(searchHistoryRepository).delete(entity);
    }

    @Test
    @DisplayName("throws NotFoundException when history item does not belong to user")
    void throwsNotFoundException_whenItemDoesNotBelongToUser() {
      when(searchHistoryRepository.findByIdAndUserId(ITEM_ID, USER_ID))
          .thenReturn(Optional.empty());

      assertThatThrownBy(() -> historyProcessor.deleteHistoryItem(ITEM_ID, USER_ID))
          .isInstanceOf(NotFoundException.class)
          .hasMessageContaining("History item not found");

      verify(searchHistoryRepository, never()).delete(any());
    }

    @Test
    @DisplayName("throws NotFoundException when history item does not exist")
    void throwsNotFoundException_whenItemDoesNotExist() {
      when(searchHistoryRepository.findByIdAndUserId(ITEM_ID, USER_ID))
          .thenReturn(Optional.empty());

      assertThatThrownBy(() -> historyProcessor.deleteHistoryItem(ITEM_ID, USER_ID))
          .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("does not interact with userRepository during item deletion")
    void doesNotInteractWithUserRepository_duringItemDeletion() {
      UserEntity user = buildUser(USER_ID);
      SearchHistoryEntity entity = buildEntity(user);
      when(searchHistoryRepository.findByIdAndUserId(ITEM_ID, USER_ID))
          .thenReturn(Optional.of(entity));

      historyProcessor.deleteHistoryItem(ITEM_ID, USER_ID);

      verifyNoInteractions(userRepository);
    }
  }

  // -------------------------------------------------------------------------
  // clearHistory
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("clearHistory")
  class ClearHistory {

    @Test
    @DisplayName("deletes all history for user when user exists")
    void deletesAllHistory_whenUserExists() {
      UserEntity user = buildUser(USER_ID);
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

      historyProcessor.clearHistory(USER_ID);

      verify(searchHistoryRepository).deleteByUserId(USER_ID);
    }

    @Test
    @DisplayName("passes user entity's id to repository deleteByUserId")
    void passesUserEntityId_toDeleteByUserId() {
      UUID persistedId = UUID.randomUUID();
      UserEntity user = buildUser(persistedId);
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

      historyProcessor.clearHistory(USER_ID);

      verify(searchHistoryRepository).deleteByUserId(persistedId);
    }

    @Test
    @DisplayName("throws NotFoundException when user does not exist")
    void throwsNotFoundException_whenUserDoesNotExist() {
      when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> historyProcessor.clearHistory(USER_ID))
          .isInstanceOf(NotFoundException.class)
          .hasMessageContaining("User not found");

      verifyNoInteractions(searchHistoryRepository);
    }

    @Test
    @DisplayName("calls deleteByUserId exactly once for a valid user")
    void callsDeleteByUserId_exactlyOnce_forValidUser() {
      UserEntity user = buildUser(USER_ID);
      when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

      historyProcessor.clearHistory(USER_ID);

      verify(searchHistoryRepository, times(1)).deleteByUserId(any());
    }
  }
}
