import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { productAPI } from '../services/api';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import Loading from '../components/Loading';

const ProductDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addToCart } = useCart();
  const { isAuthenticated } = useAuth();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [quantity, setQuantity] = useState(1);
  const [adding, setAdding] = useState(false);
  const [added, setAdded] = useState(false);

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        setLoading(true);
        const response = await productAPI.getById(id);
        setProduct(response.data);
      } catch (error) {
        console.error('Failed to fetch product:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchProduct();
  }, [id]);

  const handleAddToCart = async () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    
    setAdding(true);
    const success = await addToCart(product.id, quantity);
    setAdding(false);
    
    if (success) {
      setAdded(true);
      setTimeout(() => setAdded(false), 3000);
    }
  };

  const handleBuyNow = async () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    
    await addToCart(product.id, quantity);
    navigate('/cart');
  };

  const renderStars = (rating) => {
    const stars = [];
    const fullStars = Math.floor(rating);
    
    for (let i = 0; i < 5; i++) {
      stars.push(
        <svg
          key={i}
          className={`w-5 h-5 ${i < fullStars ? 'text-yellow-400' : 'text-gray-300'} fill-current`}
          viewBox="0 0 20 20"
        >
          <path d="M10 15l-5.878 3.09 1.123-6.545L.489 6.91l6.572-.955L10 0l2.939 5.955 6.572.955-4.756 4.635 1.123 6.545z" />
        </svg>
      );
    }
    return stars;
  };

  if (loading) {
    return <Loading message="Loading product details..." />;
  }

  if (!product) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-12 text-center">
        <h2 className="text-2xl font-bold text-gray-900">Product not found</h2>
        <button
          onClick={() => navigate('/')}
          className="mt-4 text-amazon-orange hover:underline"
        >
          Return to Home
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      {/* Breadcrumb */}
      <nav className="mb-6 text-sm">
        <button onClick={() => navigate('/')} className="text-blue-600 hover:underline">
          Home
        </button>
        <span className="mx-2 text-gray-400">/</span>
        <button 
          onClick={() => navigate(`/category/${product.category}`)} 
          className="text-blue-600 hover:underline"
        >
          {product.category}
        </button>
        <span className="mx-2 text-gray-400">/</span>
        <span className="text-gray-600">{product.name}</span>
      </nav>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        {/* Product Image */}
        <div className="bg-white rounded-lg p-8">
          <img
            src={product.imageUrl || `https://via.placeholder.com/500x500?text=${encodeURIComponent(product.name)}`}
            alt={product.name}
            className="w-full h-auto max-h-[500px] object-contain"
            onError={(e) => {
              e.target.src = `https://via.placeholder.com/500x500?text=${encodeURIComponent(product.name)}`;
            }}
          />
        </div>

        {/* Product Info */}
        <div>
          <h1 className="text-2xl md:text-3xl font-bold text-gray-900 mb-2">
            {product.name}
          </h1>

          {/* Rating */}
          <div className="flex items-center mb-4">
            <div className="flex">{renderStars(product.rating || 4)}</div>
            <span className="ml-2 text-blue-600 hover:text-blue-800 cursor-pointer">
              {product.reviewCount?.toLocaleString() || 0} ratings
            </span>
          </div>

          <hr className="my-4" />

          {/* Price */}
          <div className="mb-4">
            <span className="text-sm text-gray-500">Price:</span>
            <span className="text-3xl font-bold text-gray-900 ml-2">
              ${product.price?.toFixed(2)}
            </span>
          </div>

          {/* Description */}
          <div className="mb-6">
            <h3 className="font-semibold text-gray-900 mb-2">About this item</h3>
            <p className="text-gray-600">{product.description}</p>
          </div>

          <hr className="my-4" />

          {/* Purchase Box */}
          <div className="bg-white rounded-lg border border-gray-200 p-4">
            <div className="text-2xl font-bold text-gray-900 mb-2">
              ${product.price?.toFixed(2)}
            </div>

            {/* Stock Status */}
            {product.stockQuantity > 0 ? (
              <p className="text-lg text-green-600 mb-4">In Stock</p>
            ) : (
              <p className="text-lg text-red-600 mb-4">Out of Stock</p>
            )}

            {product.stockQuantity > 0 && (
              <>
                {/* Quantity Selector */}
                <div className="mb-4">
                  <label className="text-sm text-gray-700 mr-2">Qty:</label>
                  <select
                    value={quantity}
                    onChange={(e) => setQuantity(parseInt(e.target.value))}
                    className="border border-gray-300 rounded px-3 py-1 focus:outline-none focus:ring-2 focus:ring-amazon-orange"
                  >
                    {[...Array(Math.min(10, product.stockQuantity))].map((_, i) => (
                      <option key={i + 1} value={i + 1}>
                        {i + 1}
                      </option>
                    ))}
                  </select>
                </div>

                {/* Add to Cart Button */}
                <button
                  onClick={handleAddToCart}
                  disabled={adding}
                  className={`w-full py-2 px-4 rounded-full text-sm font-medium mb-2 transition ${
                    added
                      ? 'bg-green-500 text-white'
                      : 'bg-amazon-yellow hover:bg-yellow-500 text-amazon-dark'
                  }`}
                >
                  {adding ? 'Adding...' : added ? '✓ Added to Cart' : 'Add to Cart'}
                </button>

                {/* Buy Now Button */}
                <button
                  onClick={handleBuyNow}
                  className="w-full py-2 px-4 rounded-full text-sm font-medium bg-amazon-orange hover:bg-orange-500 text-white transition"
                >
                  Buy Now
                </button>
              </>
            )}

            {/* Additional Info */}
            <div className="mt-4 pt-4 border-t border-gray-200">
              <div className="flex items-center text-sm text-gray-600 mb-2">
                <svg className="w-5 h-5 mr-2 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
                Secure transaction
              </div>
              <div className="flex items-center text-sm text-gray-600">
                <svg className="w-5 h-5 mr-2 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 12H4" />
                </svg>
                Ships from ShopEase
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductDetail;
