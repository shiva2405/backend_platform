import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { orderAPI } from '../services/api';

const Checkout = () => {
  const { cartItems, cartTotal, fetchCart } = useCart();
  const { user, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [address, setAddress] = useState(user?.address || '');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  if (!isAuthenticated) {
    navigate('/login');
    return null;
  }

  if (cartItems.length === 0) {
    navigate('/cart');
    return null;
  }

  const handlePlaceOrder = async (e) => {
    e.preventDefault();
    
    if (!address.trim()) {
      setError('Please enter a shipping address');
      return;
    }

    try {
      setLoading(true);
      setError('');
      const response = await orderAPI.checkout(address);
      await fetchCart(); // Clear cart locally
      navigate('/order-success', { state: { order: response.data } });
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to place order. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Checkout</h1>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {/* Shipping Information */}
        <div>
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-xl font-semibold mb-4">Shipping Address</h2>
            
            <form onSubmit={handlePlaceOrder}>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Full Name
                </label>
                <input
                  type="text"
                  value={user?.fullName || ''}
                  disabled
                  className="w-full px-3 py-2 border border-gray-300 rounded bg-gray-100"
                />
              </div>

              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Email
                </label>
                <input
                  type="email"
                  value={user?.email || ''}
                  disabled
                  className="w-full px-3 py-2 border border-gray-300 rounded bg-gray-100"
                />
              </div>

              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Shipping Address *
                </label>
                <textarea
                  value={address}
                  onChange={(e) => setAddress(e.target.value)}
                  rows={3}
                  placeholder="Enter your full shipping address"
                  className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-amazon-orange"
                  required
                />
              </div>

              {error && (
                <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded">
                  {error}
                </div>
              )}

              <button
                type="submit"
                disabled={loading}
                className="w-full bg-amazon-orange hover:bg-orange-500 text-white py-3 rounded-full font-medium transition disabled:opacity-50"
              >
                {loading ? 'Processing...' : 'Place Order'}
              </button>
            </form>
          </div>

          {/* Payment Info (Mock) */}
          <div className="bg-white rounded-lg shadow p-6 mt-6">
            <h2 className="text-xl font-semibold mb-4">Payment Method</h2>
            <div className="border border-green-500 rounded p-4 bg-green-50">
              <div className="flex items-center">
                <svg className="w-5 h-5 text-green-500 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
                <span className="font-medium">Cash on Delivery</span>
              </div>
              <p className="text-sm text-gray-600 mt-2">
                Pay when your order arrives at your doorstep.
              </p>
            </div>
          </div>
        </div>

        {/* Order Summary */}
        <div>
          <div className="bg-white rounded-lg shadow p-6 sticky top-24">
            <h2 className="text-xl font-semibold mb-4">Order Summary</h2>

            {/* Items */}
            <div className="space-y-4 max-h-64 overflow-y-auto mb-4">
              {cartItems.map((item) => (
                <div key={item.id} className="flex gap-3">
                  <img
                    src={item.imageUrl || `https://via.placeholder.com/60x60?text=Product`}
                    alt={item.productName}
                    className="w-16 h-16 object-contain"
                    onError={(e) => {
                      e.target.src = `https://via.placeholder.com/60x60?text=Product`;
                    }}
                  />
                  <div className="flex-1">
                    <p className="text-sm font-medium line-clamp-2">{item.productName}</p>
                    <p className="text-sm text-gray-500">Qty: {item.quantity}</p>
                  </div>
                  <p className="font-medium">${(item.price * item.quantity).toFixed(2)}</p>
                </div>
              ))}
            </div>

            <hr className="my-4" />

            {/* Totals */}
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span>Subtotal:</span>
                <span>${cartTotal.toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span>Shipping:</span>
                <span className="text-green-600">FREE</span>
              </div>
              <div className="flex justify-between">
                <span>Tax:</span>
                <span>$0.00</span>
              </div>
            </div>

            <hr className="my-4" />

            <div className="flex justify-between text-xl font-bold">
              <span>Total:</span>
              <span className="text-red-700">${cartTotal.toFixed(2)}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Checkout;
