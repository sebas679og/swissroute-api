package com.group4.swissrouteapi.services.processors;

import com.group4.swissrouteapi.exceptions.NotFoundException;
import com.group4.swissrouteapi.models.SearchHistoryEntity;
import com.group4.swissrouteapi.models.UserEntity;
import com.group4.swissrouteapi.repositories.SearchHistoryRepository;
import com.group4.swissrouteapi.repositories.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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

  private UserEntity searchUser(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));
  }
}
