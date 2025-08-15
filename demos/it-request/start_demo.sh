#!/bin/bash

# IT Request Demo Startup Script
echo "📋 Starting IT Request Demo..."
echo "================================"

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
    echo "================================"
else
    echo "🚀 Running in FULL mode (with LittleHorse standalone server)"
    echo "================================"
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

# Function to wait for service to be ready
wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1
    
    echo "⏳ Waiting for $service_name to be ready..."
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "$url" > /dev/null 2>&1; then
            echo "✅ $service_name is ready!"
            return 0
        fi
        echo "   Attempt $attempt/$max_attempts - $service_name not ready yet..."
        sleep 2
        ((attempt++))
    done
    echo "❌ $service_name failed to start within expected time"
    return 1
}

# Check required ports
echo "🔍 Checking for port conflicts..."
PORTS_OK=true

if [ "$NO_SERVER" = false ]; then
    check_port 2023 "LittleHorse Server" || PORTS_OK=false
    check_port 9092 "Kafka Broker" || PORTS_OK=false
    check_port 8080 "LittleHorse Dashboard" || PORTS_OK=false
fi

check_port 4000 "Backend API" || PORTS_OK=false
check_port 5173 "Frontend Dev Server" || PORTS_OK=false

if [ "$PORTS_OK" = false ]; then
    echo ""
    echo "❌ Port conflicts detected. Please resolve them before continuing."
    echo "   You can use './kill_services.sh' to stop any running demo services."
    exit 1
fi

echo "✅ All ports are available"

# Start Docker services
if [ "$NO_SERVER" = false ]; then
    echo ""
    echo "🐳 Starting LittleHorse server with Docker Compose..."
    docker compose up -d littlehorse
    
    if [ $? -ne 0 ]; then
        echo "❌ Failed to start Docker services"
        exit 1
    fi
    
    # Wait for LittleHorse to be ready
    wait_for_service "http://localhost:8080" "LittleHorse Server"
    if [ $? -ne 0 ]; then
        echo "❌ LittleHorse server failed to start"
        docker compose down
        exit 1
    fi
else
    echo ""
    echo "⚠️  Skipping LittleHorse server startup (--no-server mode)"
    echo "   Make sure you have LittleHorse running on localhost:2023"
fi

# Build the project if needed
echo ""
echo "🔨 Building the project..."
./build_all.sh
if [ $? -ne 0 ]; then
    echo "❌ Build failed"
    if [ "$NO_SERVER" = false ]; then
        docker compose down
    fi
    exit 1
fi

# Start the Java application (task worker and workflow deployer)
echo ""
echo "☕ Starting Java application (Task Worker)..."
cd .
nohup ./gradlew run > java-app.log 2>&1 &
JAVA_PID=$!
echo $JAVA_PID > java-app.pid
echo "✅ Java application started (PID: $JAVA_PID, logs: java-app.log)"

# Wait a bit for the Java app to register tasks
echo "⏳ Waiting for task registration..."
sleep 5

# Start the webapp
echo ""
echo "🌐 Starting webapp..."
cd webapp
nohup npm run dev > ../webapp.log 2>&1 &
WEBAPP_PID=$!
echo $WEBAPP_PID > ../webapp.pid
cd ..
echo "✅ Webapp started (PID: $WEBAPP_PID, logs: webapp.log)"

# Wait for services to be ready
echo ""
echo "⏳ Waiting for services to be fully ready..."
sleep 3

# Final status
echo ""
echo "🎉 IT Request Demo is now running!"
echo "=================================="
echo ""
echo "📍 Access Points:"
echo "   • Frontend:           http://localhost:5173"
echo "   • Backend API:        http://localhost:4000"
if [ "$NO_SERVER" = false ]; then
    echo "   • LittleHorse Dashboard: http://localhost:8080"
fi
echo ""
echo "📋 Demo Usage:"
echo "   1. Open http://localhost:5173 in your browser"
echo "   2. Create a new IT request workflow using the UI"
echo "   3. Monitor workflow progress in the LittleHorse Dashboard"
echo ""
echo "📝 Log Files:"
echo "   • Java App:  java-app.log"
echo "   • Webapp:    webapp.log"
echo ""
echo "🛑 To stop the demo:"
echo "   ./kill_services.sh"
echo ""
