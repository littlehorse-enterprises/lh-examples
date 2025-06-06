# Order Service

This service manages order processing for the order resilience demo. It allows clients to create orders and retrieve order history.

## Features

- Create new orders with multiple order lines
- Retrieve orders by client ID
- Simple order management without complex validation
- Bidirectional relationships between orders and order lines

## API Endpoints

### Create a new order
```
POST /api/orders
```
Request body:
```json
{
  "clientId": 1,
  "orderLines": [
    {
      "productId": 101,
      "quantity": 2,
      "unitPrice": 29.99
    },
    {
      "productId": 102,
      "quantity": 1,
      "unitPrice": 49.99
    }
  ]
}
```

### Get orders by client ID
```
GET /api/orders/client/{clientId}
```

## Order States

Orders can be in one of three states:
- `PENDING`: Initial state of an order
- `COMPLETED`: Order has been successfully processed 
- `CANCELLED`: Order has been cancelled

## Running the service

```bash
./gradlew bootRun
```

Or using Docker:

```bash
docker build -t order-service .
docker run -p 8083:8083 order-service
```

## Usage Examples

### Create an order

```bash
curl -X POST 'http://localhost:8083/api/orders' \
-H 'Content-Type: application/json' \
-d '{
  "clientId": 1,
  "orderLines": [
    {
      "productId": 101,
      "quantity": 2,
      "unitPrice": 29.99
    },
    {
      "productId": 102,
      "quantity": 1,
      "unitPrice": 49.99
    }
  ]
}'
```

### Get orders by client ID

```bash
curl -X GET 'http://localhost:8083/api/orders/client/1' \
-H 'Accept: application/json'
```

## Database

The service uses an in-memory H2 database accessible at:
```
http://localhost:8083/h2-console
```

Connection details:
- JDBC URL: `jdbc:h2:mem:orderdb`
- User: `sa`
- Password: `password`

## Architecture

The Order Service is built with Spring Boot and follows a standard layered architecture:

- **Controller Layer**: Handles HTTP requests/responses
- **Service Layer**: Contains business logic
- **Repository Layer**: Interacts with the database
- **Domain Layer**: Contains the entity models

Key classes:
- `Order`: Main entity representing customer orders
- `OrderLine`: Entity representing individual line items in an order
- `OrderController`: REST API endpoints for order operations
- `OrderService`: Business logic for order operations
- `OrderRepository`: Data access for orders

## Integration with Order Resilience Demo

This Order Service is part of the larger order-resilience demo that showcases resilient ordering patterns with LittleHorse. It interacts with other services to provide a complete ordering experience.

The order resilience architecture demonstrates:
- Service resilience
- Handling transient failures
- Error recovery mechanisms
- Event-driven architecture

## Notes

- This is a simplified implementation for demonstration purposes
- No authentication/authorization is implemented
- For production, additional validation and error handling would be required
