#!/bin/bash
echo "Shutting down IT Request Demo services..."

# Function to kill process by PID file
kill_by_pidfile() {
    local pidfile=$1
    local service_name=$2
    
    if [ -f "$pidfile" ]; then
        local pid=$(cat "$pidfile")
        if kill -0 "$pid" 2>/dev/null; then
            echo "Stopping $service_name (PID: $pid)"
            kill -TERM "$pid" 2>/dev/null
            sleep 2
            if kill -0 "$pid" 2>/dev/null; then
                echo "Force killing $service_name (PID: $pid)"
                kill -9 "$pid" 2>/dev/null
            fi
        fi
        rm -f "$pidfile"
    fi
}

# Function to kill process on specific port
kill_process_on_port() {
    local port=$1
    local service_name=$2
    local pid=$(lsof -ti:$port 2>/dev/null)
    if [ -n "$pid" ]; then
        echo "Killing $service_name on port $port (PID: $pid)"
        kill -TERM $pid 2>/dev/null
        sleep 2
        # Force kill if still running
        if kill -0 $pid 2>/dev/null; then
            kill -9 $pid 2>/dev/null
        fi
    fi
}

# Stop services using PID files
kill_by_pidfile "java-app.pid" "Java Application"
kill_by_pidfile "webapp.pid" "Webapp"

# Stop Docker services
echo "Stopping Docker services..."
docker compose down

# Kill any lingering processes on known ports
echo "Checking for lingering processes..."
kill_process_on_port 4000 "Backend API"
kill_process_on_port 5173 "Frontend Dev Server"
kill_process_on_port 2023 "LittleHorse Server"
kill_process_on_port 9092 "Kafka Broker"
kill_process_on_port 8080 "LittleHorse Dashboard"

# Clean up log files
echo "Cleaning up log files..."
rm -f java-app.log
rm -f webapp.log

echo "All services have been terminated and cleaned up."
