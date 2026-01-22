package com.kdrama.backend.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kdrama.backend.enums.*;

@Data // Automatic generation of getXXX(), setXXX(), etc.
@Entity
public class Actor {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer actorId; // Primary Key

    // Basically most of them can be fetched via TMDB API
    // From Searching: Query = chineseName

    @Column(unique = true)
    private Integer tmdbId; // fetch "id"

    private String chineseName; // search query

    private String englishName; // switch language to "en" then fetch "name"

    private String koreanName; // fetch "original_name"

    private String profilePicUrl; // fetch "profile_path"

    @Enumerated(EnumType.STRING)
    private ActorGender actorGender; // fetch "gender", 1 = FEMALE, 2 = MALE

    // Fetchable via tmdbId

    private String birthday; // fetch "birthday"

    @Column(length = 600) // Around 200 Chinese Characters
    private String biography; // fetch "biography"

    // Set Relationships with Drama and Movie

    @ManyToMany
    @JoinTable(name = "actor_drama",
        joinColumns = @JoinColumn(name = "actor_id"),
        inverseJoinColumns = @JoinColumn(name = "drama_id"))
    private List<Drama> dramas = new ArrayList<>();;

    // Break relationship with the drama before deleting the drama
    public void removeDramaFromActor(Drama drama) {
        this.getDramas().remove(drama);
    }

    @ManyToMany
    @JoinTable(name = "actor_movie",
        joinColumns = @JoinColumn(name = "actor_id"),
        inverseJoinColumns = @JoinColumn(name = "movie_id"))
    private List<Movie> movies = new ArrayList<>();;

    // Break relationship with the drama before deleting the drama
    public void removeMovieFromActor(Movie movie) {
        this.getMovies().remove(movie);
    }

    private String namuWikiPageUrl; // Manually added

    private boolean manuallyEdited = false; // e.g. Manual Addition of Information
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdatedByApi; // Sync with TMDB API

}
