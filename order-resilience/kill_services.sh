#!/bin/bash

echo "Shutting down Order Resilience Demo services..."

# Just in case any processes are still running on these ports
function kill_process_on_port() {
  local port=$1
  local service_name=$2
  local pid=$(lsof -ti:$port)
  if [ -n "$pid" ]; then
    echo "Killing $service_name on port $port (PID: $pid)"
    kill -9 $pid
  fi
}

# Check for any lingering processes
kill_process_on_port 8080 "Order service"
kill_process_on_port 8081 "Customer service" 
kill_process_on_port 8082 "Product service"
kill_process_on_port 8083 "Promo service"
kill_process_on_port 4200 "Frontend"

echo "All services have been terminated. run:"
echo "docker compose down"
echo "to stop the yugabyte db container"
