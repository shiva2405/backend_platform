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

import java.util.List;
import java.util.Map;

@Service
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${order.service.url}")
    private String orderServiceUrl;
    
    @Autowired
    private CartService cartService;
    
    public OrderDTO checkout(Long userId, CheckoutRequest request) {
        logger.info("Processing checkout for user: {}", userId);
        
        // Fetch cart items from cart service
        List<CartItemDTO> cartItems = cartService.getCartItems(userId);
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        
        // Add cart items to the request
        request.setCartItems(cartItems);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Id", userId.toString());
        HttpEntity<CheckoutRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<OrderDTO> response = restTemplate.exchange(
            orderServiceUrl + "/api/orders/checkout",
            HttpMethod.POST,
            entity,
            OrderDTO.class
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
