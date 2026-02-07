package com.example.inventoryservice.controller;

import com.example.inventoryservice.dto.OrderRequest;
import com.example.inventoryservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order Processing APIs")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/process")
    @Operation(summary = "Process order and update inventory")
    public ResponseEntity<String> processOrder(@Valid @RequestBody OrderRequest orderRequest) {
        String result = orderService.processOrder(orderRequest);
        if (result.startsWith("Insufficient") || result.startsWith("Product not found") || result.contains("failed")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
