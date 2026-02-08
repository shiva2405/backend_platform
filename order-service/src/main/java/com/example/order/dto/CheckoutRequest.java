package com.example.order.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class CheckoutRequest {
    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;
    
    private List<CartItemDTO> cartItems;
    
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    
    public List<CartItemDTO> getCartItems() { return cartItems; }
    public void setCartItems(List<CartItemDTO> cartItems) { this.cartItems = cartItems; }
}
