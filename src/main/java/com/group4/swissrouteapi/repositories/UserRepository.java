package com.group4.swissrouteapi.repositories;

import com.group4.swissrouteapi.models.UserEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

  boolean existsByEmail(String email);
}
