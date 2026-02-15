#!/bin/bash

# E-commerce Platform Stop Script

echo "=========================================="
echo "  Stopping ShopEase E-commerce Platform"
echo "=========================================="

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

# Kill services by PID files
for pidfile in pids/*.pid; do
    if [ -f "$pidfile" ]; then
        pid=$(cat "$pidfile")
        name=$(basename "$pidfile" .pid)
        if kill -0 "$pid" 2>/dev/null; then
            kill "$pid"
            echo -e "${GREEN}Stopped $name (PID: $pid)${NC}"
        fi
        rm "$pidfile"
    fi
done

# Also kill by port as backup
for port in 8080 8081 8082 8084 8085 8086 5173; do
    pid=$(lsof -ti:$port)
    if [ ! -z "$pid" ]; then
        kill -9 $pid 2>/dev/null
        echo -e "${GREEN}Killed process on port $port${NC}"
    fi
done

echo ""
echo -e "${GREEN}All services stopped!${NC}"
