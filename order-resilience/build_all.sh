#!/bin/bash

# This script builds all the microservices for the Order Resilience Demo
# Run this script before starting the docker-compose services

echo "Building microservices for Order Resilience Demo..."

# Save current directory
BASE_DIR=$(pwd)

echo "Building customer service..."
cd "$BASE_DIR/microservices/customer" && ./gradlew build -x test || { echo "Customer service build failed"; exit 1; }
echo "✅ Customer service built successfully"

echo "Building product service..."
cd "$BASE_DIR/microservices/product" && ./gradlew build -x test || { echo "Product service build failed"; exit 1; }
echo "✅ Product service built successfully"

echo "Building promo service..."
cd "$BASE_DIR/microservices/promo" && ./gradlew build -x test || { echo "Promo service build failed"; exit 1; }
echo "✅ Promo service built successfully"

echo "Building order service..."
cd "$BASE_DIR/microservices/order" && ./gradlew build -x test || { echo "Order service build failed"; exit 1; }
echo "✅ Order service built successfully"

echo "Building frontend application..."
cd "$BASE_DIR/front" && npm install && ng build || { echo "Frontend build failed"; exit 1; }
echo "✅ Frontend built successfully"

echo "All components built successfully! 🎉"
echo "You can now run './start_demo.sh' to start the application using Docker Compose"

cd "$BASE_DIR"
