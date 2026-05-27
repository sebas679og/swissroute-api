package com.group4.swissrouteapi.models;

import com.group4.swissrouteapi.utils.enums.TransportType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a user's favorite route within the system.
 *
 * <p>This entity maps to the {@code favorite_routes} table and stores information about routes
 * saved by a user, including name, origin, destination, and transport type. It maintains a
 * relationship with {@link UserEntity} to associate each route with its owner.
 *
 * <p>Designed for persistence and retrieval using JPA/Hibernate, ensuring proper management of
 * user-defined travel preferences.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "favorite_routes",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_favorite_routes_user_name",
            columnNames = {"user_id", "name"}))
public class FavoriteRouteEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String origin;

  @Column(nullable = false)
  private String destination;

  @Column(name = "transport_type")
  @Enumerated(EnumType.STRING)
  private TransportType transportType;

  @Column(
      name = "created_at",
      nullable = false,
      updatable = false,
      columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
  }
}
