package com.group4.swissrouteapi.repositories;

import com.group4.swissrouteapi.models.SearchHistoryEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * SearchHistoryRepository
 *
 * <p>JPA repository interface for managing {@link SearchHistoryEntity} persistence. Provides CRUD
 * operations and query execution for search history records.
 */
public interface SearchHistoryRepository extends JpaRepository<SearchHistoryEntity, UUID> {}
