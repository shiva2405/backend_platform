import { Link } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';

const ProductCard = ({ product }) => {
  const { addToCart } = useCart();
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [adding, setAdding] = useState(false);
  const [added, setAdded] = useState(false);

  const handleAddToCart = async (e) => {
    e.preventDefault();
    e.stopPropagation();
    
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    
    setAdding(true);
    const success = await addToCart(product.id, 1);
    setAdding(false);
    
    if (success) {
      setAdded(true);
      setTimeout(() => setAdded(false), 2000);
    }
  };

  const renderStars = (rating) => {
    const stars = [];
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 >= 0.5;
    
    for (let i = 0; i < 5; i++) {
      if (i < fullStars) {
        stars.push(
          <svg key={i} className="w-4 h-4 text-amber-400 fill-current drop-shadow-sm" viewBox="0 0 20 20">
            <path d="M10 15l-5.878 3.09 1.123-6.545L.489 6.91l6.572-.955L10 0l2.939 5.955 6.572.955-4.756 4.635 1.123 6.545z" />
          </svg>
        );
      } else if (i === fullStars && hasHalfStar) {
        stars.push(
          <svg key={i} className="w-4 h-4 text-amber-400 fill-current" viewBox="0 0 20 20">
            <defs>
              <linearGradient id={`half-${product.id}`}>
                <stop offset="50%" stopColor="#F59E0B" />
                <stop offset="50%" stopColor="#E5E7EB" />
              </linearGradient>
            </defs>
            <path fill={`url(#half-${product.id})`} d="M10 15l-5.878 3.09 1.123-6.545L.489 6.91l6.572-.955L10 0l2.939 5.955 6.572.955-4.756 4.635 1.123 6.545z" />
          </svg>
        );
      } else {
        stars.push(
          <svg key={i} className="w-4 h-4 text-gray-200 fill-current" viewBox="0 0 20 20">
            <path d="M10 15l-5.878 3.09 1.123-6.545L.489 6.91l6.572-.955L10 0l2.939 5.955 6.572.955-4.756 4.635 1.123 6.545z" />
          </svg>
        );
      }
    }
    return stars;
  };

  return (
    <Link to={`/product/${product.id}`} className="block group">
      <div className="bg-white rounded-2xl shadow-md hover:shadow-2xl transition-all duration-300 overflow-hidden transform hover:-translate-y-1 border border-gray-100">
        {/* Product Image */}
        <div className="relative h-52 bg-gradient-to-br from-gray-50 to-gray-100 overflow-hidden">
          <img
            src={product.imageUrl || `https://via.placeholder.com/300x300?text=${encodeURIComponent(product.name)}`}
            alt={product.name}
            className="w-full h-full object-contain p-4 group-hover:scale-110 transition-transform duration-500"
            onError={(e) => {
              e.target.src = `https://via.placeholder.com/300x300?text=${encodeURIComponent(product.name)}`;
            }}
          />
          
          {/* Badges */}
          <div className="absolute top-3 left-3 flex flex-col gap-2">
            {product.stockQuantity < 10 && product.stockQuantity > 0 && (
              <span className="bg-gradient-to-r from-red-500 to-pink-500 text-white text-xs font-semibold px-3 py-1 rounded-full shadow-lg">
                Only {product.stockQuantity} left!
              </span>
            )}
            {product.stockQuantity === 0 && (
              <span className="bg-gray-600 text-white text-xs font-semibold px-3 py-1 rounded-full shadow-lg">
                Out of Stock
              </span>
            )}
          </div>

          {/* Quick View Overlay */}
          <div className="absolute inset-0 bg-black/0 group-hover:bg-black/5 transition-colors duration-300 flex items-center justify-center">
            <span className="opacity-0 group-hover:opacity-100 transition-opacity duration-300 bg-white/90 text-gray-800 text-sm font-medium px-4 py-2 rounded-full shadow-lg">
              Quick View
            </span>
          </div>
        </div>

        {/* Product Info */}
        <div className="p-4">
          {/* Category Tag */}
          <span className="inline-block text-xs font-semibold text-[#FF6B35] uppercase tracking-wider mb-2 bg-[#FF6B35]/10 px-2 py-1 rounded-md">
            {product.category || 'General'}
          </span>
          
          {/* Product Name */}
          <h3 className="text-base font-semibold text-gray-800 line-clamp-2 min-h-[48px] group-hover:text-[#FF6B35] transition-colors duration-300">
            {product.name}
          </h3>
          
          {/* Rating */}
          <div className="flex items-center mt-2 gap-1">
            <div className="flex">{renderStars(product.rating || 4)}</div>
            <span className="text-xs font-medium text-[#00A896]">
              ({product.reviewCount?.toLocaleString() || 0})
            </span>
          </div>

          {/* Price */}
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-2xl font-bold bg-gradient-to-r from-[#1a1a2e] to-[#16213e] bg-clip-text text-transparent">
              ${product.price?.toFixed(2)}
            </span>
            {product.price > 100 && (
              <span className="text-xs text-green-600 font-medium">Free Shipping</span>
            )}
          </div>

          {/* Add to Cart Button */}
          <button
            onClick={handleAddToCart}
            disabled={adding || product.stockQuantity === 0}
            className={`w-full mt-4 py-3 px-4 rounded-xl text-sm font-semibold transition-all duration-300 flex items-center justify-center gap-2 ${
              added
                ? 'bg-gradient-to-r from-green-500 to-emerald-500 text-white shadow-lg shadow-green-500/30'
                : product.stockQuantity === 0
                ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                : 'bg-gradient-to-r from-[#FF6B35] to-[#F7931E] hover:from-[#F7931E] hover:to-[#FF6B35] text-white shadow-lg shadow-orange-500/30 hover:shadow-xl hover:shadow-orange-500/40'
            }`}
          >
            {adding ? (
              <>
                <svg className="animate-spin h-5 w-5" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                <span>Adding...</span>
              </>
            ) : added ? (
              <>
                <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth={2.5} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                </svg>
                <span>Added to Cart!</span>
              </>
            ) : product.stockQuantity === 0 ? (
              'Out of Stock'
            ) : (
              <>
                <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
                <span>Add to Cart</span>
              </>
            )}
          </button>
        </div>
      </div>
    </Link>
  );
};

export default ProductCard;
