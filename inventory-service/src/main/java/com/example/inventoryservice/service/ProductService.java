package com.example.inventoryservice.service;

import com.example.inventoryservice.dto.ProductDTO;
import com.example.inventoryservice.model.Product;
import com.example.inventoryservice.repository.ProductRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final Counter productAddCounter;
    private final Counter productUpdateCounter;

    @Autowired
    public ProductService(ProductRepository productRepository, MeterRegistry meterRegistry) {
        this.productRepository = productRepository;
        this.productAddCounter = Counter.builder("inventory.products.added")
                .description("Number of products added")
                .register(meterRegistry);
        this.productUpdateCounter = Counter.builder("inventory.products.updated")
                .description("Number of products updated")
                .register(meterRegistry);
    }

    public ProductDTO addProduct(ProductDTO productDTO) {
        logger.info("Adding new product: {}", productDTO.getName());
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setStockQuantity(productDTO.getStockQuantity());
        product.setCategory(productDTO.getCategory() != null ? productDTO.getCategory() : "General");
        product.setImageUrl(productDTO.getImageUrl());
        product.setRating(productDTO.getRating() != null ? productDTO.getRating() : 4.0);
        product.setReviewCount(productDTO.getReviewCount() != null ? productDTO.getReviewCount() : 0);
        Product saved = productRepository.save(product);
        productAddCounter.increment();
        logger.debug("Product added with ID: {}", saved.getId());
        return mapToDTO(saved);
    }

    public Optional<ProductDTO> updateProduct(Long id, ProductDTO productDTO) {
        logger.info("Updating product ID: {}", id);
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setName(productDTO.getName());
            product.setDescription(productDTO.getDescription());
            product.setPrice(productDTO.getPrice());
            if (productDTO.getStockQuantity() != null) {
                product.setStockQuantity(productDTO.getStockQuantity());
            }
            if (productDTO.getCategory() != null) {
                product.setCategory(productDTO.getCategory());
            }
            if (productDTO.getImageUrl() != null) {
                product.setImageUrl(productDTO.getImageUrl());
            }
            if (productDTO.getRating() != null) {
                product.setRating(productDTO.getRating());
            }
            if (productDTO.getReviewCount() != null) {
                product.setReviewCount(productDTO.getReviewCount());
            }
            Product updated = productRepository.save(product);
            productUpdateCounter.increment();
            logger.debug("Product updated: {}", updated.getId());
            return Optional.of(mapToDTO(updated));
        }
        logger.warn("Product not found for update: {}", id);
        return Optional.empty();
    }

    public List<ProductDTO> getAllProducts() {
        logger.debug("Fetching all products");
        return productRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    public Optional<ProductDTO> getProductById(Long id) {
        logger.debug("Fetching product by ID: {}", id);
        return productRepository.findById(id).map(this::mapToDTO);
    }

    public void deleteProduct(Long id) {
        logger.info("Deleting product ID: {}", id);
        productRepository.deleteById(id);
    }

    private ProductDTO mapToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setCategory(product.getCategory());
        dto.setImageUrl(product.getImageUrl());
        dto.setRating(product.getRating());
        dto.setReviewCount(product.getReviewCount());
        return dto;
    }
}
