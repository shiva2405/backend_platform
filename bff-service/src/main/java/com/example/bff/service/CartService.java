package com.example.bff.service;

import com.example.bff.dto.CartItemDTO;
import com.example.bff.dto.ProductDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CartService {
    
    private static final Logger logger = LoggerFactory.getLogger(CartService.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ProductService productService;
    
    @Value("${cart.service.url}")
    private String cartUrl;
    
    public List<CartItemDTO> getCartItems(Long userId) {
        logger.info("Fetching cart items for user: {}", userId);
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                cartUrl + "/api/cart/user/" + userId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            
            List<Map<String, Object>> items = response.getBody();
            if (items == null) return new ArrayList<>();
            
            return items.stream().map(item -> enrichCartItem(mapToCartItemDTO(item))).toList();
        } catch (Exception e) {
            logger.error("Error fetching cart: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public CartItemDTO addToCart(Long userId, Long productId, Integer quantity) {
        logger.info("Adding to cart - user: {}, product: {}, qty: {}", userId, productId, quantity);
        
        Map<String, Object> request = Map.of(
            "userId", userId,
            "productId", productId,
            "quantity", quantity
        );
        
        ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
            cartUrl + "/api/cart",
            request,
            (Class<Map<String, Object>>) (Class<?>) Map.class
        );
        
        return enrichCartItem(mapToCartItemDTO(response.getBody()));
    }
    
    public CartItemDTO updateCartItem(Long itemId, Integer quantity) {
        logger.info("Updating cart item: {} to qty: {}", itemId, quantity);
        
        Map<String, Object> request = Map.of("quantity", quantity);
        restTemplate.put(cartUrl + "/api/cart/" + itemId, request);
        
        // Return updated item
        CartItemDTO item = new CartItemDTO();
        item.setId(itemId);
        item.setQuantity(quantity);
        return item;
    }
    
    public void removeFromCart(Long itemId) {
        logger.info("Removing cart item: {}", itemId);
        restTemplate.delete(cartUrl + "/api/cart/" + itemId);
    }
    
    public void clearCart(Long userId) {
        logger.info("Clearing cart for user: {}", userId);
        restTemplate.delete(cartUrl + "/api/cart/user/" + userId);
    }
    
    public Double getCartTotal(Long userId) {
        List<CartItemDTO> items = getCartItems(userId);
        return items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
    
    private CartItemDTO mapToCartItemDTO(Map<String, Object> map) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(((Number) map.get("id")).longValue());
        dto.setUserId(((Number) map.get("userId")).longValue());
        dto.setProductId(((Number) map.get("productId")).longValue());
        dto.setQuantity(((Number) map.get("quantity")).intValue());
        return dto;
    }
    
    private CartItemDTO enrichCartItem(CartItemDTO item) {
        Optional<ProductDTO> product = productService.getProductById(item.getProductId());
        if (product.isPresent()) {
            ProductDTO p = product.get();
            item.setProductName(p.getName());
            item.setPrice(p.getPrice());
            item.setImageUrl(p.getImageUrl());
            item.setStockQuantity(p.getStockQuantity());
        }
        return item;
    }
}
