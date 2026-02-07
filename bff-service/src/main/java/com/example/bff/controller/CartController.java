package com.example.bff.controller;

import com.example.bff.config.JwtUtil;
import com.example.bff.dto.CartItemDTO;
import com.example.bff.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "Shopping cart APIs")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @GetMapping
    @Operation(summary = "Get cart items")
    public ResponseEntity<List<CartItemDTO>> getCartItems(@RequestHeader("Authorization") String token) {
        Long userId = extractUserId(token);
        return ResponseEntity.ok(cartService.getCartItems(userId));
    }
    
    @PostMapping
    @Operation(summary = "Add item to cart")
    public ResponseEntity<CartItemDTO> addToCart(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> request) {
        Long userId = extractUserId(token);
        Long productId = ((Number) request.get("productId")).longValue();
        Integer quantity = ((Number) request.get("quantity")).intValue();
        
        return ResponseEntity.ok(cartService.addToCart(userId, productId, quantity));
    }
    
    @PutMapping("/{itemId}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<CartItemDTO> updateCartItem(
            @PathVariable Long itemId,
            @RequestBody Map<String, Integer> request) {
        return ResponseEntity.ok(cartService.updateCartItem(itemId, request.get("quantity")));
    }
    
    @DeleteMapping("/{itemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long itemId) {
        cartService.removeFromCart(itemId);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping
    @Operation(summary = "Clear cart")
    public ResponseEntity<Void> clearCart(@RequestHeader("Authorization") String token) {
        Long userId = extractUserId(token);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/total")
    @Operation(summary = "Get cart total")
    public ResponseEntity<Map<String, Double>> getCartTotal(@RequestHeader("Authorization") String token) {
        Long userId = extractUserId(token);
        return ResponseEntity.ok(Map.of("total", cartService.getCartTotal(userId)));
    }
    
    private Long extractUserId(String token) {
        String jwt = token.replace("Bearer ", "");
        return jwtUtil.extractUserId(jwt);
    }
}
