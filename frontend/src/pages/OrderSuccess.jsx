import { useLocation, useNavigate } from 'react-router-dom';

const OrderSuccess = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const order = location.state?.order;

  if (!order) {
    navigate('/');
    return null;
  }

  return (
    <div className="max-w-2xl mx-auto px-4 py-12">
      <div className="bg-white rounded-lg shadow p-8 text-center">
        {/* Success Icon */}
        <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
          <svg className="w-10 h-10 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
          </svg>
        </div>

        <h1 className="text-3xl font-bold text-gray-900 mb-2">Order Placed Successfully!</h1>
        <p className="text-gray-600 mb-6">
          Thank you for your order. We'll send you a confirmation email shortly.
        </p>

        {/* Order Details */}
        <div className="bg-gray-50 rounded-lg p-6 text-left mb-6">
          <h2 className="font-semibold text-lg mb-4">Order Details</h2>
          
          <div className="space-y-2 text-sm">
            <div className="flex justify-between">
              <span className="text-gray-600">Order Number:</span>
              <span className="font-medium">#{order.id}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Order Date:</span>
              <span className="font-medium">
                {new Date(order.orderDate).toLocaleDateString('en-US', {
                  year: 'numeric',
                  month: 'long',
                  day: 'numeric',
                  hour: '2-digit',
                  minute: '2-digit'
                })}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Status:</span>
              <span className="inline-flex items-center px-2 py-1 rounded text-xs font-medium bg-yellow-100 text-yellow-800">
                {order.status}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Total Amount:</span>
              <span className="font-bold text-lg text-green-600">${order.totalAmount.toFixed(2)}</span>
            </div>
          </div>

          <hr className="my-4" />

          <h3 className="font-medium mb-2">Shipping Address:</h3>
          <p className="text-gray-600">{order.shippingAddress}</p>

          <hr className="my-4" />

          <h3 className="font-medium mb-2">Items Ordered:</h3>
          <div className="space-y-2">
            {order.items?.map((item) => (
              <div key={item.id} className="flex justify-between text-sm">
                <span>{item.productName} × {item.quantity}</span>
                <span>${(item.price * item.quantity).toFixed(2)}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Actions */}
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <button
            onClick={() => navigate('/orders')}
            className="bg-amazon-dark hover:bg-gray-800 text-white px-6 py-2 rounded-full font-medium transition"
          >
            View My Orders
          </button>
          <button
            onClick={() => navigate('/')}
            className="bg-amazon-yellow hover:bg-yellow-500 text-amazon-dark px-6 py-2 rounded-full font-medium transition"
          >
            Continue Shopping
          </button>
        </div>
      </div>
    </div>
  );
};

export default OrderSuccess;
