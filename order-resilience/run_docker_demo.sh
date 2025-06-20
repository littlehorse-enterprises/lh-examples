#!/bin/bash

set -e  # Exit immediately if a command exits with a non-zero status

echo "======================================================="
echo "    Building and starting Order Resilience Demo"
echo "======================================================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "Error: Docker is not running or not accessible"
    echo "Please start Docker and try again"
    exit 1
fi

# Check if native images have been built
SERVICES=("customer" "product" "promo" "order")
NATIVE_MISSING=false

for service in "${SERVICES[@]}"; do
    if ! ls microservices/$service/build/*-runner &> /dev/null; then
        echo "Native image for $service service not found!"
        NATIVE_MISSING=true
    fi
done

if [ "$NATIVE_MISSING" = true ]; then
    echo ""
    echo "ERROR: Some native images are missing!"
    echo "Please run ./build_native_images.sh first to build all required native images."
    echo ""
    read -p "Would you like to run the build script now? (y/n): " choice
    if [[ "$choice" =~ ^[Yy]$ ]]; then
        ./build_native_images.sh
    else
        echo "Build canceled. Please run ./build_native_images.sh manually."
        exit 1
    fi
fi

# Check if frontend has been built
if ! ls front/dist/front/browser/index.html &> /dev/null; then
    echo ""
    echo "ERROR: Frontend build is missing!"
    echo "Please run ./build_frontend.sh first to build the frontend application."
    echo ""
    read -p "Would you like to run the frontend build script now? (y/n): " choice
    if [[ "$choice" =~ ^[Yy]$ ]]; then
        ./build_frontend.sh
    else
        echo "Build canceled. Please run ./build_frontend.sh manually."
        exit 1
    fi
fi

echo "Stopping any existing containers from previous runs (if any)..."
docker-compose down -v --remove-orphans 2>/dev/null || true

echo "Building and starting services..."
echo "This may take a few minutes for the first run..."

# Start all services with Docker Compose
docker-compose up --build "$@"

# Note: If you want to run in detached mode, run with -d flag:
# ./run_docker_demo.sh -d
#
# To view logs when running in detached mode:
# docker-compose logs -f
