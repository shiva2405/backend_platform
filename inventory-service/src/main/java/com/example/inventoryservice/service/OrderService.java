package com.example.inventoryservice.service;

import com.example.inventoryservice.dto.OrderRequest;
import com.example.inventoryservice.model.Order;
import com.example.inventoryservice.model.Product;
import com.example.inventoryservice.repository.OrderRepository;
import com.example.inventoryservice.repository.ProductRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final Counter orderProcessedCounter;
    private final Counter stockUpdateCounter;

    @Autowired
    public OrderService(ProductRepository productRepository, OrderRepository orderRepository, MeterRegistry meterRegistry) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.orderProcessedCounter = Counter.builder("inventory.orders.processed")
                .description("Number of orders processed")
                .register(meterRegistry);
        this.stockUpdateCounter = Counter.builder("inventory.stock.updates")
                .description("Number of stock level updates")
                .register(meterRegistry);
    }

    @Transactional
    public String processOrder(OrderRequest orderRequest) {
        // Minimal validation in service layer
        if (orderRequest.getOrderId() == null || orderRequest.getOrderId().trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID is required for idempotency");
        }
        if (orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain items");
        }

        logger.info("Processing order with ID: {}", orderRequest.getOrderId());

        // Idempotency check
        Optional<Order> existing = orderRepository.findById(orderRequest.getOrderId());
        if (existing.isPresent()) {
            if ("PROCESSED".equals(existing.get().getStatus())) {
                logger.info("Idempotent order already processed: {}", orderRequest.getOrderId());
                return "Order already processed successfully.";
            }
            // Failed before, allow retry
        }

        int retries = 1;  // optimistic lock retry
        while (retries >= 0) {
            try {
                Set<Long> productIds = orderRequest.getItems().stream()
                        .map(OrderRequest.OrderItem::getProductId)
                        .collect(Collectors.toSet());
                List<Product> products = productRepository.findAllById(productIds);
                Map<Long, Product> productMap = products.stream()
                        .collect(Collectors.toMap(Product::getId, p -> p));
                for (OrderRequest.OrderItem item : orderRequest.getItems()) {
                    // Minimal item validation
                    if (item.getQuantity() == null || item.getQuantity() <= 0) {
                        throw new IllegalArgumentException("Invalid quantity for product: " + item.getProductId());
                    }
                    Product product = productMap.get(item.getProductId());
                    if (product != null) {
                        if (product.getStockQuantity() >= item.getQuantity()) {
                            int newStock = product.getStockQuantity() - item.getQuantity();
                            product.setStockQuantity(newStock);
                            stockUpdateCounter.increment();
                            logger.debug("Stock updated for product {}: new stock {}", item.getProductId(), newStock);
                        } else {
                            logger.warn("Insufficient stock for product ID: {}", item.getProductId());
                            saveOrderStatus(orderRequest.getOrderId(), "FAILED");
                            return "Insufficient stock for product ID: " + item.getProductId();
                        }
                    } else {
                        logger.warn("Product not found: {}", item.getProductId());
                        saveOrderStatus(orderRequest.getOrderId(), "FAILED");
                        return "Product not found: " + item.getProductId();
                    }
                }
                productRepository.saveAll(products);
                orderProcessedCounter.increment();
                saveOrderStatus(orderRequest.getOrderId(), "PROCESSED");
                logger.info("Order processed successfully");
                return "Order processed successfully. Inventory updated.";
            } catch (ObjectOptimisticLockingFailureException e) {
                logger.warn("Optimistic lock failure, retrying: {}", e.getMessage());
                retries--;
                if (retries < 0) {
                    saveOrderStatus(orderRequest.getOrderId(), "FAILED");
                    throw e;  // fail after retry
                }
            }
        }
        saveOrderStatus(orderRequest.getOrderId(), "FAILED");
        return "Order processing failed due to concurrency";
    }

    private void saveOrderStatus(String orderId, String status) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setStatus(status);
        orderRepository.save(order);
    }
}
