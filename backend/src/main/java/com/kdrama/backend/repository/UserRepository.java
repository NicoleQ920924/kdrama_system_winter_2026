package com.kdrama.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kdrama.backend.model.Drama;
import com.kdrama.backend.model.Movie;
import com.kdrama.backend.model.User;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    List<User> findByWatchedDramasContaining(Drama drama);
    List<User> findByWatchedMoviesContaining(Movie movie);
}
