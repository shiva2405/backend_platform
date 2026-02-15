package com.example.payment.controller;

import com.example.payment.dto.*;
import com.example.payment.model.PaymentTransaction;
import com.example.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class PaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    @Autowired
    private PaymentService paymentService;
    
    // ==================== Payment Methods ====================
    
    /**
     * Add a new card for a user
     */
    @PostMapping("/methods/card")
    public ResponseEntity<PaymentMethodDTO> addCard(@Valid @RequestBody AddCardRequest request) {
        logger.info("REST: Add card for user: {}", request.getUserId());
        PaymentMethodDTO method = paymentService.addCard(request);
        return ResponseEntity.ok(method);
    }
    
    /**
     * Enable COD for a user
     */
    @PostMapping("/methods/cod")
    public ResponseEntity<PaymentMethodDTO> enableCOD(@RequestBody Map<String, Object> request) {
        Long userId = ((Number) request.get("userId")).longValue();
        String phoneNumber = (String) request.get("phoneNumber");
        logger.info("REST: Enable COD for user: {}", userId);
        PaymentMethodDTO method = paymentService.enableCOD(userId, phoneNumber);
        return ResponseEntity.ok(method);
    }
    
    /**
     * Get all payment methods for a user
     */
    @GetMapping("/methods/user/{userId}")
    public ResponseEntity<List<PaymentMethodDTO>> getUserPaymentMethods(@PathVariable Long userId) {
        logger.debug("REST: Get payment methods for user: {}", userId);
        List<PaymentMethodDTO> methods = paymentService.getUserPaymentMethods(userId);
        return ResponseEntity.ok(methods);
    }
    
    /**
     * Delete a payment method
     */
    @DeleteMapping("/methods/{methodId}")
    public ResponseEntity<Map<String, String>> deletePaymentMethod(
            @PathVariable Long methodId,
            @RequestHeader("X-User-Id") Long userId) {
        logger.info("REST: Delete payment method: {} for user: {}", methodId, userId);
        paymentService.deletePaymentMethod(methodId, userId);
        return ResponseEntity.ok(Map.of("message", "Payment method deleted successfully"));
    }
    
    /**
     * Set a payment method as default
     */
    @PutMapping("/methods/{methodId}/default")
    public ResponseEntity<PaymentMethodDTO> setDefaultPaymentMethod(
            @PathVariable Long methodId,
            @RequestHeader("X-User-Id") Long userId) {
        logger.info("REST: Set default payment method: {} for user: {}", methodId, userId);
        PaymentMethodDTO method = paymentService.setDefaultPaymentMethod(methodId, userId);
        return ResponseEntity.ok(method);
    }
    
    // ==================== Payment Processing ====================
    
    /**
     * Process a payment
     */
    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody ProcessPaymentRequest request) {
        logger.info("REST: Process payment for order: {} amount: ${}", request.getOrderId(), request.getAmount());
        PaymentResponse response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get transaction by ID
     */
    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<PaymentTransaction> getTransaction(@PathVariable String transactionId) {
        logger.debug("REST: Get transaction: {}", transactionId);
        PaymentTransaction transaction = paymentService.getTransaction(transactionId);
        return ResponseEntity.ok(transaction);
    }
    
    /**
     * Get transaction by order ID
     */
    @GetMapping("/transactions/order/{orderId}")
    public ResponseEntity<PaymentTransaction> getTransactionByOrderId(@PathVariable Long orderId) {
        logger.debug("REST: Get transaction for order: {}", orderId);
        PaymentTransaction transaction = paymentService.getTransactionByOrderId(orderId);
        if (transaction == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(transaction);
    }
    
    /**
     * Get user's transaction history
     */
    @GetMapping("/transactions/user/{userId}")
    public ResponseEntity<List<PaymentTransaction>> getUserTransactions(@PathVariable Long userId) {
        logger.debug("REST: Get transactions for user: {}", userId);
        List<PaymentTransaction> transactions = paymentService.getUserTransactions(userId);
        return ResponseEntity.ok(transactions);
    }
    
    // ==================== Health Check ====================
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "payment-service",
            "gateway", "SIMULATOR"
        ));
    }
}
