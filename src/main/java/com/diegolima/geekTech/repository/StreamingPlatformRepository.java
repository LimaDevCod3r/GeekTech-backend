package com.diegolima.geekTech.repository;

import com.diegolima.geekTech.models.StreamingPlatform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StreamingPlatformRepository extends JpaRepository<StreamingPlatform, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    Page<StreamingPlatform> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
