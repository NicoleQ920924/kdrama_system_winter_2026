package com.kdrama.backend.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import lombok.Data;

@Data // Automatic generation of getXXX(), setXXX(), etc.
@Entity
public class Movie {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer movieId; // Primary Key

    // Basically most of them can be fetched via TMDB API

    @Column(unique = true)
    private Integer tmdbId;
    
    private String chineseName; // e.g. 變身機長

    private String englishName; // e.g. Pilot

    private String koreanName; // e.g. 파일럿

    private Integer totalRuntime; // in minutes

    private Integer krAgeRestriction;

    private String releaseDate;

    @ElementCollection
    private List<String> genres; // Romantic Comedy, Thriller, etc.
    
    @ElementCollection
    @MapKeyColumn(name = "platform_name")
    @Column(name = "url")
    @CollectionTable(name = "movie_tw_platform_map", joinColumns = @JoinColumn(name = "movie_id"))
    private Map<String, String> movieTwPlatformMap; // <platform_name, url>, platforms include Netflix, friDay Video, etc., handled by TWOTTPlatformScraper.java

    @ElementCollection
    private List<String> leadActors; // In Chinese

    @ElementCollection
    private List<String> directorNames; // In Chinese

    @ElementCollection
    private List<String> scriptwriterNames; // In Chinese

    private String mainPosterUrl; // Fetched via TMDB API

    private String trailerUrl; // YouTube video link (manually edited)

    private String introPageUrl; // The information page on TMDB

    private String namuWikiPageUrl; // Manually added

    private boolean manuallyEdited = false; // e.g. Manual Addition of Information
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastUpdatedByApi; // Sync with TMDB API
}
