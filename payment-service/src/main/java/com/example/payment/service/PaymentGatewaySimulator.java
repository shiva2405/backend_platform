package com.example.payment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Simulates an external payment gateway (like Stripe, PayPal, Razorpay)
 * In production, this would be replaced with actual payment gateway SDK integration
 */
@Component
public class PaymentGatewaySimulator {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentGatewaySimulator.class);
    
    @Value("${payment.gateway.simulate-delay:500}")
    private int simulateDelay;
    
    @Value("${payment.gateway.failure-rate:0.05}")
    private double failureRate;
    
    private final Random random = new Random();
    
    /**
     * Simulates card payment authorization
     * In production: This would call actual payment gateway API
     */
    public GatewayResponse authorizeCardPayment(String cardNumber, String cvv, 
            String expiryMonth, String expiryYear, Double amount) {
        
        logger.info("GATEWAY: Authorizing card payment for amount: ${}", amount);
        
        // Simulate network delay
        simulateNetworkDelay();
        
        // Validate card (basic Luhn check simulation)
        if (!isValidCardNumber(cardNumber)) {
            logger.warn("GATEWAY: Invalid card number format");
            return new GatewayResponse(false, "Invalid card number", null);
        }
        
        // Check expiry
        if (!isValidExpiry(expiryMonth, expiryYear)) {
            logger.warn("GATEWAY: Card expired");
            return new GatewayResponse(false, "Card has expired", null);
        }
        
        // Simulate random failure (5% by default)
        if (random.nextDouble() < failureRate) {
            logger.warn("GATEWAY: Payment declined by issuer");
            return new GatewayResponse(false, "Payment declined by issuing bank", null);
        }
        
        // Special test card numbers for specific scenarios
        if (cardNumber.endsWith("0001")) {
            logger.warn("GATEWAY: Insufficient funds");
            return new GatewayResponse(false, "Insufficient funds", null);
        }
        if (cardNumber.endsWith("0002")) {
            logger.warn("GATEWAY: Card blocked");
            return new GatewayResponse(false, "Card blocked by issuer", null);
        }
        
        // Success!
        String authCode = "AUTH-" + System.currentTimeMillis() + "-" + random.nextInt(10000);
        logger.info("GATEWAY: Payment authorized. Auth code: {}", authCode);
        
        return new GatewayResponse(true, "Payment authorized successfully", authCode);
    }
    
    /**
     * Simulates COD order confirmation
     */
    public GatewayResponse confirmCODOrder(Long orderId, String phoneNumber) {
        logger.info("GATEWAY: Confirming COD order: {} for phone: {}", orderId, phoneNumber);
        
        simulateNetworkDelay();
        
        String confirmationCode = "COD-" + System.currentTimeMillis();
        logger.info("GATEWAY: COD order confirmed. Code: {}", confirmationCode);
        
        return new GatewayResponse(true, "COD order confirmed", confirmationCode);
    }
    
    /**
     * Detects card brand from card number
     */
    public String detectCardBrand(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) return "UNKNOWN";
        
        String prefix = cardNumber.substring(0, 4);
        
        if (cardNumber.startsWith("4")) return "VISA";
        if (cardNumber.matches("^5[1-5].*")) return "MASTERCARD";
        if (cardNumber.matches("^3[47].*")) return "AMEX";
        if (cardNumber.matches("^6(?:011|5).*")) return "DISCOVER";
        if (cardNumber.matches("^35(?:2[89]|[3-8]).*")) return "JCB";
        if (cardNumber.matches("^3(?:0[0-5]|[68]).*")) return "DINERS";
        
        return "UNKNOWN";
    }
    
    /**
     * Masks card number for storage
     */
    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        String last4 = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + last4;
    }
    
    private void simulateNetworkDelay() {
        try {
            Thread.sleep(simulateDelay + random.nextInt(200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    private boolean isValidCardNumber(String cardNumber) {
        // Basic validation - in production use Luhn algorithm
        return cardNumber != null && cardNumber.matches("\\d{13,19}");
    }
    
    private boolean isValidExpiry(String month, String year) {
        try {
            int m = Integer.parseInt(month);
            int y = Integer.parseInt(year);
            if (y < 100) y += 2000; // Handle 2-digit year
            
            // Simple check - in production validate against current date
            return m >= 1 && m <= 12 && y >= 2024;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Gateway response wrapper
     */
    public static class GatewayResponse {
        private final boolean success;
        private final String message;
        private final String referenceCode;
        
        public GatewayResponse(boolean success, String message, String referenceCode) {
            this.success = success;
            this.message = message;
            this.referenceCode = referenceCode;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getReferenceCode() { return referenceCode; }
    }
}
