package com.group4.swissrouteapi.repositories;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.group4.swissrouteapi.AbstractIntegrationTest;
import com.group4.swissrouteapi.models.SearchHistoryEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.providers.SearchHistoryDataProvider;
import com.group4.swissrouteapi.providers.UserDataProvider;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class SearchHistoryRepositoryTest extends AbstractIntegrationTest {

  @Autowired SearchHistoryRepository searchHistoryRepository;
  @Autowired UserRepository userRepository;

  private UserEntity userA;
  private UserEntity userB;

  @BeforeEach
  void setUp() {
    searchHistoryRepository.deleteAll();
    userRepository.deleteAll();
    userA = userRepository.save(UserDataProvider.createMockUser());
    userB = userRepository.save(UserDataProvider.createAnotherMockUser());
  }

  // ─── findByUserId ────────────────────────────────────────────────────────────

  @Test
  void shouldReturnPagedHistoryForUser() {
    searchHistoryRepository.save(SearchHistoryDataProvider.createMockHistory(userA));
    searchHistoryRepository.save(SearchHistoryDataProvider.createMockHistory(userA));

    Page<SearchHistoryEntity> result =
        searchHistoryRepository.findByUserId(userA.getId(), PageRequest.of(0, 10));

    assertEquals(2, result.getTotalElements());
  }

  @Test
  void shouldReturnEmptyPageWhenUserHasNoHistory() {
    searchHistoryRepository.save(SearchHistoryDataProvider.createMockHistory(userB));

    Page<SearchHistoryEntity> result =
        searchHistoryRepository.findByUserId(userA.getId(), PageRequest.of(0, 10));

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnEmptyPageWhenRepositoryIsEmpty() {
    Page<SearchHistoryEntity> result =
        searchHistoryRepository.findByUserId(userA.getId(), PageRequest.of(0, 10));

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnOnlyHistoryBelongingToRequestedUser() {
    SearchHistoryEntity historyA =
        searchHistoryRepository.save(SearchHistoryDataProvider.createMockHistory(userA));
    searchHistoryRepository.save(SearchHistoryDataProvider.createMockHistory(userB));

    Page<SearchHistoryEntity> result =
        searchHistoryRepository.findByUserId(userA.getId(), PageRequest.of(0, 10));

    assertEquals(1, result.getTotalElements());
    assertEquals(historyA.getId(), result.getContent().getFirst().getId());
  }

  @Test
  void shouldRespectPaginationLimits() {
    for (int i = 0; i < 5; i++) {
      searchHistoryRepository.save(SearchHistoryDataProvider.createMockHistory(userA));
    }

    Page<SearchHistoryEntity> firstPage =
        searchHistoryRepository.findByUserId(userA.getId(), PageRequest.of(0, 3));
    Page<SearchHistoryEntity> secondPage =
        searchHistoryRepository.findByUserId(userA.getId(), PageRequest.of(1, 3));

    assertEquals(3, firstPage.getNumberOfElements());
    assertEquals(2, secondPage.getNumberOfElements());
    assertEquals(5, firstPage.getTotalElements());
  }

  // ─── findByIdAndUserId ───────────────────────────────────────────────────────

  @Test
  void shouldReturnHistoryWhenIdAndUserIdMatch() {
    SearchHistoryEntity saved =
        searchHistoryRepository.save(SearchHistoryDataProvider.createMockHistory(userA));

    Optional<SearchHistoryEntity> result =
        searchHistoryRepository.findByIdAndUserId(saved.getId(), userA.getId());

    assertTrue(result.isPresent());
    assertEquals(saved.getId(), result.get().getId());
    assertEquals(saved.getOrigin(), result.get().getOrigin());
    assertEquals(saved.getDestination(), result.get().getDestination());
  }

  @Test
  void shouldReturnEmptyWhenHistoryIdDoesNotExist() {
    Optional<SearchHistoryEntity> result =
        searchHistoryRepository.findByIdAndUserId(UUID.randomUUID(), userA.getId());

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnEmptyWhenHistoryBelongsToAnotherUser() {
    SearchHistoryEntity saved =
        searchHistoryRepository.save(SearchHistoryDataProvider.createMockHistory(userA));

    Optional<SearchHistoryEntity> result =
        searchHistoryRepository.findByIdAndUserId(saved.getId(), userB.getId());

    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnCorrectEntryAmongMultiple() {
    SearchHistoryEntity historyA1 =
        searchHistoryRepository.save(SearchHistoryDataProvider.createMockHistory(userA));
    SearchHistoryEntity historyA2 =
        searchHistoryRepository.save(SearchHistoryDataProvider.createAnotherMockHistory(userA));

    Optional<SearchHistoryEntity> result =
        searchHistoryRepository.findByIdAndUserId(historyA1.getId(), userA.getId());

    assertTrue(result.isPresent());
    assertEquals(historyA1.getId(), result.get().getId());
    assertNotEquals(historyA2.getId(), result.get().getId());
  }

  // ─── deleteByUserId ──────────────────────────────────────────────────────────

  @Test
  void shouldDeleteAllHistoryForGivenUser() {
    searchHistoryRepository.save(SearchHistoryDataProvider.createMockHistory(userA));
    searchHistoryRepository.save(SearchHistoryDataProvider.createMockHistory(userA));

    searchHistoryRepository.deleteByUserId(userA.getId());

    Page<SearchHistoryEntity> result =
        searchHistoryRepository.findByUserId(userA.getId(), PageRequest.of(0, 10));
    assertTrue(result.isEmpty());
  }

  @Test
  void shouldNotDeleteHistoryOfOtherUsers() {
    searchHistoryRepository.save(SearchHistoryDataProvider.createMockHistory(userA));
    SearchHistoryEntity historyB =
        searchHistoryRepository.save(SearchHistoryDataProvider.createMockHistory(userB));

    searchHistoryRepository.deleteByUserId(userA.getId());

    Page<SearchHistoryEntity> result =
        searchHistoryRepository.findByUserId(userB.getId(), PageRequest.of(0, 10));
    assertEquals(1, result.getTotalElements());
    assertEquals(historyB.getId(), result.getContent().getFirst().getId());
  }

  @Test
  void shouldNotFailWhenDeletingHistoryForUserWithNoRecords() {
    searchHistoryRepository.save(SearchHistoryDataProvider.createMockHistory(userB));

    assertDoesNotThrow(() -> searchHistoryRepository.deleteByUserId(userA.getId()));
  }

  @Test
  void shouldLeaveRepositoryEmptyWhenAllHistoriesAreDeleted() {
    searchHistoryRepository.save(SearchHistoryDataProvider.createMockHistory(userA));
    searchHistoryRepository.save(SearchHistoryDataProvider.createMockHistory(userB));

    searchHistoryRepository.deleteByUserId(userA.getId());
    searchHistoryRepository.deleteByUserId(userB.getId());

    assertEquals(0, searchHistoryRepository.count());
  }
}
