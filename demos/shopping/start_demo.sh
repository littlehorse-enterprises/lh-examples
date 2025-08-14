#!/bin/bash

# Order Resilience Demo Startup Script
echo "📋 Starting Order Resilience Demo..."
echo "======================================"

# Parse command line arguments
NO_SERVER=false
for arg in "$@"; do
    case $arg in
        --no-server)
            NO_SERVER=true
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [OPTIONS]"
            echo "Options:"
            echo "  --no-server    Skip LittleHorse standalone server (use external LH server)"
            echo "  -h, --help     Show this help message"
            exit 0
            ;;
        *)
            echo "Unknown option: $arg"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

if [ "$NO_SERVER" = true ]; then
    echo "🔧 Running in NO-SERVER mode (external LittleHorse server required)"
    echo "======================================"
else
    echo "🚀 Running in FULL mode (with LittleHorse standalone server)"
    echo "======================================"
fi

# Function to check if a port is in use
check_port() {
    local port=$1
    local service=$2
    local pid=$(lsof -ti:$port 2>/dev/null)
    if [ ! -z "$pid" ]; then
        echo "❌ Port $port is already in use by $service (PID: $pid)"
        echo "   Please stop the process first."
        return 1
    fi
    return 0
}

# Check for conflicting services on LittleHorse ports (only if not in no-server mode)
if [ "$NO_SERVER" = false ]; then
    echo "🔍 Checking for conflicting services on LittleHorse ports..."
    conflict=0

    if ! check_port 2023 "LittleHorse Server"; then
        conflict=1
    fi


    if [ $conflict -eq 1 ]; then
        echo "======================================"
        echo "❌ Found conflicting services running on required ports!"
        echo "💡 Please stop the conflicting processes and try again."
        echo "🔄 You can also run './kill_services.sh' to stop any related services."
        exit 1
    fi

    echo "✅ All required ports are available!"
    echo "======================================"
else
    echo "⚠️  Skipping port checks - assuming external LittleHorse server"
    echo "   Make sure LittleHorse server is running on localhost:2023"
    echo "   and Kafka on localhost:9092"
    echo "======================================"
fi

# Save current directory
BASE_DIR=$(pwd)

# 1. Kill any potentially running services using the kill_services.sh script
echo "🛑 Killing any existing services..."
$BASE_DIR/kill_services.sh

echo "✅ Services stopped successfully!"
echo "======================================"

echo "Starting containers..."
echo "======================================"
if [ "$NO_SERVER" = true ]; then
    # Start only YugabyteDB and db-init when in no-server mode
    docker compose up -d yugabytedb db-init
else
    # Start all containers including LittleHorse
    docker compose up -d
fi



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
echo "   - Frontend:         http://localhost:4200"
if [ "$NO_SERVER" = false ]; then
    echo "   - LittleHorse UI:   http://localhost:8080"
fi
echo ""
echo "   - Order Service:    http://localhost:4210/api/orders/client/1"
echo "   - Customer Service: http://localhost:4211/api/customers"
echo "   - Product Service:  http://localhost:4212/api/products"
echo "   - Promo Service:    http://localhost:4213/api/coupons/client/1"
echo ""
if [ "$NO_SERVER" = true ]; then
    echo "⚠️  NOTE: Running in NO-SERVER mode"
    echo "   Make sure your external LittleHorse server is running on localhost:2023"
fi
echo "💡 Use './kill_services.sh' to stop all services when done"
echo "======================================"
