import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { useState } from 'react';

// SVG Icons as components for better visibility
const SearchIcon = () => (
  <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth={2.5} viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
  </svg>
);

const CartIcon = () => (
  <svg className="w-8 h-8" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
  </svg>
);

const MenuIcon = () => (
  <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" d="M4 6h16M4 12h16M4 18h16" />
  </svg>
);

const UserIcon = () => (
  <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
  </svg>
);

const OrderIcon = () => (
  <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
  </svg>
);

const ChevronDownIcon = () => (
  <svg className="w-4 h-4 ml-1" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" d="M19 9l-7 7-7-7" />
  </svg>
);

const Header = () => {
  const { user, logout, isAdmin } = useAuth();
  const { cartCount } = useCart();
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [showDropdown, setShowDropdown] = useState(false);

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchQuery)}`);
    }
  };

  return (
    <header className="sticky top-0 z-50 shadow-lg">
      {/* Main Header - Gradient Background */}
      <div className="bg-gradient-to-r from-[#1a1a2e] via-[#16213e] to-[#0f3460] text-white">
        <div className="max-w-7xl mx-auto flex items-center justify-between px-4 py-3">
          {/* Logo */}
          <Link to="/" className="flex items-center group">
            <div className="flex items-center">
              <span className="text-3xl font-extrabold bg-gradient-to-r from-[#FF6B35] to-[#F7931E] bg-clip-text text-transparent group-hover:from-[#F7931E] group-hover:to-[#FF6B35] transition-all duration-300">
                Shop
              </span>
              <span className="text-3xl font-extrabold text-white group-hover:text-[#00D4BE] transition-colors duration-300">
                Ease
              </span>
            </div>
          </Link>

          {/* Search Bar */}
          <form onSubmit={handleSearch} className="flex-1 max-w-2xl mx-6">
            <div className="flex rounded-lg overflow-hidden shadow-md hover:shadow-xl transition-shadow duration-300">
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search for products, brands and more..."
                className="flex-1 px-5 py-3 text-gray-800 bg-white focus:outline-none focus:ring-2 focus:ring-[#FF6B35] text-sm font-medium placeholder-gray-400"
              />
              <button
                type="submit"
                className="bg-gradient-to-r from-[#FF6B35] to-[#F7931E] hover:from-[#F7931E] hover:to-[#FF6B35] px-6 py-3 text-white font-semibold transition-all duration-300 flex items-center gap-2"
              >
                <SearchIcon />
                <span className="hidden sm:inline">Search</span>
              </button>
            </div>
          </form>

          {/* Right Side Actions */}
          <div className="flex items-center space-x-4">
            {/* Account */}
            <div className="relative"
              onMouseEnter={() => setShowDropdown(true)}
              onMouseLeave={() => setShowDropdown(false)}
            >
              {user ? (
                <button className="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-white/10 transition-colors duration-200">
                  <div className="w-9 h-9 bg-gradient-to-br from-[#00A896] to-[#00D4BE] rounded-full flex items-center justify-center text-white font-bold text-sm shadow-md">
                    {user.fullName?.charAt(0).toUpperCase() || 'U'}
                  </div>
                  <div className="text-left hidden md:block">
                    <p className="text-xs text-gray-300">Hello, {user.fullName?.split(' ')[0]}</p>
                    <p className="text-sm font-semibold flex items-center">
                      Account
                      <ChevronDownIcon />
                    </p>
                  </div>
                </button>
              ) : (
                <Link 
                  to="/login" 
                  className="flex items-center gap-2 px-4 py-2 rounded-lg bg-gradient-to-r from-[#FF6B35] to-[#F7931E] hover:from-[#F7931E] hover:to-[#FF6B35] transition-all duration-300 shadow-md hover:shadow-lg"
                >
                  <UserIcon />
                  <span className="font-semibold">Sign In</span>
                </Link>
              )}
              
              {/* Dropdown Menu */}
              {user && showDropdown && (
                <div className="absolute right-0 mt-2 w-56 bg-white text-gray-800 rounded-xl shadow-2xl border border-gray-100 overflow-hidden animate-fade-in z-50">
                  <div className="p-4 bg-gradient-to-r from-[#f8f9fa] to-[#e9ecef] border-b">
                    <p className="font-semibold text-gray-900">{user.fullName}</p>
                    <p className="text-xs text-gray-500">{user.email || user.username}</p>
                  </div>
                  <div className="py-2">
                    <Link 
                      to="/orders" 
                      className="flex items-center gap-3 px-4 py-3 hover:bg-[#FF6B35]/10 transition-colors"
                    >
                      <OrderIcon />
                      <span>My Orders</span>
                    </Link>
                    {isAdmin() && (
                      <Link 
                        to="/admin" 
                        className="flex items-center gap-3 px-4 py-3 hover:bg-[#00A896]/10 text-[#00A896] font-semibold transition-colors"
                      >
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                          <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        </svg>
                        <span>Admin Panel</span>
                      </Link>
                    )}
                    <hr className="my-2 border-gray-100" />
                    <button
                      onClick={logout}
                      className="flex items-center gap-3 w-full px-4 py-3 text-red-500 hover:bg-red-50 transition-colors"
                    >
                      <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth={2} viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                      </svg>
                      <span>Sign Out</span>
                    </button>
                  </div>
                </div>
              )}
            </div>

            {/* Orders */}
            <Link 
              to="/orders" 
              className="hidden md:flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-white/10 transition-colors duration-200"
            >
              <OrderIcon />
              <div className="text-left">
                <p className="text-xs text-gray-300">Returns</p>
                <p className="text-sm font-semibold">& Orders</p>
              </div>
            </Link>

            {/* Cart */}
            <Link 
              to="/cart" 
              className="flex items-center gap-1 px-3 py-2 rounded-lg hover:bg-white/10 transition-colors duration-200 relative group"
            >
              <div className="relative">
                <CartIcon />
                {cartCount > 0 && (
                  <span className="absolute -top-2 -right-2 bg-gradient-to-r from-[#FF6B35] to-[#F7931E] text-white text-xs font-bold rounded-full min-w-[22px] h-[22px] flex items-center justify-center shadow-lg animate-pulse-subtle">
                    {cartCount > 99 ? '99+' : cartCount}
                  </span>
                )}
              </div>
              <span className="font-semibold hidden sm:inline">Cart</span>
            </Link>
          </div>
        </div>
      </div>

      {/* Sub Header - Categories */}
      <div className="bg-[#0f3460] text-white px-4 py-2 shadow-md">
        <div className="max-w-7xl mx-auto flex items-center space-x-1 text-sm overflow-x-auto scrollbar-hide">
          <Link 
            to="/" 
            className="flex items-center gap-1 px-4 py-2 rounded-full bg-white/10 hover:bg-[#FF6B35] transition-all duration-300 font-medium whitespace-nowrap"
          >
            <MenuIcon />
            <span>All</span>
          </Link>
          {['Electronics', 'Clothing', 'Home & Kitchen', 'Books', 'Sports & Outdoors'].map((category) => (
            <Link
              key={category}
              to={`/category/${category}`}
              className="px-4 py-2 rounded-full hover:bg-[#FF6B35] transition-all duration-300 font-medium whitespace-nowrap"
            >
              {category}
            </Link>
          ))}
        </div>
      </div>
    </header>
  );
};

export default Header;
