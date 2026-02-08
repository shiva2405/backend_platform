package com.example.identity.config;

import com.example.identity.model.User;
import com.example.identity.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            logger.info("Initializing demo users...");
            
            // Create admin user
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@shopease.com");
            admin.setFullName("Admin User");
            admin.setRole(User.Role.ADMIN);
            admin.setAddress("123 Admin Street");
            admin.setPhone("555-0100");
            userRepository.save(admin);
            
            // Create regular user
            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setEmail("user@example.com");
            user.setFullName("John Doe");
            user.setRole(User.Role.USER);
            user.setAddress("456 User Avenue");
            user.setPhone("555-0101");
            userRepository.save(user);
            
            // Create another user
            User jane = new User();
            jane.setUsername("jane");
            jane.setPassword(passwordEncoder.encode("jane123"));
            jane.setEmail("jane@example.com");
            jane.setFullName("Jane Smith");
            jane.setRole(User.Role.USER);
            jane.setAddress("789 Oak Lane");
            jane.setPhone("555-0102");
            userRepository.save(jane);
            
            logger.info("Demo users initialized successfully!");
        }
    }
}
