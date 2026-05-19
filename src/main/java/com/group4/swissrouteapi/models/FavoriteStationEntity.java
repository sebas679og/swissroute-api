package com.group4.swissrouteapi.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a user's favorite station within the system.
 *
 * <p>This entity maps to the {@code favorite_stations} table and stores information about stations
 * marked as favorites by a user. It maintains a relationship with {@link UserEntity} to associate
 * each station with its owner.
 *
 * <p>Designed for persistence and retrieval using JPA/Hibernate, ensuring proper management of user
 * preferences and station data.
 */
@Entity
@Table(name = "favorite_stations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteStationEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Column(name = "external_station_id", nullable = false)
  private String externalStationId;

  @Column(name = "station_name", nullable = false)
  private String stationName;

  @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant createdAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
  }
}
