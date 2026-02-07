import { createContext, useContext, useState, useEffect } from 'react';
import { cartAPI } from '../services/api';
import { useAuth } from './AuthContext';

const CartContext = createContext(null);

export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
};

export const CartProvider = ({ children }) => {
  const [cartItems, setCartItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const { isAuthenticated } = useAuth();

  const fetchCart = async () => {
    if (!isAuthenticated) {
      setCartItems([]);
      return;
    }
    
    try {
      setLoading(true);
      const response = await cartAPI.getCart();
      setCartItems(response.data);
    } catch (error) {
      console.error('Failed to fetch cart:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCart();
  }, [isAuthenticated]);

  const addToCart = async (productId, quantity = 1) => {
    try {
      await cartAPI.addToCart(productId, quantity);
      await fetchCart();
      return true;
    } catch (error) {
      console.error('Failed to add to cart:', error);
      return false;
    }
  };

  const updateQuantity = async (itemId, quantity) => {
    try {
      await cartAPI.updateQuantity(itemId, quantity);
      await fetchCart();
    } catch (error) {
      console.error('Failed to update quantity:', error);
    }
  };

  const removeFromCart = async (itemId) => {
    try {
      await cartAPI.removeItem(itemId);
      await fetchCart();
    } catch (error) {
      console.error('Failed to remove from cart:', error);
    }
  };

  const clearCart = async () => {
    try {
      await cartAPI.clearCart();
      setCartItems([]);
    } catch (error) {
      console.error('Failed to clear cart:', error);
    }
  };

  const cartTotal = cartItems.reduce(
    (total, item) => total + (item.price || 0) * item.quantity,
    0
  );

  const cartCount = cartItems.reduce((count, item) => count + item.quantity, 0);

  const value = {
    cartItems,
    loading,
    addToCart,
    updateQuantity,
    removeFromCart,
    clearCart,
    fetchCart,
    cartTotal,
    cartCount,
  };

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
};
