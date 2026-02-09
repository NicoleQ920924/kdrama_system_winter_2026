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
    private String passwordHash;

    @ManyToMany
    @JoinTable(name = "user_watched_movies",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "movie_id"))
    private Set<Movie> watchedMovies = new HashSet<>();

    // Break relationship with the movie before deleting the movie
    public void removeMovieFromUser(Movie movie) {
        this.getWatchedMovies().remove(movie);
    }

    @ManyToMany
    @JoinTable(name = "user_watched_dramas",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "drama_id"))
    private Set<Drama> watchedDramas = new HashSet<>();

    // Break relationship with the drama before deleting the drama
    public void removeDramaFromUser(Drama drama) {
        this.getWatchedDramas().remove(drama);
    }
}
