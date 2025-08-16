#!/bin/bash

# IT Request Demo Next.js Startup Script
echo "Starting LittleHorse IT Request Demo (Next.js)"
echo "================================================"

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "Node.js is not installed or not in PATH"
    echo " Please install Node.js 18 or higher"
    exit 1
fi

# Check if npm is installed
if ! command -v npm &> /dev/null; then
    echo "npm is not installed or not in PATH"
    echo " Please install npm"
    exit 1
fi

# Check Node version
NODE_VERSION=$(node --version | cut -d'v' -f2)
MAJOR_VERSION=$(echo $NODE_VERSION | cut -d'.' -f1)

if [ "$MAJOR_VERSION" -lt 18 ]; then
    echo "Node.js version $NODE_VERSION is not supported"
    echo " Please install Node.js 18 or higher"
    exit 1
fi

echo "Node.js version: $NODE_VERSION"

# Install dependencies if node_modules doesn't exist
if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm install
    if [ $? -ne 0 ]; then
        echo "Failed to install dependencies"
        exit 1
    fi
    echo "Dependencies installed successfully"
fi

# Set environment variables if .env.local doesn't exist
if [ ! -f ".env.local" ]; then
    echo "🔧 Creating .env.local file..."
    cat > .env.local << EOF
LHC_API_HOST=localhost
LHC_API_PORT=2023
LHC_API_PROTOCOL=PLAINTEXT
EOF
    echo "Environment file created"
fi

echo ""
echo "Starting Next.js development server..."
echo " Frontend will be available at: http://localhost:5173"
echo ""
echo "Make sure you have:"
echo " • LittleHorse server running on localhost:2023"
echo " • Java application running (workflow definitions)"
echo ""

# Start the development server
npm run dev
