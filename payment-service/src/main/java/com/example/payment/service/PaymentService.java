package com.example.payment.service;

import com.example.payment.dto.*;
import com.example.payment.model.PaymentMethod;
import com.example.payment.model.PaymentTransaction;
import com.example.payment.repository.PaymentMethodRepository;
import com.example.payment.repository.PaymentTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    
    @Autowired
    private PaymentTransactionRepository transactionRepository;
    
    @Autowired
    private PaymentGatewaySimulator gatewaySimulator;
    
    /**
     * Add a new card for a user
     */
    @Transactional
    public PaymentMethodDTO addCard(AddCardRequest request) {
        logger.info("Adding new card for user: {}", request.getUserId());
        
        // Validate card with gateway
        String cardBrand = gatewaySimulator.detectCardBrand(request.getCardNumber());
        String maskedNumber = gatewaySimulator.maskCardNumber(request.getCardNumber());
        
        // Clear existing default if this is being set as default
        if (request.isSetAsDefault()) {
            paymentMethodRepository.clearDefaultForUser(request.getUserId());
        }
        
        PaymentMethod method = new PaymentMethod();
        method.setUserId(request.getUserId());
        method.setType(PaymentMethod.PaymentType.valueOf(request.getCardType()));
        method.setCardHolderName(request.getCardHolderName());
        method.setCardNumber(maskedNumber); // Store only masked
        method.setCardBrand(cardBrand);
        method.setExpiryMonth(request.getExpiryMonth());
        method.setExpiryYear(request.getExpiryYear());
        method.setDefault(request.isSetAsDefault());
        
        PaymentMethod saved = paymentMethodRepository.save(method);
        logger.info("Card added successfully: {} ending in {}", cardBrand, maskedNumber.substring(maskedNumber.length() - 4));
        
        return mapToDTO(saved);
    }
    
    /**
     * Enable COD for a user
     */
    @Transactional
    public PaymentMethodDTO enableCOD(Long userId, String phoneNumber) {
        logger.info("Enabling COD for user: {}", userId);
        
        // Check if COD already exists
        if (paymentMethodRepository.existsByUserIdAndType(userId, PaymentMethod.PaymentType.COD)) {
            throw new RuntimeException("COD already enabled for this user");
        }
        
        PaymentMethod method = new PaymentMethod();
        method.setUserId(userId);
        method.setType(PaymentMethod.PaymentType.COD);
        method.setPhoneNumber(phoneNumber);
        method.setDefault(false);
        
        PaymentMethod saved = paymentMethodRepository.save(method);
        logger.info("COD enabled for user: {}", userId);
        
        return mapToDTO(saved);
    }
    
    /**
     * Get all payment methods for a user
     */
    public List<PaymentMethodDTO> getUserPaymentMethods(Long userId) {
        logger.debug("Fetching payment methods for user: {}", userId);
        return paymentMethodRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Delete a payment method
     */
    @Transactional
    public void deletePaymentMethod(Long methodId, Long userId) {
        logger.info("Deleting payment method: {} for user: {}", methodId, userId);
        PaymentMethod method = paymentMethodRepository.findById(methodId)
                .orElseThrow(() -> new RuntimeException("Payment method not found"));
        
        if (!method.getUserId().equals(userId)) {
            throw new RuntimeException("Payment method does not belong to user");
        }
        
        paymentMethodRepository.delete(method);
        logger.info("Payment method deleted: {}", methodId);
    }
    
    /**
     * Set a payment method as default
     */
    @Transactional
    public PaymentMethodDTO setDefaultPaymentMethod(Long methodId, Long userId) {
        logger.info("Setting payment method {} as default for user: {}", methodId, userId);
        
        PaymentMethod method = paymentMethodRepository.findById(methodId)
                .orElseThrow(() -> new RuntimeException("Payment method not found"));
        
        if (!method.getUserId().equals(userId)) {
            throw new RuntimeException("Payment method does not belong to user");
        }
        
        paymentMethodRepository.clearDefaultForUser(userId);
        method.setDefault(true);
        PaymentMethod saved = paymentMethodRepository.save(method);
        
        return mapToDTO(saved);
    }
    
    /**
     * Process a payment
     */
    @Transactional
    public PaymentResponse processPayment(ProcessPaymentRequest request) {
        logger.info("Processing payment for order: {} amount: ${}", request.getOrderId(), request.getAmount());
        
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Create transaction record
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setTransactionId(transactionId);
        transaction.setOrderId(request.getOrderId());
        transaction.setUserId(request.getUserId());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setPaymentType(PaymentMethod.PaymentType.valueOf(request.getPaymentType()));
        transaction.setStatus(PaymentTransaction.TransactionStatus.PROCESSING);
        
        transactionRepository.save(transaction);
        
        try {
            PaymentResponse response;
            
            if ("COD".equals(request.getPaymentType())) {
                response = processCODPayment(transaction, request);
            } else {
                response = processCardPayment(transaction, request);
            }
            
            return response;
            
        } catch (Exception e) {
            logger.error("Payment processing failed: {}", e.getMessage());
            transaction.setStatus(PaymentTransaction.TransactionStatus.FAILED);
            transaction.setStatusMessage(e.getMessage());
            transaction.setProcessedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            
            return PaymentResponse.failed(transactionId, request.getOrderId(), e.getMessage());
        }
    }
    
    private PaymentResponse processCardPayment(PaymentTransaction transaction, ProcessPaymentRequest request) {
        logger.info("Processing card payment for transaction: {}", transaction.getTransactionId());
        
        String cardNumber;
        String cvv;
        String expiryMonth;
        String expiryYear;
        
        // Use saved payment method or provided card details
        if (request.getPaymentMethodId() != null) {
            PaymentMethod method = paymentMethodRepository.findById(request.getPaymentMethodId())
                    .orElseThrow(() -> new RuntimeException("Payment method not found"));
            
            // In production, you'd have tokenized card data
            // For simulation, we'll use the masked card (this is NOT how real systems work)
            cardNumber = "4111111111111111"; // Simulated valid card
            cvv = "123";
            expiryMonth = method.getExpiryMonth();
            expiryYear = method.getExpiryYear();
            
            transaction.setPaymentMethodId(method.getId());
            transaction.setMaskedCardNumber(method.getCardNumber());
            transaction.setCardBrand(method.getCardBrand());
        } else {
            cardNumber = request.getCardNumber();
            cvv = request.getCvv();
            expiryMonth = request.getExpiryMonth();
            expiryYear = request.getExpiryYear();
            
            transaction.setMaskedCardNumber(gatewaySimulator.maskCardNumber(cardNumber));
            transaction.setCardBrand(gatewaySimulator.detectCardBrand(cardNumber));
        }
        
        // Call gateway
        PaymentGatewaySimulator.GatewayResponse gatewayResponse = 
                gatewaySimulator.authorizeCardPayment(cardNumber, cvv, expiryMonth, expiryYear, request.getAmount());
        
        if (gatewayResponse.isSuccess()) {
            transaction.setStatus(PaymentTransaction.TransactionStatus.SUCCESS);
            transaction.setStatusMessage("Payment successful");
            transaction.setGatewayReference(gatewayResponse.getReferenceCode());
            transaction.setProcessedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            
            logger.info("Card payment successful: {} gateway ref: {}", 
                    transaction.getTransactionId(), gatewayResponse.getReferenceCode());
            
            return PaymentResponse.success(
                    transaction.getTransactionId(),
                    transaction.getOrderId(),
                    transaction.getAmount(),
                    transaction.getPaymentType().name(),
                    transaction.getMaskedCardNumber(),
                    transaction.getCardBrand()
            );
        } else {
            transaction.setStatus(PaymentTransaction.TransactionStatus.FAILED);
            transaction.setStatusMessage(gatewayResponse.getMessage());
            transaction.setProcessedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            
            logger.warn("Card payment failed: {} reason: {}", 
                    transaction.getTransactionId(), gatewayResponse.getMessage());
            
            return PaymentResponse.failed(
                    transaction.getTransactionId(),
                    transaction.getOrderId(),
                    gatewayResponse.getMessage()
            );
        }
    }
    
    private PaymentResponse processCODPayment(PaymentTransaction transaction, ProcessPaymentRequest request) {
        logger.info("Processing COD payment for transaction: {}", transaction.getTransactionId());
        
        PaymentGatewaySimulator.GatewayResponse gatewayResponse = 
                gatewaySimulator.confirmCODOrder(request.getOrderId(), request.getDeliveryPhone());
        
        transaction.setStatus(PaymentTransaction.TransactionStatus.SUCCESS);
        transaction.setStatusMessage("COD order confirmed");
        transaction.setGatewayReference(gatewayResponse.getReferenceCode());
        transaction.setProcessedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
        
        logger.info("COD payment confirmed: {}", transaction.getTransactionId());
        
        return PaymentResponse.codSuccess(
                transaction.getTransactionId(),
                transaction.getOrderId(),
                transaction.getAmount()
        );
    }
    
    /**
     * Get transaction by ID
     */
    public PaymentTransaction getTransaction(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }
    
    /**
     * Get transaction by order ID
     */
    public PaymentTransaction getTransactionByOrderId(Long orderId) {
        return transactionRepository.findByOrderId(orderId)
                .orElse(null);
    }
    
    /**
     * Get user's transaction history
     */
    public List<PaymentTransaction> getUserTransactions(Long userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    private PaymentMethodDTO mapToDTO(PaymentMethod method) {
        PaymentMethodDTO dto = new PaymentMethodDTO();
        dto.setId(method.getId());
        dto.setUserId(method.getUserId());
        dto.setType(method.getType().name());
        dto.setCardHolderName(method.getCardHolderName());
        dto.setMaskedCardNumber(method.getCardNumber());
        dto.setCardBrand(method.getCardBrand());
        dto.setExpiryMonth(method.getExpiryMonth());
        dto.setExpiryYear(method.getExpiryYear());
        dto.setPhoneNumber(method.getPhoneNumber());
        dto.setDefault(method.isDefault());
        dto.setCreatedAt(method.getCreatedAt());
        return dto;
    }
}
