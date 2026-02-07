package com.example.bff.service;

import com.example.bff.config.JwtUtil;
import com.example.bff.dto.LoginRequest;
import com.example.bff.dto.LoginResponse;
import com.example.bff.dto.RegisterRequest;
import com.example.bff.model.User;
import com.example.bff.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    public LoginResponse login(LoginRequest request) {
        logger.info("Login attempt for user: {}", request.getUsername());
        
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getId());
        
        logger.info("User {} logged in successfully", request.getUsername());
        return new LoginResponse(token, user.getUsername(), user.getFullName(), user.getRole().name(), user.getId());
    }
    
    public User register(RegisterRequest request) {
        logger.info("Registration attempt for user: {}", request.getUsername());
        
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setAddress(request.getAddress());
        user.setPhone(request.getPhone());
        user.setRole(User.Role.USER);
        
        User saved = userRepository.save(user);
        logger.info("User {} registered successfully", request.getUsername());
        return saved;
    }
}
