package com.kdrama.backend.repository;

import com.kdrama.backend.model.Drama;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DramaRepository extends JpaRepository<Drama, Integer> {
    // Already includes CRUD (Create, Read, Update, Delete) methods
    // Called by DramaService.java
    
    List<Drama> findByTmdbId(Integer tmdbId);
    Optional<List<Drama>> findByChineseName(String chineseName);
}
