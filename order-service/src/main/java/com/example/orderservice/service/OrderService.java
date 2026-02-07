package com.example.orderservice.service;

import com.example.orderservice.dto.ItemDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final RestTemplate restTemplate;

    @Value("${inventory.service.url:http://localhost:8081}")
    private String inventoryUrl;

    @Value("${cart.service.url:http://localhost:8082}")
    private String cartUrl;

    @Autowired
    public OrderService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<ItemDTO> listItems() {
        logger.info("Listing items");
        // Get cart (global)
        ResponseEntity<List> cartResp = restTemplate.getForEntity(cartUrl + "/api/cart", List.class);
        List<Map> cartItems = cartResp.getBody() != null ? cartResp.getBody() : new ArrayList<>();
        // Get inventory products
        ResponseEntity<List> invResp = restTemplate.getForEntity(inventoryUrl + "/api/products", List.class);
        List<Map> products = invResp.getBody() != null ? invResp.getBody() : new ArrayList<>();

        List<ItemDTO> items = new ArrayList<>();
        for (Map prod : products) {
            ItemDTO item = new ItemDTO();
            item.setProductId(((Number) prod.get("id")).longValue());
            item.setName((String) prod.get("name"));
            item.setPrice((Double) prod.get("price"));
            item.setStockQuantity((Integer) prod.get("stockQuantity"));
            // Find in cart
            for (Map cartItem : cartItems) {
                if (item.getProductId().equals(((Number) cartItem.get("productId")).longValue())) {
                    item.setCartQuantity((Integer) cartItem.get("quantity"));
                    break;
                }
            }
            items.add(item);
        }
        return items;
    }

    // CRUD cart proxies (minimal)
    public void addToCart(String userId, Long productId, Integer qty) {
        // Call cart add
        Map<String, Object> req = Map.of("userId", userId, "productId", productId, "quantity", qty);
        restTemplate.postForEntity(cartUrl + "/api/cart", req, Void.class);
    }

    // Similar for update/remove/clear...
    public void updateCartItem(Long itemId, Integer qty) {
        Map<String, Object> req = Map.of("quantity", qty);  // assume
        restTemplate.put(cartUrl + "/api/cart/" + itemId, req);
    }

    public void removeFromCart(Long itemId) {
        restTemplate.delete(cartUrl + "/api/cart/" + itemId);
    }
}
