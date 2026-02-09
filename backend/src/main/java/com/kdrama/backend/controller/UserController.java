package com.kdrama.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.kdrama.backend.model.User;
import com.kdrama.backend.service.UserService;
import com.kdrama.backend.util.DisplayNameEnumSerializer;
import com.kdrama.backend.repository.UserRepository;

import org.apache.hc.core5.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper;
    public UserController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestParam String username,
                                        @RequestParam(required = false) String displayName,
                                        @RequestParam(required = false) String password,
                                        @RequestParam(required = false, defaultValue = "USER") String role) {
        // Check if user already exists
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.status(409).body(createErrorResponse("Username already exists"));
        }

        try {
            User newUser = userService.createUser(username, displayName, password, role);
            return ResponseEntity.ok(newUser);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(createErrorResponse("Failed to create user: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        Optional<User> user = userService.getUserByUsername(username);
        
        if (!user.isPresent()) {
            return ResponseEntity.status(401).body(createErrorResponse("Invalid username or password"));
        }

        // Verify password
        if (!userService.authenticateUser(username, password)) {
            return ResponseEntity.status(401).body(createErrorResponse("Invalid username or password"));
        }

        return ResponseEntity.ok(user.get());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JsonNode> getUser(@PathVariable Integer id,
        @RequestParam(required = false, defaultValue = "true") boolean displayNameMode) {
        
        Optional<User> optionalUser = userService.getUserById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }
        
        User user = optionalUser.get();
        
        SimpleModule module = new SimpleModule();
        module.addSerializer(Enum.class, new DisplayNameEnumSerializer(displayNameMode));

        ObjectMapper mapper = objectMapper.copy();
        mapper.registerModule(module);

        try {
            // Return JsonNode to frontend
            JsonNode jsonNode = mapper.valueToTree(user);
            return ResponseEntity.ok(jsonNode);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userService.getUserByUsername(username);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> listUsers() {
        List<User> all = userRepository.findAll();
        if (all.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(all);
    }

    /**
     * Helper method to create consistent error response
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}

