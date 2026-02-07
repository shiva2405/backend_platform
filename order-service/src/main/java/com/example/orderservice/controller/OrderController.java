package com.example.orderservice.controller;

import com.example.orderservice.dto.ItemDTO;
import com.example.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order & Cart Integration APIs")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/items/{userId}")
    @Operation(summary = "List items with details from inventory and cart")
    public ResponseEntity<List<ItemDTO>> listItems(@PathVariable String userId) {
        List<ItemDTO> items = orderService.listItems(userId);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/cart/add")
    @Operation(summary = "Add to cart via cart service")
    public ResponseEntity<Void> addToCart(@RequestParam String userId, @RequestParam Long productId, @RequestParam Integer qty) {
        orderService.addToCart(userId, productId, qty);
        return ResponseEntity.ok().build();
    }

    // Similar endpoints for update/remove
    @PutMapping("/cart/{itemId}")
    @Operation(summary = "Update cart item")
    public ResponseEntity<Void> updateCart(@PathVariable Long itemId, @RequestParam Integer qty) {
        orderService.updateCartItem(itemId, qty);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cart/{itemId}")
    @Operation(summary = "Remove from cart")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long itemId) {
        orderService.removeFromCart(itemId);
        return ResponseEntity.ok().build();
    }
}
