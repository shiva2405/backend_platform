package com.example.bff.controller;

import com.example.bff.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * Add a new card
     */
    @PostMapping("/methods/card")
    public ResponseEntity<Map<String, Object>> addCard(
            @RequestBody Map<String, Object> cardRequest,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        logger.info("BFF: Add card for user: {}", userId);
        
        // Ensure userId in request matches authenticated user
        cardRequest.put("userId", userId);
        
        Map<String, Object> result = paymentService.addCard(cardRequest);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Enable COD
     */
    @PostMapping("/methods/cod")
    public ResponseEntity<Map<String, Object>> enableCOD(
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String phoneNumber = (String) request.get("phoneNumber");
        logger.info("BFF: Enable COD for user: {}", userId);
        
        Map<String, Object> result = paymentService.enableCOD(userId, phoneNumber);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Get user's payment methods
     */
    @GetMapping("/methods")
    public ResponseEntity<List<Map<String, Object>>> getPaymentMethods(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        logger.debug("BFF: Get payment methods for user: {}", userId);
        
        List<Map<String, Object>> methods = paymentService.getUserPaymentMethods(userId);
        return ResponseEntity.ok(methods);
    }
    
    /**
     * Delete a payment method
     */
    @DeleteMapping("/methods/{methodId}")
    public ResponseEntity<Map<String, String>> deletePaymentMethod(
            @PathVariable Long methodId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        logger.info("BFF: Delete payment method: {} for user: {}", methodId, userId);
        
        paymentService.deletePaymentMethod(methodId, userId);
        return ResponseEntity.ok(Map.of("message", "Payment method deleted successfully"));
    }
    
    /**
     * Set default payment method
     */
    @PutMapping("/methods/{methodId}/default")
    public ResponseEntity<Map<String, Object>> setDefaultPaymentMethod(
            @PathVariable Long methodId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        logger.info("BFF: Set default payment method: {} for user: {}", methodId, userId);
        
        Map<String, Object> result = paymentService.setDefaultPaymentMethod(methodId, userId);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Get user's transaction history
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<Map<String, Object>>> getTransactions(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        logger.debug("BFF: Get transactions for user: {}", userId);
        
        List<Map<String, Object>> transactions = paymentService.getUserTransactions(userId);
        return ResponseEntity.ok(transactions);
    }
}
