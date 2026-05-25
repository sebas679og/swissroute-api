package com.group4.swissrouteapi;

import com.group4.swissrouteapi.models.SearchHistoryEntity;
import com.group4.swissrouteapi.models.UserEntity;

/**
 * SearchHistoryDataProvider
 *
 * <p>Utility class for providing mock {@link SearchHistoryEntity} instances used in testing
 * scenarios.
 *
 * <p>Contains predefined origin and destination constants (e.g., Madrid → Paris, Barcelona →
 * London) and factory methods to create transient mock entities associated with a given {@link
 * UserEntity}.
 *
 * <p>Intended for use in unit tests or integration tests where sample search history data is
 * required.
 */
public class SearchHistoryDataProvider {

  public static final String ORIGIN_MADRID = "Madrid";
  public static final String DESTINATION_PARIS = "Paris";
  public static final String ORIGIN_BARCELONA = "Barcelona";
  public static final String DESTINATION_LONDON = "London";

  /**
   * Creates a mock {@link SearchHistoryEntity} for the given user.
   *
   * <p>Uses Madrid → Paris as the default route. {@code searchedAt} is set automatically by the
   * {@code @PrePersist} hook on persist.
   *
   * @param user the owning {@link UserEntity}
   * @return a transient mock entity ready to be saved
   */
  public static SearchHistoryEntity createMockHistory(UserEntity user) {
    return SearchHistoryEntity.builder()
        .user(user)
        .origin(ORIGIN_MADRID)
        .destination(DESTINATION_PARIS)
        .resultCount(5)
        .build();
  }

  /**
   * Creates an alternative mock {@link SearchHistoryEntity} with a different route.
   *
   * <p>Useful for scenarios that require two distinct history entries for the same user.
   *
   * @param user the owning {@link UserEntity}
   * @return a transient mock entity ready to be saved
   */
  public static SearchHistoryEntity createAnotherMockHistory(UserEntity user) {
    return SearchHistoryEntity.builder()
        .user(user)
        .origin(ORIGIN_BARCELONA)
        .destination(DESTINATION_LONDON)
        .resultCount(3)
        .build();
  }
}
