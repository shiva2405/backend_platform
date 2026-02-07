package com.example.bff.service;

import com.example.bff.dto.ProductDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${inventory.service.url}")
    private String inventoryUrl;
    
    public List<ProductDTO> getAllProducts() {
        logger.info("Fetching all products from inventory service");
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                inventoryUrl + "/api/products",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            
            List<Map<String, Object>> products = response.getBody();
            if (products == null) return new ArrayList<>();
            
            return products.stream().map(this::mapToProductDTO).toList();
        } catch (Exception e) {
            logger.error("Error fetching products: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public Optional<ProductDTO> getProductById(Long id) {
        logger.info("Fetching product by id: {}", id);
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                inventoryUrl + "/api/products/" + id,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            Map<String, Object> product = response.getBody();
            if (product == null) return Optional.empty();
            
            return Optional.of(mapToProductDTO(product));
        } catch (Exception e) {
            logger.error("Error fetching product {}: {}", id, e.getMessage());
            return Optional.empty();
        }
    }
    
    public List<ProductDTO> getProductsByCategory(String category) {
        return getAllProducts().stream()
                .filter(p -> category.equalsIgnoreCase(p.getCategory()))
                .toList();
    }
    
    public List<ProductDTO> searchProducts(String query) {
        return getAllProducts().stream()
                .filter(p -> p.getName().toLowerCase().contains(query.toLowerCase()) ||
                            (p.getDescription() != null && p.getDescription().toLowerCase().contains(query.toLowerCase())))
                .toList();
    }
    
    // Admin operations
    public ProductDTO addProduct(ProductDTO productDTO) {
        logger.info("Adding new product: {}", productDTO.getName());
        ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
            inventoryUrl + "/api/products",
            productDTO,
            (Class<Map<String, Object>>) (Class<?>) Map.class
        );
        return mapToProductDTO(response.getBody());
    }
    
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        logger.info("Updating product: {}", id);
        restTemplate.put(inventoryUrl + "/api/products/" + id, productDTO);
        return getProductById(id).orElse(productDTO);
    }
    
    public void deleteProduct(Long id) {
        logger.info("Deleting product: {}", id);
        restTemplate.delete(inventoryUrl + "/api/products/" + id);
    }
    
    private ProductDTO mapToProductDTO(Map<String, Object> map) {
        ProductDTO dto = new ProductDTO();
        dto.setId(((Number) map.get("id")).longValue());
        dto.setName((String) map.get("name"));
        dto.setDescription((String) map.get("description"));
        dto.setPrice(((Number) map.get("price")).doubleValue());
        dto.setStockQuantity(((Number) map.get("stockQuantity")).intValue());
        dto.setCategory((String) map.getOrDefault("category", "General"));
        dto.setImageUrl((String) map.getOrDefault("imageUrl", "https://via.placeholder.com/300x300?text=" + dto.getName()));
        dto.setRating(map.get("rating") != null ? ((Number) map.get("rating")).doubleValue() : 4.0);
        dto.setReviewCount(map.get("reviewCount") != null ? ((Number) map.get("reviewCount")).intValue() : 0);
        return dto;
    }
}
