#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

echo "================================================="
echo "  Building Quarkus Native Images for Microservices"
echo "================================================="

BASE_DIR=$(pwd)

# Build Customer Service
echo "Building Customer Service..."
cd $BASE_DIR/microservices/customer
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true

# Build Product Service
echo "Building Product Service..."
cd $BASE_DIR/microservices/product
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true

# Build Promo Service
echo "Building Promo Service..."
cd $BASE_DIR/microservices/promo
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true

# Build Order Service
echo "Building Order Service..."
cd $BASE_DIR/microservices/order
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true

echo "================================================="
echo "  All Quarkus Native Images Built Successfully!"
echo "================================================="
echo "Now you can run ./run_docker_demo.sh to start the application"
