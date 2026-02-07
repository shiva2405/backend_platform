const Loading = ({ message = 'Loading...' }) => {
  return (
    <div className="flex flex-col items-center justify-center min-h-[400px] animate-fade-in">
      {/* Animated Logo Spinner */}
      <div className="relative">
        <div className="w-16 h-16 rounded-full border-4 border-gray-200"></div>
        <div className="absolute top-0 left-0 w-16 h-16 rounded-full border-4 border-transparent border-t-[#FF6B35] border-r-[#F7931E] animate-spin"></div>
        <div className="absolute top-2 left-2 w-12 h-12 rounded-full border-4 border-transparent border-t-[#00A896] border-r-[#00D4BE] animate-spin" style={{ animationDirection: 'reverse', animationDuration: '0.8s' }}></div>
      </div>
      
      {/* Loading Text */}
      <p className="mt-6 text-[#64748B] font-medium flex items-center gap-1">
        {message}
        <span className="flex gap-1 ml-1">
          <span className="w-1.5 h-1.5 bg-[#FF6B35] rounded-full animate-bounce" style={{ animationDelay: '0ms' }}></span>
          <span className="w-1.5 h-1.5 bg-[#F7931E] rounded-full animate-bounce" style={{ animationDelay: '150ms' }}></span>
          <span className="w-1.5 h-1.5 bg-[#00D4BE] rounded-full animate-bounce" style={{ animationDelay: '300ms' }}></span>
        </span>
      </p>
    </div>
  );
};

export default Loading;
