# Order Resilience Demo

This demo showcases a microservices-based e-commerce application with resilience patterns implemented to handle various failure scenarios.

## System Architecture

The application consists of the following components:

- **Order Service** (Port 8080): Handles order processing and coordinates with other services
- **Customer Service** (Port 8081): Manages customer data and accounts
- **Product Service** (Port 8082): Manages product catalog and inventory
- **Promo Service** (Port 8083): Handles promotional offers and coupon codes
- **Frontend** (Port 4200): Angular-based user interface
- **Database** (Port 5433): YugabyteDB (PostgreSQL-compatible distributed database)

## Prerequisites

- Java 17 or higher
- Docker and Docker Compose
- Node.js (v14+) and npm
- Angular CLI (`npm install -g @angular/cli`)

## Running the Demo

### Option 1: Automated Setup (Recommended)

Use the provided script to build and start all services:

```bash
# Step 1: Build all components (microservices and frontend)
./build_all.sh

# Step 2: Start all services (database, microservices, and frontend)
./start_demo.sh
```

### Option 2: Manual Setup

If you prefer to run the services manually:

1. **Start the Database**:
   ```bash
   docker-compose up -d yugabytedb db-init
   ```
   Wait about 15 seconds for the database to initialize.

2. **Build and Start the Microservices**:
   ```bash
   # Build and start Customer Service
   cd microservices/customer
   ./gradlew build -x test
   java -jar build/quarkus-app/quarkus-run.jar
   
   # Build and start Product Service
   cd ../product
   ./gradlew build -x test
   java -jar build/quarkus-app/quarkus-run.jar
   
   # Build and start Promo Service
   cd ../promo
   ./gradlew build -x test
   java -jar build/quarkus-app/quarkus-run.jar
   
   # Build and start Order Service
   cd ../order
   ./gradlew build -x test
   java -jar build/quarkus-app/quarkus-run.jar
   ```

3. **Build and Start the Frontend**:
   ```bash
   cd front
   npm install
   ng serve
   ```

## Accessing the Demo

Once all services are running, you can access:

- Frontend UI: http://localhost:4200
- Order Service API: http://localhost:8080
- Customer Service API: http://localhost:8081
- Product Service API: http://localhost:8082
- Promo Service API: http://localhost:8083
- YugabyteDB Admin UI: http://localhost:7000

## Testing Resilience Features

The demo includes various failure scenarios you can test:

1. **Service Unavailability**:
   - Stop a service (e.g., Product Service) and observe how the system handles the failure
   - Restart the service to see automatic recovery

2. **Database Connection Issues**:
   - The system uses connection pooling and retry mechanisms

3. **High Latency**:
   - The services implement timeout and circuit breaker patterns

## Monitoring

- Check the log files for each service in the root directory:
  - `order.log`
  - `customer.log`
  - `product.log`
  - `promo.log`
  - `frontend.log`

## Shutting Down the Demo

To stop all services:

```bash
./kill_services.sh
```

This script will:
- Shut down all Docker containers
- Kill any processes running on the service ports

## Troubleshooting

- **First Request Delay**: The first request to each microservice might take longer due to JVM warm-up and database connection establishment. Subsequent requests should be faster.
- **Connection Issues**: Ensure no other services are running on the required ports (8080-8083, 4200, 5433, 7000).
- **Build Failures**: Check individual service logs for detailed error messages.
