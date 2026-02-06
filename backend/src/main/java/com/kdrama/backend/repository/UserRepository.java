package com.kdrama.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.kdrama.backend.model.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
}
