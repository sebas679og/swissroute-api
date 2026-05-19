package com.group4.swissrouteapi.repositories;

import com.group4.swissrouteapi.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
}
