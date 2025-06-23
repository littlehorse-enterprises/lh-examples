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

## LittleHorse Dependency

This demo depends on the [LittleHorse repository](https://github.com/littlehorse-enterprises/littlehorse) using the `input-variables-and-output-topic` branch. Before running the demo, you need to:

1. Clone and checkout the required branch:
   ```bash
   git clone https://github.com/littlehorse-enterprises/littlehorse
   cd littlehorse
   git checkout input-variables-and-output-topic
   ```

2. Start the Kafka cluster:
   ```bash
   # Start the Kafka cluster using compose
   ./local-dev/setup.sh
   ```

3. Start the LittleHorse server with the required fixes in the branch:
   ```bash
   ./local-dev/do-server.sh
   ```

## Running the Demo

### Option 1: Automated Setup (Recommended)

Use the provided script to build and start all services:

```bash
# In a new terminal, start all services (database, microservices, and frontend)
./start_demo.sh
```

After starting the services, you can access the frontend at: http://localhost:4200

Once you're done with the demo, you can shut down all services with:

```bash
# Stop all services
./kill_services.sh
```

## API Usage Examples

### Increasing Product Stock

To increase the stock for a specific product, use the following curl command:

```bash
curl --location 'localhost:8082/api/products/stock' \
--header 'Content-Type: application/json' \
--data '{
   "productId": 3,
   "quantity": 11
}'
```

This will add 11 units to the stock of the product with ID 3.