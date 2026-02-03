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
import com.kdrama.backend.service.AiService;
import com.kdrama.backend.service.MovieService;

@RestController
@RequestMapping("/api/movies")
public class MovieController {
    
    @Autowired
    private MovieService movieService;

    private final ObjectMapper objectMapper;

    private final AiService aiService;

    public MovieController(ObjectMapper objectMapper, AiService aiService) {
        this.objectMapper = objectMapper;
        this.aiService = aiService;
    }

    @PostMapping("/import")
    public ResponseEntity<?> importMovie(@RequestParam String name) {
        // Check if the movie already exists in database
        Optional<Movie> optionalExistingMovie = movieService.getMovieByChineseName(name);
        if (optionalExistingMovie.isPresent()) {
            // movie 已存在 → 回傳 409
            return ResponseEntity.status(HttpStatus.SC_CONFLICT)
                                .body("Movie already exists in database");
        }
        Movie movie = movieService.fillMovieBasicInfo(name);
        if (movie == null) {
            return ResponseEntity.notFound().build();
        }
        // Check if the movie already exists in database
        optionalExistingMovie = movieService.getMovieByTmdbId(movie.getTmdbId());
        if (optionalExistingMovie.isPresent()) {
            // movie 已存在 → 回傳 409
            return ResponseEntity.status(HttpStatus.SC_CONFLICT)
                                .body("Movie already exists in database");
        }
        
        movie = movieService.fillMovieMoreInfo(movie);

        if (movie == null) {
            return ResponseEntity.notFound().build();
        }
        else {
            movie = aiService.aiUpdateMovieInfo(movie);
            if (movie == null) {
                return ResponseEntity.notFound().build();
            }
            else {
                Movie savedMovie = movieService.saveMovie(movie);
                return ResponseEntity.ok(savedMovie);
            }    
        }
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
    public ResponseEntity<JsonNode> findSelectedMovieById(
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

    @GetMapping("/chineseName")
    public ResponseEntity<JsonNode> findSelectedMovieByChineseName(
        @RequestParam String chineseName) {
        
        Optional<Movie> optionalMovie = movieService.getMovieByChineseName(chineseName);
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
            Movie updatedMovie = movieService.fillMovieMoreInfo(movie);
            if (updatedMovie == null) {
                return ResponseEntity.notFound().build();
            }
            else {
                Movie savedMovie = movieService.updateMovie(id, updatedMovie, true);
                return ResponseEntity.ok(savedMovie);
            } 
        }
    }

    @PutMapping("/aiupdate/{id}")
    public ResponseEntity<?> updateSelectedMovieViaAiAndForm(@PathVariable Integer id, @RequestBody Movie movieToUpdate) {
       Movie aiUpdated = aiService.aiUpdateMovieInfo(movieToUpdate);
       Movie movie = movieService.updateMovie(id, aiUpdated, false);
       return ResponseEntity.ok(movie);
    }

    @PutMapping("/formupdate/{id}")
    public ResponseEntity<Movie> updateSelectedMovieViaForm(@PathVariable Integer id, @RequestBody Movie movieToUpdate) {
        Movie movie = movieService.updateMovie(id, movieToUpdate, false);
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
