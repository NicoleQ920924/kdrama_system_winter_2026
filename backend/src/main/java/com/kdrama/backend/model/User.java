package com.kdrama.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashSet;
import java.util.Set;

import com.kdrama.backend.enums.Role;

@Data
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(unique = true)
    private String username;

    private String displayName;

    @Enumerated(EnumType.STRING)
    private Role role;

    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "user_watched_movies",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "movie_id"))
    private Set<Movie> watchedMovies = new HashSet<>();

    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "user_watched_dramas",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "drama_id"))
    private Set<Drama> watchedDramas = new HashSet<>();
}
