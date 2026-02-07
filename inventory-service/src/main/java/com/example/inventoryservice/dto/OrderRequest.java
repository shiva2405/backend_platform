package com.example.inventoryservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class OrderRequest {
    @NotBlank(message = "Order ID is required for idempotency")
    private String orderId;  // client-provided for idempotency
    @NotEmpty(message = "Order must contain items")
    private List<@Valid OrderItem> items;

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public static class OrderItem {
        @NotNull(message = "Product ID is required")
        private Long productId;
        @NotNull(message = "Quantity is required")
        private Integer quantity;

        // Getters and Setters
        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
