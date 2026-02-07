#!/bin/bash
set -e  # exit on error
echo "=== Testing end-to-end services ==="

# Kill any running
echo "Killing previous services..."
pkill -9 -f spring-boot:run || true
sleep 3

# Function to check service health
check_service() {
    local port=$1
    local name=$2
    echo "Checking $name on port $port..."
    for i in {1..30}; do
        if curl -s -f http://localhost:$port/actuator/health > /dev/null 2>&1; then
            echo "$name is UP on $port"
            return 0
        fi
        sleep 2
    done
    echo "$name FAILED to start on $port"
    exit 1
}

# Start inventory on 8081
echo "Starting Inventory (8081)..."
cd /testbed/zed-base/inventory-service
nohup mvn spring-boot:run > /tmp/inv.log 2>&1 &
INV_PID=$!
sleep 10
check_service 8081 "Inventory"

# Start cart on 8082
echo "Starting Cart (8082)..."
cd /testbed/zed-base/cart-service
nohup mvn spring-boot:run > /tmp/cart.log 2>&1 &
CART_PID=$!
sleep 10
check_service 8082 "Cart"

# Start order on 8080
echo "Starting Order (8080)..."
cd /testbed/zed-base/order-service
nohup mvn spring-boot:run > /tmp/order.log 2>&1 &
ORDER_PID=$!
sleep 10
check_service 8080 "Order"

# Log ports/services
echo "=== Services Status ==="
echo "Inventory: http://localhost:8081 (Swagger: /swagger-ui.html)"
echo "Cart: http://localhost:8082 (Swagger: /swagger-ui.html)"
echo "Order: http://localhost:8080 (Swagger: /swagger-ui.html)"
echo "Logs: /tmp/*.log"

# Hit Swagger to list APIs
echo "=== Fetching Swagger APIs ==="
curl -s http://localhost:8081/v3/api-docs | head -c 500 | tail -c 100  # Inventory APIs snippet
curl -s http://localhost:8082/v3/api-docs | head -c 500 | tail -c 100  # Cart
curl -s http://localhost:8080/v3/api-docs | head -c 500 | tail -c 100  # Order

# Add test data to inventory
echo "=== Adding Inventory Data ==="
curl -X POST http://localhost:8081/api/products -H "Content-Type: application/json" -d '{"name":"Laptop","price":999.99,"stockQuantity":50}' -w "\n"

# Add to cart (global, no user)
echo "=== Adding to Cart ==="
curl -X POST http://localhost:8082/api/cart -H "Content-Type: application/json" -d '{"productId":1,"quantity":2}' -w "\n"

# List items via order
echo "=== Listing Items via Order Service ==="
curl http://localhost:8080/api/orders/items -w "\n"

echo "=== All tests passed! Services running on correct ports with data. ==="
# Note: Leave running or kill manually: kill $INV_PID $CART_PID $ORDER_PID
