#!/bin/bash

# E-commerce Platform Startup Script
# This script starts all the microservices and frontend

echo "=========================================="
echo "  ShopEase E-commerce Platform Startup"
echo "=========================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to check if a port is in use
check_port() {
    if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null ; then
        return 0
    else
        return 1
    fi
}

# Function to start a service
start_service() {
    local name=$1
    local dir=$2
    local port=$3
    
    echo -e "${YELLOW}Starting $name on port $port...${NC}"
    
    if check_port $port; then
        echo -e "${RED}Port $port is already in use. Please stop the existing service.${NC}"
        return 1
    fi
    
    cd "$dir"
    mvn spring-boot:run > "../logs/${name}.log" 2>&1 &
    local pid=$!
    echo $pid > "../pids/${name}.pid"
    
    # Wait for service to start
    local count=0
    while ! check_port $port && [ $count -lt 60 ]; do
        sleep 1
        count=$((count + 1))
    done
    
    if check_port $port; then
        echo -e "${GREEN}$name started successfully (PID: $pid)${NC}"
    else
        echo -e "${RED}Failed to start $name${NC}"
        return 1
    fi
    cd ..
}

# Create directories for logs and pids
mkdir -p logs pids

# Start Inventory Service (Port 8081)
start_service "inventory-service" "inventory-service" 8081

# Start Cart Service (Port 8082)
start_service "cart-service" "cart-service" 8082

# Start Identity Service (Port 8084)
start_service "identity-service" "identity-service" 8084

# Start Order Service (Port 8085)
start_service "order-service" "order-service" 8085

# Start BFF Service (Port 8080)
start_service "bff-service" "bff-service" 8080

echo ""
echo "=========================================="
echo "  Starting Frontend..."
echo "=========================================="

# Start Frontend
cd frontend
npm run dev > "../logs/frontend.log" 2>&1 &
echo $! > "../pids/frontend.pid"
cd ..

sleep 3

echo ""
echo "=========================================="
echo -e "${GREEN}  All services started!${NC}"
echo "=========================================="
echo ""
echo "Services running:"
echo "  - Inventory Service: http://localhost:8081 (Swagger: http://localhost:8081/swagger-ui.html)"
echo "  - Cart Service:      http://localhost:8082 (Swagger: http://localhost:8082/swagger-ui.html)"
echo "  - Identity Service:  http://localhost:8084 (Swagger: http://localhost:8084/swagger-ui.html)"
echo "  - Order Service:     http://localhost:8085 (Swagger: http://localhost:8085/swagger-ui.html)"
echo "  - BFF Service:       http://localhost:8080 (Swagger: http://localhost:8080/swagger-ui.html)"
echo "  - Frontend:          http://localhost:5173"
echo ""
echo "Demo Credentials:"
echo "  - User:  user / user123"
echo "  - Admin: admin / admin123"
echo ""
echo "To stop all services, run: ./stop-services.sh"
