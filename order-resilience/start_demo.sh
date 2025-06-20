#!/bin/bash

# Start YugabyteDB with Docker Compose
echo "Starting YugabyteDB..."
docker-compose up -d yugabytedb

# Wait for YugabyteDB to be ready and create databases
echo "Initializing databases..."
./init-db.sh

# Verify databases were created
echo "Verifying database creation..."
PGPASSWORD=yugabyte psql -h localhost -p 5433 -U yugabyte -c "SELECT datname FROM pg_database WHERE datistemplate = false;"

# Start microservices in the background
echo "Starting microservices..."
BASE_DIR=$(pwd)
cd $BASE_DIR/microservices/customer && ./gradlew quarkusDev -Dquarkus.http.port=8081 &
cd $BASE_DIR/microservices/product && ./gradlew quarkusDev -Dquarkus.http.port=8082 &
cd $BASE_DIR/microservices/promo && ./gradlew quarkusDev -Dquarkus.http.port=8083 &
cd $BASE_DIR/microservices/order && ./gradlew quarkusDev -Dquarkus.http.port=8080 

# Start frontend
echo "Starting frontend..."
cd $BASE_DIR/front && npm start

# Wait for all background processes to complete
wait
