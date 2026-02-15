package com.example.payment.dto;

import java.time.LocalDateTime;

public class PaymentResponse {
    private String transactionId;
    private Long orderId;
    private Double amount;
    private String currency;
    private String paymentType;
    private String maskedCardNumber;
    private String cardBrand;
    private String status;
    private String statusMessage;
    private String gatewayReference;
    private LocalDateTime processedAt;
    
    // Static factory methods
    public static PaymentResponse success(String transactionId, Long orderId, Double amount, 
            String paymentType, String maskedCardNumber, String cardBrand) {
        PaymentResponse response = new PaymentResponse();
        response.setTransactionId(transactionId);
        response.setOrderId(orderId);
        response.setAmount(amount);
        response.setPaymentType(paymentType);
        response.setMaskedCardNumber(maskedCardNumber);
        response.setCardBrand(cardBrand);
        response.setStatus("SUCCESS");
        response.setStatusMessage("Payment processed successfully");
        response.setGatewayReference("GW-" + System.currentTimeMillis());
        response.setProcessedAt(LocalDateTime.now());
        return response;
    }
    
    public static PaymentResponse codSuccess(String transactionId, Long orderId, Double amount) {
        PaymentResponse response = new PaymentResponse();
        response.setTransactionId(transactionId);
        response.setOrderId(orderId);
        response.setAmount(amount);
        response.setPaymentType("COD");
        response.setStatus("SUCCESS");
        response.setStatusMessage("Cash on Delivery order confirmed. Pay at delivery.");
        response.setGatewayReference("COD-" + System.currentTimeMillis());
        response.setProcessedAt(LocalDateTime.now());
        return response;
    }
    
    public static PaymentResponse failed(String transactionId, Long orderId, String message) {
        PaymentResponse response = new PaymentResponse();
        response.setTransactionId(transactionId);
        response.setOrderId(orderId);
        response.setStatus("FAILED");
        response.setStatusMessage(message);
        response.setProcessedAt(LocalDateTime.now());
        return response;
    }
    
    // Getters and Setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }
    
    public String getMaskedCardNumber() { return maskedCardNumber; }
    public void setMaskedCardNumber(String maskedCardNumber) { this.maskedCardNumber = maskedCardNumber; }
    
    public String getCardBrand() { return cardBrand; }
    public void setCardBrand(String cardBrand) { this.cardBrand = cardBrand; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }
    
    public String getGatewayReference() { return gatewayReference; }
    public void setGatewayReference(String gatewayReference) { this.gatewayReference = gatewayReference; }
    
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}
