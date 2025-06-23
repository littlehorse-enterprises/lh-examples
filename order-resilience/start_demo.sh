#!/bin/bash

# Check if build_all.sh has been run
if [ ! -d "microservices/customer/build/quarkus-app" ] || [ ! -d "front/dist" ]; then
  echo "Building apps"
  echo "Please run './build_all.sh' first to build all the components."
  
  read -p "Do you want to run build_all.sh now? (y/n) " -n 1 -r
  echo
  if [[ $REPLY =~ ^[Yy]$ ]]; then
    ./build_all.sh
  else
    echo "Exiting. Please run './build_all.sh' before starting the demo."
    exit 1
  fi
fi

# Function to kill processes on a specific port
kill_process_on_port() {
  local port=$1
  local pid=$(lsof -ti:$port)
  if [ -n "$pid" ]; then
    echo "Killing process on port $port (PID: $pid)"
    kill -9 $pid
  else
    echo "No process found running on port $port"
  fi
}

# Kill existing processes on the ports we'll use
echo "Cleaning up existing processes..."
kill_process_on_port 5433  # YugabyteDB
kill_process_on_port 8080  # order service
kill_process_on_port 8081  # customer service
kill_process_on_port 8082  # product service
kill_process_on_port 8083  # promo service
kill_process_on_port 4200  # frontend

# Clean up any existing Docker containers
echo "Stopping any existing Docker containers..."
docker-compose down

# Start the services using Docker Compose
docker-compose up 

echo "Order Resilience Demo is starting..."
echo "You can access the application at http://localhost:4200"
echo "API endpoints are available at:"
echo "- Orders:    http://localhost:8080"
echo "- Customer:  http://localhost:8081"
echo "- Products:  http://localhost:8082"
echo "- Promos:    http://localhost:8083"
echo "- YugabytDB: http://localhost:7000"

echo ""
echo "To view logs from all services: docker-compose logs -f"
echo "To stop the demo: ./kill_services.sh"
