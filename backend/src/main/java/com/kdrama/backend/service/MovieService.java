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

    private final ObjectMapper objectMapper;

    public MovieService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // Refer to MovieRepository.java for the CRUD repository methods

    // C1-1: Call TMDB API to fetch movie information by chineseName
    public Movie getMovieFromTmdbByChineseName(String title) {
        try {
            Integer tmdbId = tmdbMovieClient.getMovieTmdbIdByChineseName(title);
            Movie fetchedMovie = getMovieFromTmdbByTmdbId(tmdbId);
            return fetchedMovie;
		} catch (Exception e) {
			System.err.println("Exception Occurred!" + e.getStackTrace());
            return null;
		}
    }

    // C1-2: Call TMDB API to fetch movie information by tmdbId
    public Movie getMovieFromTmdbByTmdbId(Integer tmdbId) {
        try {
            Movie fetchedMovie = tmdbMovieClient.fillMovieInfoByTmdbId(tmdbId);
            if (fetchedMovie != null) {
                tmdbMovieClient.fillEngName(fetchedMovie);
                tmdbMovieClient.fillKrAgeRestriction(fetchedMovie);
                tmdbMovieClient.fillMovieStaff(fetchedMovie);
                fillTWPlatformInformation(fetchedMovie);
            }
            return fetchedMovie;
		} catch (Exception e) {
			System.err.println("Exception Occurred!" + e.getStackTrace());
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
    public Movie updateMovie(@PathVariable Integer id, @RequestBody Movie updatedMovie, boolean apiMode) {
        return movieRepository.findById(id)
                .map(movie -> {
                    movie.setTmdbId(updatedMovie.getTmdbId());
                    if ((movie.getChineseName().isEmpty() && apiMode) || !apiMode) {
                        movie.setChineseName(updatedMovie.getChineseName());
                    }
                    if ((movie.getEnglishName().isEmpty() && apiMode) || !apiMode) {
                        movie.setEnglishName(updatedMovie.getEnglishName());
                    }
                    if ((movie.getKoreanName().isEmpty() && apiMode) || !apiMode) {
                        movie.setKoreanName(updatedMovie.getKoreanName());
                    }
                    movie.setTotalRuntime(updatedMovie.getTotalRuntime());
                    movie.setKrAgeRestriction(updatedMovie.getKrAgeRestriction());
                    movie.setReleaseDate(updatedMovie.getReleaseDate());
                    movie.setGenres(updatedMovie.getGenres());
                    movie.setMovieTwPlatformMap(updatedMovie.getMovieTwPlatformMap());
                    movie.setLeadActors(updatedMovie.getLeadActors());
                    movie.setDirectorNames(updatedMovie.getDirectorNames());
                    movie.setScriptwriterNames(updatedMovie.getScriptwriterNames());
                    movie.setMainPosterUrl(updatedMovie.getMainPosterUrl());
                    if (!apiMode) {
                        movie.setTrailerUrl(updatedMovie.getTrailerUrl());
                    }
                    movie.setIntroPageUrl(updatedMovie.getIntroPageUrl());
                    if (!apiMode) {
                        movie.setNamuWikiPageUrl(updatedMovie.getNamuWikiPageUrl());
                    }
                    if (movie.isManuallyEdited() == false) {
                        movie.setManuallyEdited(updatedMovie.isManuallyEdited());
                    }
                    movie.setLastUpdatedByApi(updatedMovie.getLastUpdatedByApi());
                    return movieRepository.save(movie);
                })
                .orElseGet(() -> {
                    updatedMovie.setMovieId(id);
                    return movieRepository.save(updatedMovie);
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
