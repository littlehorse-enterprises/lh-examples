#!/bin/bash
echo "Shutting down Order Resilience Demo services and db..."

docker compose down

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
kill_process_on_port 4210 "Order service"
kill_process_on_port 4211 "Customer service" 
kill_process_on_port 4212 "Product service"
kill_process_on_port 4213 "Promo service"
kill_process_on_port 4200 "Frontend"

echo "All services and db have been terminated."
