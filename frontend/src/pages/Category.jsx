import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { productAPI } from '../services/api';
import ProductCard from '../components/ProductCard';
import Loading from '../components/Loading';

const Category = () => {
  const { category } = useParams();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        setLoading(true);
        const response = await productAPI.getByCategory(category);
        setProducts(response.data);
      } catch (error) {
        console.error('Failed to fetch products:', error);
        // Fallback: fetch all and filter
        try {
          const allResponse = await productAPI.getAll();
          const filtered = allResponse.data.filter(
            p => p.category?.toLowerCase() === category.toLowerCase()
          );
          setProducts(filtered);
        } catch (e) {
          setProducts([]);
        }
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, [category]);

  if (loading) {
    return <Loading message={`Loading ${category}...`} />;
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold text-gray-900 mb-2">{category}</h1>
      <p className="text-gray-600 mb-6">
        {products.length} {products.length === 1 ? 'product' : 'products'} available
      </p>

      {products.length === 0 ? (
        <div className="bg-white rounded-lg shadow p-8 text-center">
          <svg className="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
          </svg>
          <h2 className="text-xl font-semibold text-gray-900 mb-2">No products in this category</h2>
          <p className="text-gray-600">Check back later for new arrivals!</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
          {products.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
      )}
    </div>
  );
};

export default Category;
