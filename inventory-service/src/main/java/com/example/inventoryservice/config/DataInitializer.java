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

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            logger.info("Initializing sample products...");
            
            // Electronics
            addProduct("iPhone 15 Pro", "Latest Apple iPhone with A17 Pro chip, 48MP camera, titanium design", 
                999.99, 50, "Electronics", "https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=400", 4.8, 1250);
            
            addProduct("Samsung Galaxy S24 Ultra", "Premium Android smartphone with S Pen, 200MP camera", 
                1199.99, 35, "Electronics", "https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=400", 4.7, 890);
            
            addProduct("MacBook Pro 14\"", "Apple M3 Pro chip, 18GB RAM, 512GB SSD, Liquid Retina XDR display", 
                1999.99, 25, "Electronics", "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=400", 4.9, 2100);
            
            addProduct("Sony WH-1000XM5", "Premium wireless noise-cancelling headphones", 
                349.99, 100, "Electronics", "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400", 4.6, 3200);
            
            addProduct("iPad Air", "10.9-inch Liquid Retina display, M1 chip, 256GB", 
                749.99, 60, "Electronics", "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=400", 4.7, 1800);
            
            // Clothing
            addProduct("Nike Air Max 270", "Men's running shoes with Air Max cushioning", 
                159.99, 80, "Clothing", "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400", 4.5, 2500);
            
            addProduct("Levi's 501 Original Jeans", "Classic straight fit jeans, 100% cotton denim", 
                79.99, 150, "Clothing", "https://images.unsplash.com/photo-1542272604-787c3835535d?w=400", 4.4, 4200);
            
            addProduct("North Face Jacket", "Men's waterproof insulated winter jacket", 
                249.99, 45, "Clothing", "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400", 4.6, 890);
            
            // Home & Kitchen
            addProduct("Instant Pot Duo 7-in-1", "Electric pressure cooker, slow cooker, rice cooker", 
                89.99, 200, "Home & Kitchen", "https://images.unsplash.com/photo-1585515320310-259814833e62?w=400", 4.8, 15000);
            
            addProduct("Dyson V15 Detect", "Cordless vacuum with laser dust detection", 
                749.99, 30, "Home & Kitchen", "https://images.unsplash.com/photo-1558317374-067fb5f30001?w=400", 4.7, 3400);
            
            addProduct("KitchenAid Stand Mixer", "Artisan series 5-quart tilt-head stand mixer", 
                449.99, 40, "Home & Kitchen", "https://images.unsplash.com/photo-1594385208974-2e75f8d7bb48?w=400", 4.9, 8500);
            
            // Books
            addProduct("Atomic Habits", "By James Clear - Tiny changes, remarkable results", 
                16.99, 500, "Books", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400", 4.8, 125000);
            
            addProduct("The Psychology of Money", "By Morgan Housel - Timeless lessons on wealth", 
                19.99, 300, "Books", "https://images.unsplash.com/photo-1553729459-efe14ef6055d?w=400", 4.7, 45000);
            
            // Sports & Outdoors
            addProduct("Yeti Rambler 20oz", "Vacuum insulated tumbler with MagSlider lid", 
                35.00, 250, "Sports & Outdoors", "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=400", 4.8, 9800);
            
            addProduct("Fitbit Charge 5", "Advanced fitness tracker with built-in GPS", 
                149.95, 75, "Sports & Outdoors", "https://images.unsplash.com/photo-1575311373937-040b8e1fd5b6?w=400", 4.5, 6700);
            
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
