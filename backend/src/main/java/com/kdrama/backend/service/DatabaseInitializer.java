package com.kdrama.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kdrama.backend.model.User;
import com.kdrama.backend.repository.UserRepository;
import com.kdrama.backend.enums.Role;
import com.kdrama.backend.util.PasswordHashingUtil;

@Component
public class DatabaseInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Value("${admin.default.password}")
    private String defaultAdminPassword;
    
    @Override
    public void run(String... args) throws Exception {
        initializeDefaultAdminUser();
    }
    
    private void initializeDefaultAdminUser() {
        try {
            // Check if admin user already exists
            if (userRepository.findByUsername("admin").isPresent()) {
                logger.info("Admin user already exists, skipping initialization");
                return;
            }
            
            // Create default admin user
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setDisplayName("Admin");
            adminUser.setRole(Role.ADMIN);
            adminUser.setPasswordHash(PasswordHashingUtil.hashPassword(defaultAdminPassword));
            
            userRepository.save(adminUser);
            logger.info("Default admin user created successfully");
            logger.info("Username: admin");
            logger.info("Password set from ADMIN_DEFAULT_PASSWORD environment variable");
            
        } catch (Exception e) {
            logger.error("Failed to initialize default admin user", e);
        }
    }
}
