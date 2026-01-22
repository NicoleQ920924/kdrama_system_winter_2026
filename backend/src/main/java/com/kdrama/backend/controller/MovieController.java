package com.kdrama.backend.controller;

import java.util.List;
import java.util.Optional;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdrama.backend.model.Movie;
import com.kdrama.backend.service.MovieService;

@RestController
@RequestMapping("/api/movies")
public class MovieController {
    
    @Autowired
    private MovieService movieService;

    private final ObjectMapper objectMapper;

    public MovieController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostMapping("/import")
    public ResponseEntity<?> importMovie(@RequestParam String name) {
        // Check if the movie (with one or more seasons) already exists in database
        Optional<Movie> existingMovie = movieService.getMovieByChineseName(name);
        if (existingMovie.isPresent()) {
            // movie 已存在 → 回傳 409
            return ResponseEntity.status(HttpStatus.SC_CONFLICT)
                                .body("Movie already exists in database");
        }
        Movie movie = movieService.getMovieFromTmdbByChineseName(name);
        if (movie == null) {
            return ResponseEntity.notFound().build();
        }
        Movie savedMovie = movieService.saveMovie(movie);
        return ResponseEntity.ok(savedMovie);
    }

    @GetMapping("/findAll")
    public ResponseEntity<JsonNode> findMovies() {
        
        List<Movie> movies = movieService.getAllMovies();
        if (movies.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }

        ObjectMapper mapper = objectMapper.copy();

        try {
            JsonNode jsonNode = mapper.valueToTree(movies);
            return ResponseEntity.ok(jsonNode);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<JsonNode> findSelectedMovie(
        @PathVariable Integer id) {
        
        Optional<Movie> optionalMovie = movieService.getMovieById(id);
        if (optionalMovie.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }
        
        Movie movie = optionalMovie.get();

        ObjectMapper mapper = objectMapper.copy();

        try {
            // Return JsonNode to frontend
            JsonNode jsonNode = mapper.valueToTree(movie);
            return ResponseEntity.ok(jsonNode);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/apiupdate/{id}")
    public ResponseEntity<?> updateSelectedMovieViaApi(@PathVariable Integer id) {
        
        Optional<Movie> optionalMovie = movieService.getMovieById(id);
        if (optionalMovie.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        else {
            Movie movie = optionalMovie.get();
            Movie updatedMovie = movieService.getMovieFromTmdbByTmdbId(movie.getTmdbId());
            if (updatedMovie == null) {
                return ResponseEntity.notFound().build();
            }
            else {
                Movie savedMovie = movieService.updateMovie(id, updatedMovie, true);
                return ResponseEntity.ok(savedMovie);
            }
        }
    }

    @PutMapping("/allupdate/{id}")
    public ResponseEntity<Movie> updateSelectedMovieAllInfo(@PathVariable Integer id, @RequestBody Movie updatedMovie) {
        Movie movie = movieService.updateMovie(id, updatedMovie, false);
        return ResponseEntity.ok(movie);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteSelectedMovie(@PathVariable Integer id) {     
        if (movieService.movieExists(id)) {
            movieService.deleteMovie(id);
            return ResponseEntity.ok("Movie deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.SC_NOT_FOUND).body("Movie not found");
        }
    }
}
