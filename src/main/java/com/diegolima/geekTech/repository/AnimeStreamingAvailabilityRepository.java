package com.diegolima.geekTech.repository;

import com.diegolima.geekTech.models.AnimeStreamingAvailability;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnimeStreamingAvailabilityRepository extends JpaRepository<AnimeStreamingAvailability, Long> {
    boolean existsByAnimeIdAndStreamingPlatformId(Long animeId, Long streamingPlatformId);

    @EntityGraph(attributePaths = {"anime", "streamingPlatform"})
    List<AnimeStreamingAvailability> findByAnimeId(Long animeId);

    @EntityGraph(attributePaths = {"anime", "streamingPlatform"})
    Optional<AnimeStreamingAvailability> findByIdAndAnimeId(Long id, Long animeId);
}
