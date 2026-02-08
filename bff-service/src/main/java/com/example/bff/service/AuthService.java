package com.example.bff.service;

import com.example.bff.dto.LoginRequest;
import com.example.bff.dto.LoginResponse;
import com.example.bff.dto.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${identity.service.url}")
    private String identityServiceUrl;
    
    public LoginResponse login(LoginRequest request) {
        logger.info("Forwarding login request to identity service for user: {}", request.getUsername());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<LoginResponse> response = restTemplate.exchange(
            identityServiceUrl + "/api/auth/login",
            HttpMethod.POST,
            entity,
            LoginResponse.class
        );
        
        return response.getBody();
    }
    
    public Map<String, Object> register(RegisterRequest request) {
        logger.info("Forwarding registration request to identity service for user: {}", request.getUsername());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<Map> response = restTemplate.exchange(
            identityServiceUrl + "/api/auth/register",
            HttpMethod.POST,
            entity,
            Map.class
        );
        
        return response.getBody();
    }
}
