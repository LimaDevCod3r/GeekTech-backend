package com.diegolima.geekTech.repository;

import com.diegolima.geekTech.models.Anime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimeRepository extends JpaRepository<Anime, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    Page<Anime> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
