#!/bin/bash

# This script builds all components for the IT Request Demo
# Run this script before starting the demo services

echo "Building components for IT Request Demo..."

# Save current directory
BASE_DIR=$(pwd)

# Check if we have Java and Gradle
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed or not in PATH"
    echo "   Please install Java 17 or higher"
    exit 1
fi

echo "Building Java application..."
./gradlew clean build -x test || { echo "❌ Java application build failed"; exit 1; }
echo "✅ Java application built successfully"

# Check if webapp directory exists
if [ ! -d "webapp" ]; then
    echo "❌ webapp directory not found"
    exit 1
fi

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed or not in PATH"
    echo "   Please install Node.js 18 or higher"
    exit 1
fi

# Check if npm is installed
if ! command -v npm &> /dev/null; then
    echo "❌ npm is not installed or not in PATH"
    echo "   Please install npm"
    exit 1
fi

echo "Building webapp..."
cd "$BASE_DIR/webapp"

# Install dependencies if node_modules doesn't exist
if [ ! -d "node_modules" ]; then
    echo "Installing webapp dependencies..."
    npm install || { echo "❌ Webapp dependency installation failed"; exit 1; }
fi

# Build the webapp
npm run build || { echo "❌ Webapp build failed"; exit 1; }
echo "✅ Webapp built successfully"

echo "All components built successfully! 🎉"
echo "You can now run './start_demo.sh' to start the application"

cd "$BASE_DIR"
