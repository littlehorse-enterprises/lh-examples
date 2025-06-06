# Product Service

This service manages product inventory and stock validation for the order resilience demo.

## Features

- Product management (CRUD operations)
- Search products by criteria
- Check product availability
- Validate and reduce stock

## API Endpoints

### Get all products
```
GET /api/products
```

### Get product by ID
```
GET /api/products/{id}
```

### Search products by criteria
```
GET /api/products/search?criteria={searchTerm}
```

### Get products in stock
```
GET /api/products/in-stock?minStock={minimumStock}
```

### Create a new product
```
POST /api/products
```

### Update a product
```
PUT /api/products/{id}
```

### Delete a product
```
DELETE /api/products/{id}
```

### Validate and reduce stock
```
POST /api/products/validate-stock
```
Request body:
```json
{
  "productQuantities": {
    "product-uuid-1": 5,
    "product-uuid-2": 3
  }
}
```

## Running the service

```bash
./gradlew bootRun
```

Or using Docker:

```bash
docker build -t product-service .
docker run -p 8081:8081 product-service
```
