package com.kdrama.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kdrama.backend.service.UserService;
import com.kdrama.backend.model.User;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    @Autowired
    private UserService userService;

    // Only general users may operate watchlist
    @com.kdrama.backend.security.RequireRole({com.kdrama.backend.enums.Role.USER})
    @PostMapping("/movie/add")
    public ResponseEntity<?> addMovie(@RequestParam Integer userId, @RequestParam Integer movieId) {
        User u = userService.addMovieToWatchlist(userId, movieId);
        if (u == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(u);
    }

    @com.kdrama.backend.security.RequireRole({com.kdrama.backend.enums.Role.USER})
    @PostMapping("/movie/remove")
    public ResponseEntity<?> removeMovie(@RequestParam Integer userId, @RequestParam Integer movieId) {
        User u = userService.removeMovieFromWatchlist(userId, movieId);
        if (u == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(u);
    }

    @com.kdrama.backend.security.RequireRole({com.kdrama.backend.enums.Role.USER})
    @PostMapping("/drama/add")
    public ResponseEntity<?> addDrama(@RequestParam Integer userId, @RequestParam Integer dramaId) {
        User u = userService.addDramaToWatchlist(userId, dramaId);
        if (u == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(u);
    }

    @com.kdrama.backend.security.RequireRole({com.kdrama.backend.enums.Role.USER})
    @PostMapping("/drama/remove")
    public ResponseEntity<?> removeDrama(@RequestParam Integer userId, @RequestParam Integer dramaId) {
        User u = userService.removeDramaFromWatchlist(userId, dramaId);
        if (u == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(u);
    }
}
