package com.kdrama.backend.repository;

import com.kdrama.backend.model.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActorRepository extends JpaRepository<Actor, Integer>{
    // Already includes CRUD (Create, Read, Update, Delete) methods
    // Called by DramaService.java
    Optional<Actor> findByChineseName(String chineseName);
    List<Actor> findByDramasContaining(Drama drama);
    List<Actor> findByMoviesContaining(Movie movie);
}
