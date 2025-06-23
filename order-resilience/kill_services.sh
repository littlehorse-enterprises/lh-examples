#!/bin/bash

echo "Shutting down Order Resilience Demo services..."

# Stop all services using docker-compose
docker-compose down

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
kill_process_on_port 5433 "YugabyteDB"
kill_process_on_port 7000 "YugabyteDB Admin"

echo "All services have been terminated."
