package com.kdrama.backend.model;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.kdrama.backend.enums.*;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data // Automatic generation of getXXX(), setXXX(), etc.
@Entity
@Table(name = "drama",
    uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tmdb_id", "season_number"})
})
public class Drama {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer dramaId; // Primary Key

    // Basically most of them can be fetched via TMDB API

    @Column(name = "tmdb_id", nullable = false)
    private Integer tmdbId;

    @Column(name = "season_number", nullable = false)
    private Integer seasonNumber; // 1 by default
    
    private String chineseName; // e.g. 善意的競爭

    private String englishName; // e.g. Friendly Rivalry

    private String koreanName; // e.g. 선의의 경쟁

    private Integer totalNumOfEps; // MyDramaList's design, still more common than total running time

    private Integer currentEpNo; // Equal to totalNumOfEps if the drama is completed

    private String estRuntimePerEp; // Just an estimate, e.g. 60-70 mins

    private Integer krAgeRestriction;

    private String releaseYear; // e.g. 2025 or 2024-2025

    @Enumerated(EnumType.STRING)
    private DramaStatus status; // Ongoing or Completed, refer to DramaStatus.java

    @Enumerated(EnumType.STRING)
    private KRReleaseSchedule krReleaseSchedule; // Refer to KRReleaseSchedule.java
    
    @ElementCollection
    private List<String> genres; // Romantic Comedy, Thriller, etc.

    @ElementCollection
    private List<String> networks; // Original channel or platform for Korean users
    
    @ElementCollection
    @MapKeyColumn(name = "platform_name")
    @Column(name = "url")
    @CollectionTable(name = "drama_tw_platform_map", joinColumns = @JoinColumn(name = "drama_id"))
    private Map<String, String> dramaTwPlatformMap; // <platform_name, url>, platforms include Netflix, friDay Video, etc., handled by TWOTTPlatformScraper.java

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
