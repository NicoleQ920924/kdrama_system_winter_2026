package com.kdrama.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import com.kdrama.backend.model.User;
import com.kdrama.backend.enums.Role;
import com.kdrama.backend.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        try {
            String adminUsername = "admin";
            if (userRepository.findByUsername(adminUsername).isEmpty()) {
                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setDisplayName("Administrator");
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);
                System.out.println("Seeded admin user: admin");
            }
        } catch (Exception e) {
            System.err.println("Failed to seed admin user: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
