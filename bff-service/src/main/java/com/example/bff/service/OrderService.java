package com.example.bff.service;

import com.example.bff.dto.*;
import com.example.bff.model.Order;
import com.example.bff.model.OrderItem;
import com.example.bff.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private ProductService productService;
    
    @Transactional
    public OrderDTO checkout(Long userId, CheckoutRequest request) {
        logger.info("Processing checkout for user: {}", userId);
        
        List<CartItemDTO> cartItems = cartService.getCartItems(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }
        
        // Calculate total
        double total = cartItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        
        // Create order
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalAmount(total);
        order.setShippingAddress(request.getShippingAddress());
        
        // Add order items
        for (CartItemDTO cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setProductName(cartItem.getProductName());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPrice());
            order.addItem(orderItem);
        }
        
        Order saved = orderRepository.save(order);
        
        // Clear cart after successful order
        cartService.clearCart(userId);
        
        logger.info("Order {} created successfully for user {}", saved.getId(), userId);
        return mapToOrderDTO(saved);
    }
    
    public List<OrderDTO> getUserOrders(Long userId) {
        logger.info("Fetching orders for user: {}", userId);
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId)
                .stream()
                .map(this::mapToOrderDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<OrderDTO> getOrderById(Long orderId) {
        return orderRepository.findById(orderId).map(this::mapToOrderDTO);
    }
    
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc()
                .stream()
                .map(this::mapToOrderDTO)
                .collect(Collectors.toList());
    }
    
    public OrderDTO updateOrderStatus(Long orderId, String status) {
        logger.info("Updating order {} status to {}", orderId, status);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setStatus(Order.OrderStatus.valueOf(status));
        Order saved = orderRepository.save(order);
        return mapToOrderDTO(saved);
    }
    
    private OrderDTO mapToOrderDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus().name());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setItems(order.getItems().stream().map(this::mapToOrderItemDTO).collect(Collectors.toList()));
        return dto;
    }
    
    private OrderItemDTO mapToOrderItemDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        return dto;
    }
}
