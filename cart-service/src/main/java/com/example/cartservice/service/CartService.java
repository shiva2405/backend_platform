package com.example.cartservice.service;

import com.example.cartservice.dto.CartItemDTO;
import com.example.cartservice.model.CartItem;
import com.example.cartservice.repository.CartItemRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);
    private final CartItemRepository cartItemRepository;
    private final Counter addCounter;
    private final Counter updateCounter;

    @Autowired
    public CartService(CartItemRepository cartItemRepository, MeterRegistry meterRegistry) {
        this.cartItemRepository = cartItemRepository;
        this.addCounter = Counter.builder("cart.items.added").register(meterRegistry);
        this.updateCounter = Counter.builder("cart.items.updated").register(meterRegistry);
    }

    public CartItemDTO addItem(CartItemDTO dto) {
        logger.info("Adding item to cart for user: {}, product: {}", dto.getUserId(), dto.getProductId());
        
        // Check if item already exists in cart
        Optional<CartItem> existing = cartItemRepository.findByUserIdAndProductId(dto.getUserId(), dto.getProductId());
        if (existing.isPresent()) {
            // Update quantity instead
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + dto.getQuantity());
            CartItem saved = cartItemRepository.save(item);
            updateCounter.increment();
            return mapToDTO(saved);
        }
        
        CartItem item = new CartItem();
        item.setUserId(dto.getUserId());
        item.setProductId(dto.getProductId());
        item.setQuantity(dto.getQuantity());
        CartItem saved = cartItemRepository.save(item);
        addCounter.increment();
        return mapToDTO(saved);
    }

    public Optional<CartItemDTO> updateItem(Long id, CartItemDTO dto) {
        logger.info("Updating cart item: {}", id);
        Optional<CartItem> optional = cartItemRepository.findById(id);
        if (optional.isPresent()) {
            CartItem item = optional.get();
            item.setQuantity(dto.getQuantity());
            CartItem updated = cartItemRepository.save(item);
            updateCounter.increment();
            return Optional.of(mapToDTO(updated));
        }
        return Optional.empty();
    }

    public List<CartItemDTO> getCart() {
        logger.debug("Getting all cart items");
        return cartItemRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<CartItemDTO> getCartByUserId(Long userId) {
        logger.debug("Getting cart for user: {}", userId);
        return cartItemRepository.findByUserId(userId).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public void removeItem(Long id) {
        logger.info("Removing cart item: {}", id);
        cartItemRepository.deleteById(id);
    }

    public void clearCart() {
        logger.info("Clearing all carts");
        cartItemRepository.deleteAll();
    }

    @Transactional
    public void clearCartByUserId(Long userId) {
        logger.info("Clearing cart for user: {}", userId);
        cartItemRepository.deleteByUserId(userId);
    }

    private CartItemDTO mapToDTO(CartItem item) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(item.getId());
        dto.setUserId(item.getUserId());
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());
        return dto;
    }
}
