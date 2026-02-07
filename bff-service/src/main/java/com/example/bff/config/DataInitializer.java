package com.example.bff.config;

import com.example.bff.model.User;
import com.example.bff.repository.UserRepository;
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
        // Create admin user
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@ecommerce.com");
            admin.setFullName("Admin User");
            admin.setRole(User.Role.ADMIN);
            admin.setAddress("123 Admin Street, Tech City");
            admin.setPhone("555-0100");
            userRepository.save(admin);
            logger.info("Created admin user: admin/admin123");
        }
        
        // Create demo user
        if (!userRepository.existsByUsername("user")) {
            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setEmail("user@ecommerce.com");
            user.setFullName("John Doe");
            user.setRole(User.Role.USER);
            user.setAddress("456 Main Street, Shopping Town");
            user.setPhone("555-0200");
            userRepository.save(user);
            logger.info("Created demo user: user/user123");
        }
        
        // Create another demo user
        if (!userRepository.existsByUsername("jane")) {
            User jane = new User();
            jane.setUsername("jane");
            jane.setPassword(passwordEncoder.encode("jane123"));
            jane.setEmail("jane@ecommerce.com");
            jane.setFullName("Jane Smith");
            jane.setRole(User.Role.USER);
            jane.setAddress("789 Oak Avenue, Retail City");
            jane.setPhone("555-0300");
            userRepository.save(jane);
            logger.info("Created demo user: jane/jane123");
        }
        
        logger.info("===========================================");
        logger.info("Demo Users Created:");
        logger.info("  Admin: admin / admin123");
        logger.info("  User:  user / user123");
        logger.info("  User:  jane / jane123");
        logger.info("===========================================");
    }
}
