#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

echo "================================================="
echo "      Building Angular Frontend Application"
echo "================================================="

BASE_DIR=$(pwd)

# Build Frontend
echo "Building Frontend Application..."
cd $BASE_DIR/front
npm ci
npm run build -- --configuration production

echo "================================================="
echo "  Frontend Application Built Successfully!"
echo "================================================="
echo "Now you can run ./run_docker_demo.sh to start the application"
