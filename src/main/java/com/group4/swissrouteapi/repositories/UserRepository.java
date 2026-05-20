package com.group4.swissrouteapi.repositories;

import com.group4.swissrouteapi.models.UserEntity;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * UserRepository
 *
 * <p>Repository interface for managing persistence operations on {@link UserEntity} objects.
 *
 * <p>Extends {@link org.springframework.data.jpa.repository.JpaRepository} to provide standard CRUD
 * functionality and query derivation based on method names.
 *
 * <p>This repository is a Spring Data JPA component and will be automatically implemented by the
 * framework at runtime.
 */
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

  boolean existsByEmail(String email);

  Optional<UserEntity> findByEmail(String email);
}
