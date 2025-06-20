#!/bin/bash

set -e  # Exit immediately if a command exits with a non-zero status

echo "================================================="
echo "  Order Resilience Demo - Build and Run All"
echo "================================================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "Error: Docker is not running or not accessible"
    echo "Please start Docker and try again"
    exit 1
fi

# Build the native images for microservices
echo "Step 1/3: Building Quarkus native images for microservices..."
./build_native_images.sh

# Build the frontend
echo "Step 2/3: Building Angular frontend application..."
./build_frontend.sh

# Run the application with docker-compose
echo "Step 3/3: Running the application with docker-compose..."
./run_docker_demo.sh "$@"

echo "================================================="
echo "  Order Resilience Demo - Complete Setup Done"
echo "================================================="
