package com.example.bff.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${payment.service.url}")
    private String paymentServiceUrl;
    
    /**
     * Add a new card for a user
     */
    public Map<String, Object> addCard(Map<String, Object> cardRequest) {
        logger.info("Adding card for user: {}", cardRequest.get("userId"));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(cardRequest, headers);
        
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            paymentServiceUrl + "/api/payments/methods/card",
            HttpMethod.POST,
            entity,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        
        return response.getBody();
    }
    
    /**
     * Enable COD for a user
     */
    public Map<String, Object> enableCOD(Long userId, String phoneNumber) {
        logger.info("Enabling COD for user: {}", userId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> request = Map.of("userId", userId, "phoneNumber", phoneNumber);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            paymentServiceUrl + "/api/payments/methods/cod",
            HttpMethod.POST,
            entity,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        
        return response.getBody();
    }
    
    /**
     * Get all payment methods for a user
     */
    public List<Map<String, Object>> getUserPaymentMethods(Long userId) {
        logger.debug("Fetching payment methods for user: {}", userId);
        
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
            paymentServiceUrl + "/api/payments/methods/user/" + userId,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );
        
        return response.getBody();
    }
    
    /**
     * Delete a payment method
     */
    public void deletePaymentMethod(Long methodId, Long userId) {
        logger.info("Deleting payment method: {} for user: {}", methodId, userId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        restTemplate.exchange(
            paymentServiceUrl + "/api/payments/methods/" + methodId,
            HttpMethod.DELETE,
            entity,
            Void.class
        );
    }
    
    /**
     * Set default payment method
     */
    public Map<String, Object> setDefaultPaymentMethod(Long methodId, Long userId) {
        logger.info("Setting default payment method: {} for user: {}", methodId, userId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            paymentServiceUrl + "/api/payments/methods/" + methodId + "/default",
            HttpMethod.PUT,
            entity,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        
        return response.getBody();
    }
    
    /**
     * Process a payment
     */
    public Map<String, Object> processPayment(Map<String, Object> paymentRequest) {
        logger.info("Processing payment for order: {}", paymentRequest.get("orderId"));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(paymentRequest, headers);
        
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            paymentServiceUrl + "/api/payments/process",
            HttpMethod.POST,
            entity,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        
        return response.getBody();
    }
    
    /**
     * Get transaction by order ID
     */
    public Map<String, Object> getTransactionByOrderId(Long orderId) {
        logger.debug("Fetching transaction for order: {}", orderId);
        
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                paymentServiceUrl + "/api/payments/transactions/order/" + orderId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return response.getBody();
        } catch (Exception e) {
            logger.debug("No transaction found for order: {}", orderId);
            return null;
        }
    }
    
    /**
     * Get user's transaction history
     */
    public List<Map<String, Object>> getUserTransactions(Long userId) {
        logger.debug("Fetching transactions for user: {}", userId);
        
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
            paymentServiceUrl + "/api/payments/transactions/user/" + userId,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );
        
        return response.getBody();
    }
}
