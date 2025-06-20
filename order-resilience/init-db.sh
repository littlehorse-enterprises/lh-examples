#!/bin/bash

# Wait for YugabyteDB to be ready
echo "Waiting for YugabyteDB to be ready..."
sleep 15

# Create databases directly with psql
echo "Creating databases in YugabyteDB..."
PGPASSWORD=yugabyte psql -h localhost -p 5433 -U yugabyte -c "CREATE DATABASE customerdb;"
PGPASSWORD=yugabyte psql -h localhost -p 5433 -U yugabyte -c "CREATE DATABASE orderdb;"
PGPASSWORD=yugabyte psql -h localhost -p 5433 -U yugabyte -c "CREATE DATABASE productdb;"
PGPASSWORD=yugabyte psql -h localhost -p 5433 -U yugabyte -c "CREATE DATABASE coupondb;"

echo "Databases created successfully"
