#!/bin/bash

# Order Resilience Demo Startup Script
echo "📋 Starting Order Resilience Demo..."
echo "======================================"


# Save current directory
BASE_DIR=$(pwd)

# 1. Kill any potentially running services using the kill_services.sh script
echo "🛑 Killing any existing services..."
$BASE_DIR/kill_services.sh

echo "✅ Services stopped successfully!"
echo "======================================"

echo "Starting yugabyte db container..."
echo "======================================"
docker compose up -d



# 2. Build all components using build_all.sh
echo "🔨 Building all components..."
$BASE_DIR/build_all.sh

echo "✅ All components built successfully!"
echo "======================================"


# 3. Start microservices from built JARs

# Start Customer Service (dependency for Order)
echo "🚀 Starting Customer Service on port 8081..."
cd "$BASE_DIR/microservices/customer"
java -jar build/quarkus-app/quarkus-run.jar > "$BASE_DIR/customer.log" 2>&1 &
echo "  📝 Logs available at $BASE_DIR/customer.log"

# Start Product Service (dependency for Order)
echo "🚀 Starting Product Service on port 8082..."
cd "$BASE_DIR/microservices/product"
java -jar build/quarkus-app/quarkus-run.jar > "$BASE_DIR/product.log" 2>&1 &
echo "  📝 Logs available at $BASE_DIR/product.log"

# Start Promo Service (dependency for Order)
echo "🚀 Starting Promo Service on port 8083..."
cd "$BASE_DIR/microservices/promo"
java -jar build/quarkus-app/quarkus-run.jar > "$BASE_DIR/promo.log" 2>&1 &
echo "  📝 Logs available at $BASE_DIR/promo.log"

# Give the dependency services time to start
echo "⏳ Waiting for services to initialize (10 seconds)..."
sleep 10

# Start Order Service (depends on all others)
echo "🚀 Starting Order Service on port 8080..."
cd "$BASE_DIR/microservices/order"
java -jar build/quarkus-app/quarkus-run.jar > "$BASE_DIR/order.log" 2>&1 &
echo "  📝 Logs available at $BASE_DIR/order.log"

# 4. Start Frontend
echo "🖥️  Starting Frontend on port 4200..."
cd "$BASE_DIR/front"
# Install dependencies if needed
if [ ! -d "node_modules" ]; then
  echo "  📦 Installing frontend dependencies..."
  npm install
fi
# Start Angular app
echo "  🚀 Running ng serve..."
ng serve > "$BASE_DIR/frontend.log" 2>&1 &
echo "  📝 Logs available at $BASE_DIR/frontend.log"

# Return to base directory
cd "$BASE_DIR"

echo "======================================"
echo "🎉 All services started successfully!"
echo "📊 Service URLs:"
echo "   - Order Service:    http://localhost:4210"
echo "   - Customer Service: http://localhost:4211"
echo "   - Product Service:  http://localhost:4212"
echo "   - Promo Service:    http://localhost:4213"
echo "   - Frontend:         http://localhost:4200"
echo ""
echo "💡 Use './kill_services.sh' to stop all services when done"
echo "======================================"
