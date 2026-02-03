package com.kdrama.backend.controller;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.kdrama.backend.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    /**
     * Generate a general response using Gemini
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateResponse(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        if (prompt == null || prompt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Prompt is required"));
        }

        String response = aiService.generateResponse(prompt);
        return ResponseEntity.ok(Map.of("response", response));
    }

    /**
     * Search for actors based on user criteria
     * Returns top 3 results with summaries and notable works
     */
    @PostMapping("/actor/search")
    public ResponseEntity<?> searchActors(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        if (prompt == null || prompt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Prompt is required"));
        }

        ArrayNode results = aiService.aiSearchActorsByPrompt(prompt);
        return ResponseEntity.ok(Map.of("results", results));
    }

    /**
     * Search for dramas based on user criteria
     * Returns top 3 results with titles and summaries
     */
    @PostMapping("/drama/search")
    public ResponseEntity<?> searchDramas(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        if (prompt == null || prompt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Prompt is required"));
        }

        ArrayNode results = aiService.aiSearchDramasByPrompt(prompt);
        return ResponseEntity.ok(Map.of("results", results));
    }

    @PostMapping("/movie/search")
    public ResponseEntity<?> searchMovies(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        if (prompt == null || prompt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Prompt is required"));
        }

        ArrayNode results = aiService.aiSearchMoviesByPrompt(prompt);
        return ResponseEntity.ok(Map.of("results", results));
    }
}
