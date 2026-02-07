package com.example.inventoryservice.config;

import com.example.inventoryservice.model.Product;
import com.example.inventoryservice.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    // Base URL for local images - works offline
    private static final String IMAGE_BASE = "/images/products/";

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            logger.info("Initializing sample products...");
            
            // Electronics
            addProduct("iPhone 15 Pro", "Latest Apple iPhone with A17 Pro chip, 48MP camera, titanium design", 
                999.99, 50, "Electronics", IMAGE_BASE + "iphone.svg", 4.8, 1250);
            
            addProduct("Samsung Galaxy S24 Ultra", "Premium Android smartphone with S Pen, 200MP camera", 
                1199.99, 35, "Electronics", IMAGE_BASE + "samsung.svg", 4.7, 890);
            
            addProduct("MacBook Pro 14\"", "Apple M3 Pro chip, 18GB RAM, 512GB SSD, Liquid Retina XDR display", 
                1999.99, 25, "Electronics", IMAGE_BASE + "macbook.svg", 4.9, 2100);
            
            addProduct("Sony WH-1000XM5", "Premium wireless noise-cancelling headphones", 
                349.99, 100, "Electronics", IMAGE_BASE + "headphones.svg", 4.6, 3200);
            
            addProduct("iPad Air", "10.9-inch Liquid Retina display, M1 chip, 256GB", 
                749.99, 60, "Electronics", IMAGE_BASE + "ipad.svg", 4.7, 1800);
            
            // Clothing
            addProduct("Nike Air Max 270", "Men's running shoes with Air Max cushioning", 
                159.99, 80, "Clothing", IMAGE_BASE + "nike-shoes.svg", 4.5, 2500);
            
            addProduct("Levi's 501 Original Jeans", "Classic straight fit jeans, 100% cotton denim", 
                79.99, 150, "Clothing", IMAGE_BASE + "jeans.svg", 4.4, 4200);
            
            addProduct("North Face Jacket", "Men's waterproof insulated winter jacket", 
                249.99, 45, "Clothing", IMAGE_BASE + "jacket.svg", 4.6, 890);
            
            // Home & Kitchen
            addProduct("Instant Pot Duo 7-in-1", "Electric pressure cooker, slow cooker, rice cooker", 
                89.99, 200, "Home & Kitchen", IMAGE_BASE + "instant-pot.svg", 4.8, 15000);
            
            addProduct("Dyson V15 Detect", "Cordless vacuum with laser dust detection", 
                749.99, 30, "Home & Kitchen", IMAGE_BASE + "dyson.svg", 4.7, 3400);
            
            addProduct("KitchenAid Stand Mixer", "Artisan series 5-quart tilt-head stand mixer", 
                449.99, 40, "Home & Kitchen", IMAGE_BASE + "kitchenaid.svg", 4.9, 8500);
            
            // Books
            addProduct("Atomic Habits", "By James Clear - Tiny changes, remarkable results", 
                16.99, 500, "Books", IMAGE_BASE + "book-atomic.svg", 4.8, 125000);
            
            addProduct("The Psychology of Money", "By Morgan Housel - Timeless lessons on wealth", 
                19.99, 300, "Books", IMAGE_BASE + "book-money.svg", 4.7, 45000);
            
            // Sports & Outdoors
            addProduct("Yeti Rambler 20oz", "Vacuum insulated tumbler with MagSlider lid", 
                35.00, 250, "Sports & Outdoors", IMAGE_BASE + "yeti.svg", 4.8, 9800);
            
            addProduct("Fitbit Charge 5", "Advanced fitness tracker with built-in GPS", 
                149.95, 75, "Sports & Outdoors", IMAGE_BASE + "fitbit.svg", 4.5, 6700);
            
            logger.info("Sample products initialized successfully!");
        }
    }

    private void addProduct(String name, String description, Double price, Integer stock, 
                           String category, String imageUrl, Double rating, Integer reviewCount) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stock);
        product.setCategory(category);
        product.setImageUrl(imageUrl);
        product.setRating(rating);
        product.setReviewCount(reviewCount);
        productRepository.save(product);
    }
}
