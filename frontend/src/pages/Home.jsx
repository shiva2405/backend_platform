import { useState, useEffect } from 'react';
import { productAPI } from '../services/api';
import ProductCard from '../components/ProductCard';
import Loading from '../components/Loading';

const Home = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedCategory, setSelectedCategory] = useState('All');

  const categories = [
    { name: 'All', icon: '🛒' },
    { name: 'Electronics', icon: '📱' },
    { name: 'Clothing', icon: '👕' },
    { name: 'Home & Kitchen', icon: '🏠' },
    { name: 'Books', icon: '📚' },
    { name: 'Sports & Outdoors', icon: '⚽' }
  ];

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        setLoading(true);
        const response = await productAPI.getAll();
        setProducts(response.data);
      } catch (error) {
        console.error('Failed to fetch products:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchProducts();
  }, []);

  const filteredProducts = selectedCategory === 'All' 
    ? products 
    : products.filter(p => p.category === selectedCategory);

  if (loading) {
    return <Loading message="Loading products..." />;
  }

  return (
    <div className="min-h-screen bg-[#F1F5F9]">
      {/* Hero Banner */}
      <div className="relative overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-br from-[#667eea] via-[#764ba2] to-[#FF6B35]"></div>
        <div className="absolute inset-0 bg-[url('data:image/svg+xml,%3Csvg width=%2260%22 height=%2260%22 viewBox=%220 0 60 60%22 xmlns=%22http://www.w3.org/2000/svg%22%3E%3Cg fill=%22none%22 fill-rule=%22evenodd%22%3E%3Cg fill=%22%23ffffff%22 fill-opacity=%220.05%22%3E%3Cpath d=%22M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z%22/%3E%3C/g%3E%3C/g%3E%3C/svg%3E')] opacity-30"></div>
        <div className="relative max-w-7xl mx-auto px-4 py-16 md:py-24">
          <div className="text-center">
            <h1 className="text-4xl md:text-6xl font-extrabold text-white mb-6 drop-shadow-lg animate-fade-in">
              Welcome to <span className="text-[#FFD93D]">ShopEase</span>
            </h1>
            <p className="text-xl md:text-2xl text-white/90 mb-8 max-w-2xl mx-auto">
              Discover amazing deals on millions of products. Quality meets affordability.
            </p>
            <div className="flex flex-wrap justify-center gap-4">
              <div className="flex items-center gap-2 bg-white/20 backdrop-blur-sm text-white px-6 py-3 rounded-full text-sm font-semibold shadow-lg">
                <svg className="w-5 h-5 text-[#FFD93D]" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                </svg>
                Free Shipping on orders over $50
              </div>
              <div className="flex items-center gap-2 bg-white/20 backdrop-blur-sm text-white px-6 py-3 rounded-full text-sm font-semibold shadow-lg">
                <svg className="w-5 h-5 text-[#00D4BE]" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M18.364 5.636l-3.536 3.536m0 5.656l3.536 3.536M9.172 9.172L5.636 5.636m3.536 9.192l-3.536 3.536M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                24/7 Customer Support
              </div>
              <div className="flex items-center gap-2 bg-white/20 backdrop-blur-sm text-white px-6 py-3 rounded-full text-sm font-semibold shadow-lg">
                <svg className="w-5 h-5 text-[#FF6B9D]" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                </svg>
                Secure Payments
              </div>
            </div>
          </div>
        </div>
        
        {/* Wave decoration */}
        <div className="absolute bottom-0 left-0 right-0">
          <svg viewBox="0 0 1440 120" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M0 120L60 105C120 90 240 60 360 45C480 30 600 30 720 37.5C840 45 960 60 1080 67.5C1200 75 1320 75 1380 75L1440 75V120H1380C1320 120 1200 120 1080 120C960 120 840 120 720 120C600 120 480 120 360 120C240 120 120 120 60 120H0Z" fill="#F1F5F9"/>
          </svg>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 pb-16 -mt-4">
        {/* Category Filter */}
        <div className="mb-10">
          <h2 className="text-2xl font-bold text-[#1E293B] mb-6 flex items-center gap-3">
            <span className="w-1.5 h-8 bg-gradient-to-b from-[#FF6B35] to-[#F7931E] rounded-full"></span>
            Shop by Category
          </h2>
          <div className="flex flex-wrap gap-3">
            {categories.map((category) => (
              <button
                key={category.name}
                onClick={() => setSelectedCategory(category.name)}
                className={`flex items-center gap-2 px-5 py-3 rounded-xl text-sm font-semibold transition-all duration-300 ${
                  selectedCategory === category.name
                    ? 'bg-gradient-to-r from-[#FF6B35] to-[#F7931E] text-white shadow-lg shadow-orange-500/30 scale-105'
                    : 'bg-white text-[#1E293B] border-2 border-gray-100 hover:border-[#FF6B35]/30 hover:shadow-md'
                }`}
              >
                <span className="text-lg">{category.icon}</span>
                {category.name}
              </button>
            ))}
          </div>
        </div>

        {/* Products Grid */}
        <div className="mb-6 flex items-center justify-between">
          <h2 className="text-2xl font-bold text-[#1E293B] flex items-center gap-3">
            <span className="w-1.5 h-8 bg-gradient-to-b from-[#00A896] to-[#00D4BE] rounded-full"></span>
            {selectedCategory === 'All' ? 'All Products' : selectedCategory}
          </h2>
          <span className="text-[#64748B] bg-white px-4 py-2 rounded-full shadow-sm font-medium">
            {filteredProducts.length} {filteredProducts.length === 1 ? 'product' : 'products'}
          </span>
        </div>

        {filteredProducts.length === 0 ? (
          <div className="bg-white rounded-2xl shadow-lg p-12 text-center">
            <div className="w-20 h-20 bg-gradient-to-br from-gray-100 to-gray-200 rounded-full flex items-center justify-center mx-auto mb-6">
              <svg className="h-10 w-10 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
              </svg>
            </div>
            <h3 className="text-xl font-semibold text-[#1E293B] mb-2">No products found</h3>
            <p className="text-[#64748B]">Try selecting a different category</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
            {filteredProducts.map((product, index) => (
              <div 
                key={product.id} 
                className="animate-fade-in"
                style={{ animationDelay: `${index * 50}ms` }}
              >
                <ProductCard product={product} />
              </div>
            ))}
          </div>
        )}

        {/* Featured Section */}
        <div className="mt-20">
          <h2 className="text-2xl font-bold text-[#1E293B] mb-8 flex items-center gap-3">
            <span className="w-1.5 h-8 bg-gradient-to-b from-[#667eea] to-[#764ba2] rounded-full"></span>
            Why Shop with Us?
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {/* Quality Card */}
            <div className="group bg-white p-8 rounded-2xl shadow-lg hover:shadow-2xl transition-all duration-300 transform hover:-translate-y-2 border border-gray-100">
              <div className="w-16 h-16 bg-gradient-to-br from-[#FF6B35] to-[#F7931E] rounded-2xl flex items-center justify-center mb-6 shadow-lg shadow-orange-500/30 group-hover:scale-110 transition-transform duration-300">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z" />
                </svg>
              </div>
              <h3 className="text-xl font-bold text-[#1E293B] mb-3">Premium Quality</h3>
              <p className="text-[#64748B] leading-relaxed">Only the best products from verified sellers. Every item is quality-checked before delivery.</p>
            </div>

            {/* Prices Card */}
            <div className="group bg-white p-8 rounded-2xl shadow-lg hover:shadow-2xl transition-all duration-300 transform hover:-translate-y-2 border border-gray-100">
              <div className="w-16 h-16 bg-gradient-to-br from-[#00A896] to-[#00D4BE] rounded-2xl flex items-center justify-center mb-6 shadow-lg shadow-teal-500/30 group-hover:scale-110 transition-transform duration-300">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h3 className="text-xl font-bold text-[#1E293B] mb-3">Best Prices</h3>
              <p className="text-[#64748B] leading-relaxed">Competitive pricing on all products. Get the best deals with our price-match guarantee.</p>
            </div>

            {/* Delivery Card */}
            <div className="group bg-white p-8 rounded-2xl shadow-lg hover:shadow-2xl transition-all duration-300 transform hover:-translate-y-2 border border-gray-100">
              <div className="w-16 h-16 bg-gradient-to-br from-[#667eea] to-[#764ba2] rounded-2xl flex items-center justify-center mb-6 shadow-lg shadow-purple-500/30 group-hover:scale-110 transition-transform duration-300">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
              </div>
              <h3 className="text-xl font-bold text-[#1E293B] mb-3">Fast Delivery</h3>
              <p className="text-[#64748B] leading-relaxed">Quick and reliable shipping. Track your orders in real-time with our delivery system.</p>
            </div>
          </div>
        </div>

        {/* Trust Badges */}
        <div className="mt-16 bg-gradient-to-r from-[#1a1a2e] via-[#16213e] to-[#0f3460] rounded-2xl p-8 text-white">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-6 text-center">
            <div>
              <div className="text-4xl font-extrabold text-[#FFD93D] mb-2">10K+</div>
              <div className="text-gray-300 text-sm">Happy Customers</div>
            </div>
            <div>
              <div className="text-4xl font-extrabold text-[#00D4BE] mb-2">5K+</div>
              <div className="text-gray-300 text-sm">Products</div>
            </div>
            <div>
              <div className="text-4xl font-extrabold text-[#FF6B9D] mb-2">99%</div>
              <div className="text-gray-300 text-sm">Satisfaction Rate</div>
            </div>
            <div>
              <div className="text-4xl font-extrabold text-[#A78BFA] mb-2">24/7</div>
              <div className="text-gray-300 text-sm">Support Available</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Home;
