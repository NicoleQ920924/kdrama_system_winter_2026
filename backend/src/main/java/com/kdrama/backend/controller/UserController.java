package com.kdrama.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kdrama.backend.model.User;
import com.kdrama.backend.service.UserService;
import com.kdrama.backend.repository.UserRepository;
import com.kdrama.backend.enums.Role;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<User> createUser(@RequestParam String username,
                                           @RequestParam(required = false) String displayName,
                                           @RequestParam(required = false, defaultValue = "USER") Role role) {
        User u = new User();
        u.setUsername(username);
        u.setDisplayName(displayName == null ? username : displayName);
        u.setRole(role);
        User saved = userService.saveUser(u);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Integer id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> listUsers() {
        List<User> all = userRepository.findAll();
        if (all.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(all);
    }
}
