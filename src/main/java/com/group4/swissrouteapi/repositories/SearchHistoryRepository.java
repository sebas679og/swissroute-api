package com.group4.swissrouteapi.repositories;

import com.group4.swissrouteapi.models.SearchHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SearchHistoryRepository extends JpaRepository<SearchHistoryEntity, UUID> {
}
