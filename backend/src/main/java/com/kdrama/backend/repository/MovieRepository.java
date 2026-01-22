package com.kdrama.backend.repository;

import com.kdrama.backend.model.Movie;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer>{
    // Already includes CRUD (Create, Read, Update, Delete) methods
    // Called by MovieService.java

    Optional<Movie> findByTmdbId(Integer tmdbId);
    Optional<Movie> findByChineseName(String chineseName);
}
