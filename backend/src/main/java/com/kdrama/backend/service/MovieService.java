package com.kdrama.backend.service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdrama.backend.model.Actor;
import com.kdrama.backend.model.Movie;
import com.kdrama.backend.repository.ActorRepository;
import com.kdrama.backend.repository.MovieRepository;

@Service
public class MovieService {
    @Autowired
    private TmdbMovieClient tmdbMovieClient;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ActorRepository actorRepository;

    @Autowired
    private TmdbPlatformClient tmdbPlatformClient;

    @Autowired
    private TWOTTPlatformScraper platformScraper;

    private final AiService aiService;

    private final ObjectMapper objectMapper;

    public MovieService(ObjectMapper objectMapper, AiService aiService) {
        this.objectMapper = objectMapper;
        this.aiService = aiService;
    }

    // Refer to MovieRepository.java for the CRUD repository methods

    // C1-1: Fetch movie information (TMDB ID)
    // Return back to MovieController to check if movie exists in database
    public Movie fillMovieBasicInfo(String chineseName) {
        try {
            Movie movie = new Movie();
            movie.setChineseName(chineseName);
            Integer fetchedTmdbId = tmdbMovieClient.getMovieTmdbIdByMovieName(movie.getChineseName());
            if (fetchedTmdbId == null) {
                fetchedTmdbId = aiService.aiGetMovieTmdbId(movie.getChineseName());
            }
            movie.setTmdbId(fetchedTmdbId);
            return movie;     
		} catch (Exception e) {
			System.err.println("Exception Occurred!" + e.getMessage());
            e.printStackTrace();
            return null;
		}
    }

    // C1-2: Call TMDB API to fetch movie information
    public Movie fillMovieMoreInfo(Movie movie) {
        try {
            movie = tmdbMovieClient.fillMovieOtherInfo(movie);
            movie = tmdbMovieClient.fillKrAgeRestriction(movie);
            movie = tmdbMovieClient.fillMovieStaff(movie);
            return movie;     
		} catch (Exception e) {
			System.err.println("Exception Occurred!" + e.getMessage());
            e.printStackTrace();
            return null;
		}
    }

    // C1-3: Fill TW OTT Platform Information
    public Movie fillTWPlatformInformation(Movie fetchedMovie) {
        try {
            if (fetchedMovie == null) {
                System.err.println("fetchedMovie is null or empty, so platform information cannot be filled!");
                return fetchedMovie;
            }

            try {
                Map<String, String> platformInfoMap = platformScraper.getWorkTWOTTPlatformInfo(fetchedMovie.getChineseName(), "movie");
                fetchedMovie.setMovieTwPlatformMap(platformInfoMap);
                platformInfoMap.putAll(tmdbPlatformClient.getIntlPlatformInfoByWorkTmdbId(fetchedMovie.getTmdbId(), null, "movie"));
                System.out.println(fetchedMovie.getChineseName() + ": " + platformInfoMap.keySet());
                fetchedMovie.setLastUpdatedByApi(LocalDateTime.now());
            } catch (Exception innerE) {
                System.err.println("Failed to save platform information of " + fetchedMovie.getChineseName() + " due to" + innerE.getMessage());
                innerE.printStackTrace();
            }

            String backupFilePath = "backup/movie_backup.json";
            File backupFile = new File(backupFilePath);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(backupFile, fetchedMovie);
            System.out.println("Successfully save file: " + backupFile);

            return fetchedMovie;

        } catch (Exception e) {
            System.err.println("Exception Occurred!" + e.getStackTrace());
            e.printStackTrace();
            return null;
        }
    }
   
    // C2: Save a movie
    public Movie saveMovie(@RequestBody Movie movie) {
        return movieRepository.save(movie);
    }

    // R1: Get information of all the movies
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    // R2: Get information of a movie identified by id
    public Optional<Movie> getMovieById(@PathVariable Integer id) {
        return movieRepository.findById(id);
    }

    // R3: Get information of a movie identified by tmdbId
    public Optional<Movie> getMovieByTmdbId(@PathVariable Integer id) {
        return movieRepository.findByTmdbId(id);
    }

    // R4: Get information of a movie identified by Chinese title
    public Optional<Movie> getMovieByChineseName(@PathVariable String chineseName) {
        return movieRepository.findByChineseName(chineseName);
    }

    // U: Update a movie
    public Movie updateMovie(@PathVariable Integer id, @RequestBody Movie movieToUpdate, boolean apiMode) {
        return movieRepository.findById(id)
                .map(movie -> {
                    movie.setTmdbId(movieToUpdate.getTmdbId());
                    if ((movie.getChineseName().isEmpty() && apiMode) || !apiMode) {
                        movie.setChineseName(movieToUpdate.getChineseName());
                    }
                    if ((movie.getEnglishName().isEmpty() && apiMode) || !apiMode) {
                        movie.setEnglishName(movieToUpdate.getEnglishName());
                    }
                    if ((movie.getKoreanName().isEmpty() && apiMode) || !apiMode) {
                        movie.setKoreanName(movieToUpdate.getKoreanName());
                    }
                    movie.setTotalRuntime(movieToUpdate.getTotalRuntime());
                    movie.setKrAgeRestriction(movieToUpdate.getKrAgeRestriction());
                    movie.setReleaseDate(movieToUpdate.getReleaseDate());
                    movie.setGenres(movieToUpdate.getGenres());
                    movie.setMovieTwPlatformMap(movieToUpdate.getMovieTwPlatformMap());
                    movie.setLeadActors(movieToUpdate.getLeadActors());
                    movie.setDirectorNames(movieToUpdate.getDirectorNames());
                    movie.setScriptwriterNames(movieToUpdate.getScriptwriterNames());
                    movie.setMainPosterUrl(movieToUpdate.getMainPosterUrl());
                    if (!apiMode) {
                        movie.setTrailerUrl(movieToUpdate.getTrailerUrl());
                    }
                    movie.setIntroPageUrl(movieToUpdate.getIntroPageUrl());
                    if (!apiMode) {
                        movie.setChineseWikipediaPageUrl(movieToUpdate.getChineseWikipediaPageUrl());
                    }
                    if (!apiMode) {
                        movie.setNamuWikiPageUrl(movieToUpdate.getNamuWikiPageUrl());
                    }
                    if (movie.isAiOrManuallyEdited() == false) {
                        movie.setAiOrManuallyEdited(movieToUpdate.isAiOrManuallyEdited());
                    }
                    movie.setLastUpdatedByApi(movieToUpdate.getLastUpdatedByApi());
                    return movieRepository.save(movie);
                })
                .orElseGet(() -> {
                    movieToUpdate.setMovieId(id);
                    return movieRepository.save(movieToUpdate);
                });
    }

    // D: Delete a movie
    public void deleteMovie(@PathVariable Integer id) {
        // First, remove relationships with existing actors who act in the movie to delete
        Movie movieToDelete = getMovieById(id).get();
        List<Actor> actorsInMovie = actorRepository.findByMoviesContaining(movieToDelete);

        for (Actor actor : actorsInMovie) {
            actor.removeMovieFromActor(movieToDelete);
        }

        // Then, safely delete the movie
        movieRepository.deleteById(id);
    }

    // D-Bonus: Check if a movie exists in database before deletion
    public boolean movieExists(@PathVariable Integer id) {
        return movieRepository.existsById(id);
    }
}
