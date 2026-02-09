package com.kdrama.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kdrama.backend.model.User;
import com.kdrama.backend.model.Movie;
import com.kdrama.backend.model.Drama;
import com.kdrama.backend.repository.UserRepository;
import com.kdrama.backend.util.PasswordHashingUtil;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.kdrama.backend.repository.MovieRepository movieRepository;

    @Autowired
    private com.kdrama.backend.repository.DramaRepository dramaRepository;

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User createUser(String username, String displayName, String password, String role) {
        User u = new User();
        u.setUsername(username);
        u.setDisplayName(displayName == null ? username : displayName);
        u.setRole(com.kdrama.backend.enums.Role.valueOf(role));
        
        // Hash password if provided
        if (password != null && !password.trim().isEmpty()) {
            u.setPasswordHash(PasswordHashingUtil.hashPassword(password));
        } else {
            // If no password provided, generate a temporary hash (in production, this should be handled differently)
            u.setPasswordHash(PasswordHashingUtil.hashPassword(username + "default"));
        }
        
        return userRepository.save(u);
    }

    public boolean authenticateUser(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        if (!user.isPresent()) {
            return false;
        }
        
        String storedHash = user.get().getPasswordHash();
        return PasswordHashingUtil.verifyPassword(password, storedHash);
    }

    public User saveUser(User u) {
        return userRepository.save(u);
    }

    public User addMovieToWatchlist(Integer userId, Integer movieId) {
        User user = userRepository.findById(userId).orElse(null);
        Movie movie = movieRepository.findById(movieId).orElse(null);
        if (user == null || movie == null) return null;
        user.getWatchedMovies().add(movie);
        return userRepository.save(user);
    }

    public User removeMovieFromWatchlist(Integer userId, Integer movieId) {
        User user = userRepository.findById(userId).orElse(null);
        Movie movie = movieRepository.findById(movieId).orElse(null);
        if (user == null || movie == null) return null;
        user.getWatchedMovies().remove(movie);
        return userRepository.save(user);
    }

    public User addDramaToWatchlist(Integer userId, Integer dramaId) {
        User user = userRepository.findById(userId).orElse(null);
        Drama drama = dramaRepository.findById(dramaId).orElse(null);
        if (user == null || drama == null) return null;
        user.getWatchedDramas().add(drama);
        return userRepository.save(user);
    }

    public User removeDramaFromWatchlist(Integer userId, Integer dramaId) {
        User user = userRepository.findById(userId).orElse(null);
        Drama drama = dramaRepository.findById(dramaId).orElse(null);
        if (user == null || drama == null) return null;
        user.getWatchedDramas().remove(drama);
        return userRepository.save(user);
    }
}
