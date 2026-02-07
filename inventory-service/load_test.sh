#!/bin/bash
echo "Starting load test..."

# Start app in bg
nohup mvn spring-boot:run > /tmp/load_app.log 2>&1 &
APP_PID=$!
sleep 30

# Simple load test with ab (1000 requests, 10 concurrent for GET products)
ab -n 1000 -c 10 -g /tmp/ab_results.tsv http://localhost:8080/api/products > /tmp/ab_output.txt 2>&1

# Extract SLA metrics
RPS=$(grep "Requests per second" /tmp/ab_output.txt | awk '{print $4}')
AVG_LATENCY=$(grep "Time per request" /tmp/ab_output.txt | head -1 | awk '{print $4}')
P95_LATENCY=$(awk 'NR>1 {print $3}' /tmp/ab_results.tsv | sort -n | awk 'NR==950 {print $1/1000}')  # rough p95 in ms

echo "Load Test Results (GET /api/products):"
echo "Requests per second: $RPS"
echo "Average latency: $AVG_LATENCY ms"
echo "Approx 95th percentile latency: ${P95_LATENCY} ms"

kill $APP_PID
