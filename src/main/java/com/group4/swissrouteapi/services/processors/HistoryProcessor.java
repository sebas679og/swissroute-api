package com.group4.swissrouteapi.services.processors;

import com.group4.swissrouteapi.exceptions.NotFoundException;
import com.group4.swissrouteapi.models.SearchHistoryEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.repositories.SearchHistoryRepository;
import com.group4.swissrouteapi.repositories.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SearchHistoryProcessor
 *
 * <p>Spring service responsible for processing and persisting user search history.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Retrieves the {@link UserEntity} associated with a given user ID.
 *   <li>Builds and saves a {@link SearchHistoryEntity} with origin, destination, result count, and
 *       user association.
 *   <li>Ensures transactional integrity when persisting search history records.
 * </ul>
 *
 * <p>Annotated with {@link org.springframework.stereotype.Service} and {@link
 * lombok.RequiredArgsConstructor} for Spring integration and constructor-based dependency
 * injection.
 */
@Service
@RequiredArgsConstructor
public class HistoryProcessor {

  private final UserRepository userRepository;
  private final SearchHistoryRepository searchHistoryRepository;

  /**
   * Saves a search history entry for a given user.
   *
   * @param from origin station name
   * @param to destination station name
   * @param resultCount number of results returned by the search
   * @param userId unique identifier of the user performing the search
   * @throws NotFoundException if the user does not exist
   */
  @Transactional
  public void saveHistory(String from, String to, Integer resultCount, UUID userId) {
    UserEntity user = searchUser(userId);

    searchHistoryRepository.save(
        SearchHistoryEntity.builder()
            .user(user)
            .origin(from)
            .destination(to)
            .resultCount(resultCount)
            .build());
  }

  /**
   * Retrieves a paginated list of search history records for a given user.
   *
   * <p>Results are sorted by {@code searchedAt} in descending order to show the most recent
   * searches first.
   *
   * @param userId unique identifier of the user
   * @param page page number to retrieve (1-based index)
   * @param size number of records per page
   * @return a {@link org.springframework.data.domain.Page} of {@link SearchHistoryEntity}
   *     containing the user's search history
   * @throws NotFoundException if the user does not exist
   */
  @Transactional(readOnly = true)
  public Page<SearchHistoryEntity> getAllHistoryByUserId(UUID userId, Integer page, Integer size) {
    UserEntity user = searchUser(userId);
    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "searchedAt"));
    return searchHistoryRepository.findByUserId(user.getId(), pageable);
  }

  /**
   * Deletes a specific search history record for a given user.
   *
   * <p>Validates that the history item exists and belongs to the user before deletion. If no
   * matching record is found, a {@link NotFoundException} is thrown.
   *
   * @param itemId unique identifier of the history record to delete
   * @param userId unique identifier of the user who owns the record
   * @throws NotFoundException if the history item does not exist or does not belong to the user
   */
  @Transactional
  public void deleteHistoryItem(UUID itemId, UUID userId) {
    SearchHistoryEntity history =
        searchHistoryRepository
            .findByIdAndUserId(itemId, userId)
            .orElseThrow(() -> new NotFoundException("History item not found"));

    searchHistoryRepository.delete(history);
  }

  /**
   * Deletes all search history records for a given user.
   *
   * <p>Validates the existence of the user before performing the deletion. All records associated
   * with the user's ID are removed from the repository.
   *
   * @param userId unique identifier of the user whose history will be cleared
   * @throws NotFoundException if the user does not exist
   */
  @Transactional
  public void clearHistory(UUID userId) {
    UserEntity user = searchUser(userId);
    searchHistoryRepository.deleteByUserId(user.getId());
  }

  private UserEntity searchUser(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));
  }
}
