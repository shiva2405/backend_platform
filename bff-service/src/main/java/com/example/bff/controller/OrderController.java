package com.example.bff.controller;

import com.example.bff.config.JwtUtil;
import com.example.bff.dto.CheckoutRequest;
import com.example.bff.dto.OrderDTO;
import com.example.bff.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/checkout")
    @Operation(summary = "Checkout and create order")
    public ResponseEntity<OrderDTO> checkout(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CheckoutRequest request) {
        Long userId = extractUserId(token);
        return ResponseEntity.ok(orderService.checkout(userId, request));
    }
    
    @GetMapping
    @Operation(summary = "Get user's orders")
    public ResponseEntity<List<OrderDTO>> getUserOrders(@RequestHeader("Authorization") String token) {
        Long userId = extractUserId(token);
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }
    
    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    private Long extractUserId(String token) {
        String jwt = token.replace("Bearer ", "");
        return jwtUtil.extractUserId(jwt);
    }
}
