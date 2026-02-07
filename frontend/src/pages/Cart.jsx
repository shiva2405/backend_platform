import { useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import Loading from '../components/Loading';

const Cart = () => {
  const { cartItems, loading, updateQuantity, removeFromCart, cartTotal } = useCart();
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  if (!isAuthenticated) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-12 text-center">
        <svg className="mx-auto h-16 w-16 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
        </svg>
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Your cart is empty</h2>
        <p className="text-gray-600 mb-4">Sign in to see items in your cart</p>
        <button
          onClick={() => navigate('/login')}
          className="bg-amazon-yellow hover:bg-yellow-500 text-amazon-dark px-6 py-2 rounded-full font-medium transition"
        >
          Sign In
        </button>
      </div>
    );
  }

  if (loading) {
    return <Loading message="Loading your cart..." />;
  }

  if (cartItems.length === 0) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-12">
        <div className="bg-white rounded-lg p-8 text-center">
          <svg className="mx-auto h-16 w-16 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
          </svg>
          <h2 className="text-2xl font-bold text-gray-900 mb-2">Your cart is empty</h2>
          <p className="text-gray-600 mb-4">Looks like you haven't added anything to your cart yet.</p>
          <button
            onClick={() => navigate('/')}
            className="bg-amazon-yellow hover:bg-yellow-500 text-amazon-dark px-6 py-2 rounded-full font-medium transition"
          >
            Continue Shopping
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Shopping Cart</h1>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Cart Items */}
        <div className="lg:col-span-2">
          <div className="bg-white rounded-lg shadow">
            {cartItems.map((item, index) => (
              <div
                key={item.id}
                className={`p-6 ${index !== cartItems.length - 1 ? 'border-b border-gray-200' : ''}`}
              >
                <div className="flex gap-4">
                  {/* Product Image */}
                  <div className="w-24 h-24 flex-shrink-0">
                    <img
                      src={item.imageUrl || `https://via.placeholder.com/100x100?text=Product`}
                      alt={item.productName}
                      className="w-full h-full object-contain"
                      onError={(e) => {
                        e.target.src = `https://via.placeholder.com/100x100?text=Product`;
                      }}
                    />
                  </div>

                  {/* Product Info */}
                  <div className="flex-1">
                    <h3 
                      className="text-lg font-medium text-blue-600 hover:text-blue-800 cursor-pointer"
                      onClick={() => navigate(`/product/${item.productId}`)}
                    >
                      {item.productName}
                    </h3>
                    <p className="text-green-600 text-sm mt-1">In Stock</p>
                    
                    {/* Price */}
                    <p className="text-lg font-bold mt-2">
                      ${(item.price * item.quantity).toFixed(2)}
                    </p>

                    {/* Actions */}
                    <div className="flex items-center gap-4 mt-3">
                      {/* Quantity */}
                      <div className="flex items-center border border-gray-300 rounded">
                        <button
                          onClick={() => {
                            if (item.quantity > 1) {
                              updateQuantity(item.id, item.quantity - 1);
                            }
                          }}
                          className="px-3 py-1 hover:bg-gray-100 transition"
                        >
                          -
                        </button>
                        <span className="px-3 py-1 border-x border-gray-300">
                          {item.quantity}
                        </span>
                        <button
                          onClick={() => updateQuantity(item.id, item.quantity + 1)}
                          className="px-3 py-1 hover:bg-gray-100 transition"
                        >
                          +
                        </button>
                      </div>

                      <span className="text-gray-300">|</span>

                      {/* Delete */}
                      <button
                        onClick={() => removeFromCart(item.id)}
                        className="text-blue-600 hover:text-blue-800 text-sm"
                      >
                        Delete
                      </button>
                    </div>
                  </div>

                  {/* Unit Price */}
                  <div className="text-right">
                    <p className="text-sm text-gray-500">
                      ${item.price?.toFixed(2)} each
                    </p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Order Summary */}
        <div className="lg:col-span-1">
          <div className="bg-white rounded-lg shadow p-6 sticky top-24">
            <h2 className="text-lg font-semibold mb-4">Order Summary</h2>
            
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span>Items ({cartItems.reduce((sum, item) => sum + item.quantity, 0)}):</span>
                <span>${cartTotal.toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span>Shipping:</span>
                <span className="text-green-600">FREE</span>
              </div>
            </div>

            <hr className="my-4" />

            <div className="flex justify-between text-lg font-bold mb-4">
              <span>Order Total:</span>
              <span className="text-red-700">${cartTotal.toFixed(2)}</span>
            </div>

            <button
              onClick={() => navigate('/checkout')}
              className="w-full bg-amazon-yellow hover:bg-yellow-500 text-amazon-dark py-3 rounded-full font-medium transition"
            >
              Proceed to Checkout
            </button>

            <button
              onClick={() => navigate('/')}
              className="w-full mt-3 border border-gray-300 hover:bg-gray-50 py-2 rounded-full text-sm transition"
            >
              Continue Shopping
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Cart;
