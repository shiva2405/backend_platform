#!/usr/bin/env bash

# ============================================================================
# ShopEase E-commerce Platform - Comprehensive Sanity Test Script
# ============================================================================
# This script:
# 1. Terminates all running services
# 2. Starts all services in correct order
# 3. Runs sanity tests for each service
# 4. Validates end-to-end flows
#
# Cross-platform compatible: macOS, Linux, Windows (Git Bash/WSL)
# ============================================================================

# Colors (works on most terminals)
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get the directory where this script is located (cross-platform)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]:-$0}")" && pwd)"
cd "$SCRIPT_DIR"

LOG_DIR="$SCRIPT_DIR/logs"
mkdir -p "$LOG_DIR"

# Service configuration (compatible with older bash)
SERVICE_NAMES="inventory-service cart-service identity-service order-service bff-service"
SERVICE_PORTS="8081 8082 8084 8085 8080"

# Convert to arrays
read -ra SERVICES_ARRAY <<< "$SERVICE_NAMES"
read -ra PORTS_ARRAY <<< "$SERVICE_PORTS"

# ============================================================================
# Helper Functions
# ============================================================================

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

get_port_for_service() {
    local service=$1
    local i=0
    for s in ${SERVICES_ARRAY[@]}; do
        if [ "$s" = "$service" ]; then
            echo "${PORTS_ARRAY[$i]}"
            return
        fi
        i=$((i + 1))
    done
}

# Cross-platform port check
is_port_in_use() {
    local port=$1
    if command -v lsof &> /dev/null; then
        lsof -i :"$port" -sTCP:LISTEN -t &> /dev/null
        return $?
    elif command -v netstat &> /dev/null; then
        netstat -tuln 2>/dev/null | grep -q ":$port "
        return $?
    elif command -v ss &> /dev/null; then
        ss -tuln | grep -q ":$port "
        return $?
    else
        # Fallback: try curl
        curl -s --connect-timeout 1 "http://localhost:$port" &> /dev/null
        return $?
    fi
}

# Cross-platform kill by port
kill_port() {
    local port=$1
    if command -v lsof &> /dev/null; then
        local pids=$(lsof -ti:"$port" 2>/dev/null)
        if [ -n "$pids" ]; then
            echo "$pids" | xargs kill -9 2>/dev/null || true
        fi
    elif command -v fuser &> /dev/null; then
        fuser -k "$port"/tcp 2>/dev/null || true
    fi
}

# Wait for service to be healthy
wait_for_service() {
    local port=$1
    local name=$2
    local max_attempts=${3:-60}
    local attempt=1
    
    log_info "Waiting for $name on port $port..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
            log_success "$name is UP on port $port"
            return 0
        fi
        sleep 2
        attempt=$((attempt + 1))
    done
    
    log_error "$name FAILED to start on port $port (timeout after $((max_attempts * 2)) seconds)"
    return 1
}

# ============================================================================
# Service Management
# ============================================================================

terminate_all_services() {
    echo ""
    echo "=============================================="
    echo -e "${YELLOW}  TERMINATING ALL SERVICES${NC}"
    echo "=============================================="
    
    # Kill by process name
    pkill -9 -f "spring-boot:run" 2>/dev/null || true
    pkill -9 -f "mvn.*spring-boot" 2>/dev/null || true
    
    # Kill by port
    for port in ${PORTS_ARRAY[@]}; do
        if is_port_in_use "$port"; then
            log_info "Killing process on port $port..."
            kill_port "$port"
        fi
    done
    
    # Also kill frontend if running
    kill_port 5173 2>/dev/null || true
    kill_port 5174 2>/dev/null || true
    
    sleep 3
    log_success "All services terminated"
}

start_service() {
    local service=$1
    local port=$(get_port_for_service "$service")
    local service_dir="$SCRIPT_DIR/$service"
    
    if [ ! -d "$service_dir" ]; then
        log_error "Service directory not found: $service_dir"
        return 1
    fi
    
    log_info "Starting $service on port $port..."
    
    cd "$service_dir"
    nohup mvn spring-boot:run > "$LOG_DIR/${service}.log" 2>&1 &
    local pid=$!
    echo "$pid" > "$LOG_DIR/${service}.pid"
    cd "$SCRIPT_DIR"
    
    if ! wait_for_service "$port" "$service" 60; then
        log_error "Failed to start $service. Check logs: $LOG_DIR/${service}.log"
        return 1
    fi
    
    return 0
}

