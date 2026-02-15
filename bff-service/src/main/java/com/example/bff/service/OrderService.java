package com.example.bff.service;

import com.example.bff.dto.CartItemDTO;
import com.example.bff.dto.CheckoutRequest;
import com.example.bff.dto.OrderDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${order.service.url}")
    private String orderServiceUrl;
    
    @Value("${payment.service.url}")
    private String paymentServiceUrl;
    
    @Autowired
    private CartService cartService;
    
    public OrderDTO checkout(Long userId, CheckoutRequest request) {
        logger.info("Processing checkout for user: {}", userId);
        
        // Fetch cart items from cart service
        List<CartItemDTO> cartItems = cartService.getCartItems(userId);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        
        // Calculate total
        double total = cartItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        
        // Add cart items to the request
        request.setCartItems(cartItems);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Id", userId.toString());
        HttpEntity<CheckoutRequest> entity = new HttpEntity<>(request, headers);
        
        // Create order first
        ResponseEntity<OrderDTO> response = restTemplate.exchange(
            orderServiceUrl + "/api/orders/checkout",
            HttpMethod.POST,
            entity,
            OrderDTO.class
        );
        
        OrderDTO order = response.getBody();
        
        // Process payment if payment type is specified
        if (request.getPaymentType() != null && order != null) {
            logger.info("Processing payment for order: {} type: {}", order.getId(), request.getPaymentType());
            
            try {
                Map<String, Object> paymentResult = processPayment(userId, order.getId(), total, request);
                
                if (paymentResult != null) {
                    String paymentStatus = (String) paymentResult.get("status");
                    order.setPaymentStatus(paymentStatus);
                    order.setPaymentTransactionId((String) paymentResult.get("transactionId"));
                    
                    if ("FAILED".equals(paymentStatus)) {
                        // Update order status to PAYMENT_FAILED
                        updateOrderStatus(order.getId(), "PAYMENT_FAILED");
                        order.setStatus("PAYMENT_FAILED");
                        logger.warn("Payment failed for order: {}", order.getId());
                    } else {
                        logger.info("Payment successful for order: {} txn: {}", 
                                order.getId(), paymentResult.get("transactionId"));
                    }
                }
            } catch (Exception e) {
                logger.error("Payment processing error for order: {} - {}", order.getId(), e.getMessage());
                order.setPaymentStatus("ERROR");
            }
        }
        
        return order;
    }
    
    private Map<String, Object> processPayment(Long userId, Long orderId, Double amount, CheckoutRequest request) {
        logger.info("Calling payment service for order: {}", orderId);
        
        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("orderId", orderId);
        paymentRequest.put("userId", userId);
        paymentRequest.put("amount", amount);
        paymentRequest.put("paymentType", request.getPaymentType());
        
        if (request.getPaymentMethodId() != null) {
            paymentRequest.put("paymentMethodId", request.getPaymentMethodId());
        }
        if (request.getCardNumber() != null) {
            paymentRequest.put("cardNumber", request.getCardNumber());
            paymentRequest.put("cardHolderName", request.getCardHolderName());
            paymentRequest.put("expiryMonth", request.getExpiryMonth());
            paymentRequest.put("expiryYear", request.getExpiryYear());
            paymentRequest.put("cvv", request.getCvv());
        }
        if (request.getDeliveryPhone() != null) {
            paymentRequest.put("deliveryPhone", request.getDeliveryPhone());
        }
        
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
    
    public List<OrderDTO> getUserOrders(Long userId) {
        logger.info("Fetching orders from order service for user: {}", userId);
        
        ResponseEntity<List<OrderDTO>> response = restTemplate.exchange(
            orderServiceUrl + "/api/orders/user/" + userId,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<OrderDTO>>() {}
        );
        
        return response.getBody();
    }
    
    public OrderDTO getOrderById(Long orderId) {
        ResponseEntity<OrderDTO> response = restTemplate.exchange(
            orderServiceUrl + "/api/orders/" + orderId,
            HttpMethod.GET,
            null,
            OrderDTO.class
        );
        return response.getBody();
    }
    
    public List<OrderDTO> getAllOrders() {
        ResponseEntity<List<OrderDTO>> response = restTemplate.exchange(
            orderServiceUrl + "/api/orders",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<OrderDTO>>() {}
        );
        return response.getBody();
    }
    
    public OrderDTO updateOrderStatus(Long orderId, String status) {
        logger.info("Updating order {} status to {}", orderId, status);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(Map.of("status", status), headers);
        
        ResponseEntity<OrderDTO> response = restTemplate.exchange(
            orderServiceUrl + "/api/orders/" + orderId + "/status",
            HttpMethod.PUT,
            entity,
            OrderDTO.class
        );
        
        return response.getBody();
    }
}
