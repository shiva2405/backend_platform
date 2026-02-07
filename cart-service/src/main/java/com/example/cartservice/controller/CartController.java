package com.example.cartservice.controller;

import com.example.cartservice.dto.CartItemDTO;
import com.example.cartservice.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Cart", description = "Cart Management APIs")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    @Operation(summary = "Add item to cart")
    public ResponseEntity<CartItemDTO> addItem(@Valid @RequestBody CartItemDTO dto) {
        CartItemDTO saved = cartService.addItem(dto);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<CartItemDTO> updateItem(@PathVariable Long id, @Valid @RequestBody CartItemDTO dto) {
        Optional<CartItemDTO> updated = cartService.updateItem(id, dto);
        return updated.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all cart items")
    public ResponseEntity<List<CartItemDTO>> getCart() {
        List<CartItemDTO> cart = cartService.getCart();
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get cart items for a specific user")
    public ResponseEntity<List<CartItemDTO>> getCartByUserId(@PathVariable Long userId) {
        List<CartItemDTO> cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<Void> removeItem(@PathVariable Long id) {
        cartService.removeItem(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear all carts")
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{userId}")
    @Operation(summary = "Clear cart for a specific user")
    public ResponseEntity<Void> clearCartByUserId(@PathVariable Long userId) {
        cartService.clearCartByUserId(userId);
        return ResponseEntity.noContent().build();
    }
}