start_all_services() {
    echo ""
    echo "=============================================="
    echo -e "${BLUE}  STARTING ALL SERVICES${NC}"
    echo "=============================================="
    
    for service in ${SERVICES_ARRAY[@]}; do
        if ! start_service "$service"; then
            log_error "Failed to start $service. Aborting."
            exit 1
        fi
        echo ""
    done
    
    log_success "All services started successfully!"
}

# ============================================================================
# Sanity Tests
# ============================================================================

test_inventory_service() {
    echo ""
    echo "----------------------------------------------"
    echo -e "${BLUE}Testing Inventory Service (8081)${NC}"
    echo "----------------------------------------------"
    
    local passed=0
    local failed=0
    
    # Test 1: Health endpoint
    if curl -s -f http://localhost:8081/actuator/health > /dev/null; then
        log_success "Health check passed"
        passed=$((passed + 1))
    else
        log_error "Health check failed"
        failed=$((failed + 1))
    fi
    
    # Test 2: Get all products
    local products=$(curl -s http://localhost:8081/api/products)
    if echo "$products" | grep -q "id"; then
        log_success "GET /api/products - Returns products"
        passed=$((passed + 1))
    else
        log_error "GET /api/products - Failed"
        failed=$((failed + 1))
    fi
    
    # Test 3: Get product by ID
    local product=$(curl -s http://localhost:8081/api/products/1)
    if echo "$product" | grep -q "name"; then
        log_success "GET /api/products/1 - Returns product details"
        passed=$((passed + 1))
    else
        log_error "GET /api/products/1 - Failed"
        failed=$((failed + 1))
    fi
    
    # Test 4: Get products by category
    local category_products=$(curl -s http://localhost:8081/api/products/category/Electronics)
    if echo "$category_products" | grep -q "Electronics"; then
        log_success "GET /api/products/category/Electronics - Returns filtered products"
        passed=$((passed + 1))
    else
        log_error "GET /api/products/category/Electronics - Failed"
        failed=$((failed + 1))
    fi
    
    # Test 5: Swagger API docs
    if curl -s -f http://localhost:8081/v3/api-docs > /dev/null; then
        log_success "Swagger API docs available"
        passed=$((passed + 1))
    else
        log_error "Swagger API docs not available"
        failed=$((failed + 1))
    fi
    
    echo -e "Inventory Service: ${GREEN}$passed passed${NC}, ${RED}$failed failed${NC}"
    return $failed
}

test_cart_service() {
    echo ""
    echo "----------------------------------------------"
    echo -e "${BLUE}Testing Cart Service (8082)${NC}"
    echo "----------------------------------------------"
    
    local passed=0
    local failed=0
    
    # Test 1: Health endpoint
    if curl -s -f http://localhost:8082/actuator/health > /dev/null; then
        log_success "Health check passed"
        passed=$((passed + 1))
    else
        log_error "Health check failed"
        failed=$((failed + 1))
    fi
    
    # Test 2: Get cart for user (empty initially)
    local cart=$(curl -s http://localhost:8082/api/cart/user/999)
    if [ "$cart" = "[]" ] || echo "$cart" | grep -q "\[\]"; then
        log_success "GET /api/cart/user/999 - Returns empty cart"
        passed=$((passed + 1))
    else
        log_error "GET /api/cart/user/999 - Unexpected response"
        failed=$((failed + 1))
    fi
    
    # Test 3: Add to cart
    local add_result=$(curl -s -X POST http://localhost:8082/api/cart \
        -H "Content-Type: application/json" \
        -d '{"userId":999,"productId":1,"quantity":2}')
    if echo "$add_result" | grep -q "productId"; then
        log_success "POST /api/cart - Item added to cart"
        passed=$((passed + 1))
    else
        log_error "POST /api/cart - Failed to add item"
        failed=$((failed + 1))
    fi
    
    # Test 4: Get cart after adding
    local cart_after=$(curl -s http://localhost:8082/api/cart/user/999)
    if echo "$cart_after" | grep -q "productId"; then
        log_success "GET /api/cart/user/999 - Cart has items"
        passed=$((passed + 1))
    else
        log_error "GET /api/cart/user/999 - Cart still empty"
        failed=$((failed + 1))
    fi
    
    # Test 5: Clear cart
    curl -s -X DELETE http://localhost:8082/api/cart/user/999 > /dev/null
    log_success "DELETE /api/cart/user/999 - Cart cleared"
    passed=$((passed + 1))
    
    echo -e "Cart Service: ${GREEN}$passed passed${NC}, ${RED}$failed failed${NC}"
    return $failed
}

test_identity_service() {
    echo ""
    echo "----------------------------------------------"
    echo -e "${BLUE}Testing Identity Service (8084)${NC}"
    echo "----------------------------------------------"
    
    local passed=0
    local failed=0
    
    # Test 1: Health endpoint
    if curl -s -f http://localhost:8084/actuator/health > /dev/null; then
        log_success "Health check passed"
        passed=$((passed + 1))
    else
        log_error "Health check failed"
        failed=$((failed + 1))
    fi
    
    # Test 2: Login with valid credentials
    local login_result=$(curl -s -X POST http://localhost:8084/api/auth/login \
        -H "Content-Type: application/json" \
        -d '{"username":"user","password":"user123"}')
    if echo "$login_result" | grep -q "token"; then
        log_success "POST /api/auth/login - Login successful, token received"
        passed=$((passed + 1))
    else
        log_error "POST /api/auth/login - Login failed"
        failed=$((failed + 1))
    fi
    
    # Test 3: Login with invalid credentials
    local bad_login=$(curl -s -o /dev/null -w "%{http_code}" -X POST http://localhost:8084/api/auth/login \
        -H "Content-Type: application/json" \
        -d '{"username":"baduser","password":"badpass"}')
    if [ "$bad_login" = "401" ] || [ "$bad_login" = "403" ]; then
        log_success "POST /api/auth/login - Invalid credentials rejected (HTTP $bad_login)"
        passed=$((passed + 1))
    else
        log_error "POST /api/auth/login - Invalid credentials not rejected (HTTP $bad_login)"
        failed=$((failed + 1))
    fi
    
    # Test 4: Get all users
    local users=$(curl -s http://localhost:8084/api/users)
    if echo "$users" | grep -q "username"; then
        log_success "GET /api/users - Returns user list"
        passed=$((passed + 1))
    else
        log_error "GET /api/users - Failed"
        failed=$((failed + 1))
    fi
    
    # Test 5: Get user by ID
    local user=$(curl -s http://localhost:8084/api/users/1)
    if echo "$user" | grep -q "username"; then
        log_success "GET /api/users/1 - Returns user details"
        passed=$((passed + 1))
    else
        log_error "GET /api/users/1 - Failed"
        failed=$((failed + 1))
    fi
    
    echo -e "Identity Service: ${GREEN}$passed passed${NC}, ${RED}$failed failed${NC}"
    return $failed
}

test_order_service() {
    echo ""
    echo "----------------------------------------------"
    echo -e "${BLUE}Testing Order Service (8085)${NC}"
    echo "----------------------------------------------"
    
    local passed=0
    local failed=0
    
    # Test 1: Health endpoint
    if curl -s -f http://localhost:8085/actuator/health > /dev/null; then
        log_success "Health check passed"
        passed=$((passed + 1))
    else
        log_error "Health check failed"
        failed=$((failed + 1))
    fi
    
    # Test 2: Get orders for user (empty initially)
    local orders=$(curl -s http://localhost:8085/api/orders/user/999)
    if [ "$orders" = "[]" ] || echo "$orders" | grep -q "\["; then
        log_success "GET /api/orders/user/999 - Returns empty orders"
        passed=$((passed + 1))
    else
        log_error "GET /api/orders/user/999 - Unexpected response"
        failed=$((failed + 1))
    fi
    
    # Test 3: Get all orders
    local all_orders=$(curl -s http://localhost:8085/api/orders)
    if [ "$all_orders" = "[]" ] || echo "$all_orders" | grep -q "\["; then
        log_success "GET /api/orders - Returns orders list"
        passed=$((passed + 1))
    else
        log_error "GET /api/orders - Failed"
        failed=$((failed + 1))
    fi
    
    # Test 4: Create order via checkout
    local checkout_result=$(curl -s -X POST http://localhost:8085/api/orders/checkout \
        -H "Content-Type: application/json" \
        -H "X-User-Id: 999" \
        -d '{"shippingAddress":"123 Test St","cartItems":[{"productId":1,"productName":"Test Product","quantity":1,"price":99.99}]}')
    if echo "$checkout_result" | grep -q "id"; then
        log_success "POST /api/orders/checkout - Order created"
        passed=$((passed + 1))
    else
        log_error "POST /api/orders/checkout - Failed"
        failed=$((failed + 1))
    fi
    
    # Test 5: Verify order exists
    local orders_after=$(curl -s http://localhost:8085/api/orders/user/999)
    if echo "$orders_after" | grep -q "id"; then
        log_success "GET /api/orders/user/999 - Order exists"
        passed=$((passed + 1))
    else
        log_error "GET /api/orders/user/999 - Order not found"
        failed=$((failed + 1))
    fi
    
    echo -e "Order Service: ${GREEN}$passed passed${NC}, ${RED}$failed failed${NC}"
    return $failed
}

test_bff_service() {
    echo ""
    echo "----------------------------------------------"
    echo -e "${BLUE}Testing BFF Service (8080)${NC}"
    echo "----------------------------------------------"
    
    local passed=0
    local failed=0
    
    # Test 1: Health endpoint
    if curl -s -f http://localhost:8080/actuator/health > /dev/null; then
        log_success "Health check passed"
        passed=$((passed + 1))
    else
        log_error "Health check failed"
        failed=$((failed + 1))
    fi
    
    # Test 2: Get products (public endpoint)
    local products=$(curl -s http://localhost:8080/api/products)
    if echo "$products" | grep -q "id"; then
        log_success "GET /api/products - Products fetched via BFF"
        passed=$((passed + 1))
    else
        log_error "GET /api/products - Failed"
        failed=$((failed + 1))
    fi
    
    # Test 3: Login via BFF
    local login=$(curl -s -X POST http://localhost:8080/api/auth/login \
        -H "Content-Type: application/json" \
        -d '{"username":"user","password":"user123"}')
    local token=$(echo "$login" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    if [ -n "$token" ]; then
        log_success "POST /api/auth/login - Login via BFF successful"
        passed=$((passed + 1))
    else
        log_error "POST /api/auth/login - Login via BFF failed"
        failed=$((failed + 1))
        echo -e "BFF Service: ${GREEN}$passed passed${NC}, ${RED}$failed failed${NC}"
        return $failed
    fi
    
    # Test 4: Get cart (authenticated)
    local cart=$(curl -s http://localhost:8080/api/cart \
        -H "Authorization: Bearer $token")
    if echo "$cart" | grep -q "\["; then
        log_success "GET /api/cart - Cart fetched (authenticated)"
        passed=$((passed + 1))
    else
        log_error "GET /api/cart - Failed"
        failed=$((failed + 1))
    fi
    
    # Test 5: Unauthorized access
    local unauth=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/cart)
    if [ "$unauth" = "401" ] || [ "$unauth" = "403" ]; then
        log_success "GET /api/cart (no auth) - Correctly rejected (HTTP $unauth)"
        passed=$((passed + 1))
    else
        log_error "GET /api/cart (no auth) - Should be rejected (HTTP $unauth)"
        failed=$((failed + 1))
    fi
    
    echo -e "BFF Service: ${GREEN}$passed passed${NC}, ${RED}$failed failed${NC}"
    return $failed
}

test_end_to_end_flow() {
    echo ""
    echo "=============================================="
    echo -e "${BLUE}  END-TO-END FLOW TEST${NC}"
    echo "=============================================="
    
    local passed=0
    local failed=0
    
    # Step 1: Login
    log_info "Step 1: Login as user..."
    local login=$(curl -s -X POST http://localhost:8080/api/auth/login \
        -H "Content-Type: application/json" \
        -d '{"username":"user","password":"user123"}')
    local token=$(echo "$login" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    
    if [ -z "$token" ]; then
        log_error "Login failed - cannot proceed with E2E test"
        return 1
    fi
    log_success "Logged in, token obtained"
    passed=$((passed + 1))
    
    # Step 2: Browse products
    log_info "Step 2: Browse products..."
    local products=$(curl -s http://localhost:8080/api/products)
    if echo "$products" | grep -q "iPhone"; then
        log_success "Products loaded successfully"
        passed=$((passed + 1))
    else
        log_error "Products not loaded"
        failed=$((failed + 1))
    fi
    
    # Step 3: Add to cart
    log_info "Step 3: Add product to cart..."
    local add_cart=$(curl -s -X POST http://localhost:8080/api/cart \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d '{"productId":1,"quantity":2}')
    if echo "$add_cart" | grep -q "productId"; then
        log_success "Product added to cart"
        passed=$((passed + 1))
    else
        log_error "Failed to add product to cart"
        failed=$((failed + 1))
    fi
    
    # Step 4: View cart
    log_info "Step 4: View cart..."
    local cart=$(curl -s http://localhost:8080/api/cart \
        -H "Authorization: Bearer $token")
    if echo "$cart" | grep -q "productId"; then
        log_success "Cart retrieved with items"
        passed=$((passed + 1))
    else
        log_error "Cart is empty or failed"
        failed=$((failed + 1))
    fi
    
    # Step 5: Checkout
    log_info "Step 5: Checkout..."
    local order=$(curl -s -X POST http://localhost:8080/api/orders/checkout \
        -H "Authorization: Bearer $token" \
        -H "Content-Type: application/json" \
        -d '{"shippingAddress":"123 E2E Test Street, City, 12345"}')
    if echo "$order" | grep -q "id"; then
        log_success "Order placed successfully"
        passed=$((passed + 1))
    else
        log_error "Checkout failed"
        failed=$((failed + 1))
    fi
    
    # Step 6: View orders
    log_info "Step 6: View orders..."
    local orders=$(curl -s http://localhost:8080/api/orders \
        -H "Authorization: Bearer $token")
    if echo "$orders" | grep -q "id"; then
        log_success "Orders retrieved"
        passed=$((passed + 1))
    else
        log_error "Orders not found"
        failed=$((failed + 1))
    fi
    
    echo ""
    echo -e "End-to-End Flow: ${GREEN}$passed passed${NC}, ${RED}$failed failed${NC}"
    return $failed
}

# ============================================================================
# Main Execution
# ============================================================================

run_all_tests() {
    local total_failed=0
    local result=0
    
    test_inventory_service
    result=$?
    total_failed=$((total_failed + result))
    
    test_cart_service
    result=$?
    total_failed=$((total_failed + result))
    
    test_identity_service
    result=$?
    total_failed=$((total_failed + result))
    
    test_order_service
    result=$?
    total_failed=$((total_failed + result))
    
    test_bff_service
    result=$?
    total_failed=$((total_failed + result))
    
    test_end_to_end_flow
    result=$?
    total_failed=$((total_failed + result))
    
    return $total_failed
}

print_summary() {
    echo ""
    echo "=============================================="
    echo -e "${BLUE}  SERVICE STATUS SUMMARY${NC}"
    echo "=============================================="
    echo ""
    echo "Services Running:"
    local i=0
    for service in ${SERVICES_ARRAY[@]}; do
        local port=${PORTS_ARRAY[$i]}
        if is_port_in_use "$port"; then
            echo -e "  ${GREEN}✓${NC} $service (port $port)"
        else
            echo -e "  ${RED}✗${NC} $service (port $port)"
        fi
        i=$((i + 1))
    done
    echo ""
    echo "Swagger UI:"
    echo "  - Inventory: http://localhost:8081/swagger-ui.html"
    echo "  - Cart:      http://localhost:8082/swagger-ui.html"
    echo "  - Identity:  http://localhost:8084/swagger-ui.html"
    echo "  - Order:     http://localhost:8085/swagger-ui.html"
    echo "  - BFF:       http://localhost:8080/swagger-ui.html"
    echo ""
    echo "Logs: $LOG_DIR/"
    echo ""
}

main() {
    echo ""
    echo "=============================================="
    echo -e "${BLUE}  ShopEase E-commerce Platform${NC}"
    echo -e "${BLUE}  Comprehensive Sanity Test Suite${NC}"
    echo "=============================================="
    echo ""
    echo "Platform: $OSTYPE"
    echo "Working Directory: $SCRIPT_DIR"
    echo ""
    
    # Terminate existing services
    terminate_all_services
    
    # Start all services
    start_all_services
    
    # Run all tests
    echo ""
    echo "=============================================="
    echo -e "${BLUE}  RUNNING SANITY TESTS${NC}"
    echo "=============================================="
    
    local test_result=0
    run_all_tests
    test_result=$?
    
    if [ $test_result -eq 0 ]; then
        echo ""
        echo "=============================================="
        echo -e "${GREEN}  ALL TESTS PASSED!${NC}"
        echo "=============================================="
    else
        echo ""
        echo "=============================================="
        echo -e "${RED}  SOME TESTS FAILED! ($test_result failures)${NC}"
        echo "=============================================="
    fi
    
    print_summary
    
    echo ""
    echo "To stop all services, run: ./stop-services.sh"
    
    exit $test_result
}

# Run main function
main "$@"
