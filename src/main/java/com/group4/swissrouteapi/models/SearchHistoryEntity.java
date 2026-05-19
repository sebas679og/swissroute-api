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
 * Represents a user's search history within the system.
 *
 * <p>This entity maps to the {@code search_history} table and stores details of searches performed
 * by a user, including origin, destination, timestamp, and the number of results returned. It
 * maintains a relationship with {@link UserEntity} to associate each search entry with its owner.
 *
 * <p>Designed for persistence and retrieval using JPA/Hibernate, ensuring accurate tracking of user
 * activity and preferences.
 */
@Entity
@Table(name = "search_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistoryEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Column(nullable = false)
  private String origin;

  @Column(nullable = false)
  private String destination;

  @Column(name = "searched_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
  private Instant searchedAt;

  @Column(name = "result_count")
  private Integer resultCount;

  @PrePersist
  protected void onCreate() {
    this.searchedAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
  }
}
