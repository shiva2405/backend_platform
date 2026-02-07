#!/bin/bash

# E-commerce Platform Stop Script

echo "Stopping all services..."

# Function to stop a service
stop_service() {
    local name=$1
    local pidfile="pids/${name}.pid"
    
    if [ -f "$pidfile" ]; then
        local pid=$(cat "$pidfile")
        if kill -0 $pid 2>/dev/null; then
            echo "Stopping $name (PID: $pid)..."
            kill $pid
            rm "$pidfile"
        else
            echo "$name is not running"
            rm "$pidfile"
        fi
    else
        echo "$name pid file not found"
    fi
}

stop_service "inventory-service"
stop_service "cart-service"
stop_service "bff-service"
stop_service "frontend"

# Also try to kill by port
echo "Cleaning up any remaining processes..."
lsof -ti:8080 | xargs kill -9 2>/dev/null
lsof -ti:8081 | xargs kill -9 2>/dev/null
lsof -ti:8082 | xargs kill -9 2>/dev/null
lsof -ti:5173 | xargs kill -9 2>/dev/null

echo "All services stopped."
