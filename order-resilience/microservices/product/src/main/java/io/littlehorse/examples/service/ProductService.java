package io.littlehorse.examples.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.littlehorse.examples.exceptions.dto.ProductError;
import io.littlehorse.examples.dto.ProductPriceItem;
import io.littlehorse.examples.dto.ProductResponse;
import io.littlehorse.examples.dto.ProductStockItem;
import io.littlehorse.examples.exceptions.InsufficientStockException;
import io.littlehorse.examples.exceptions.InvalidPriceException;
import io.littlehorse.examples.exceptions.ProductNotFoundException;
import io.littlehorse.examples.model.Product;
import io.littlehorse.examples.repositories.ProductRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProductService {
    @Inject
    private ProductRepository repository;

    @Inject
    private ObjectMapper objectMapper;

    public List<Product> getAllProducts() {
        return repository.listAll();
    }

    public Product get(Long id) throws ProductNotFoundException {
        return repository.findById(id);
    }

    @Transactional
    public void dispatch(int clientId, List<ProductStockItem> productItems) throws ProductNotFoundException, InsufficientStockException, JsonProcessingException {
        if (productItems == null || productItems.isEmpty()) {
            return; // No items to process
        }
        // Collect all product IDs to validate
        List<Long> productIds = productItems.stream()
                .map(ProductStockItem::getProductId)
                .collect(Collectors.toList());
        // Validate product existence
        Map<Long, Product> productMap = validateProductExistance(productIds);

        // Validate stock and accumulate reductions for each product
        Map<Long, Integer> stockReductions = new HashMap<>();
        for (ProductStockItem item : productItems) {
            Long productId = item.getProductId();
            Integer quantity = item.getQuantity();
            stockReductions.merge(productId, quantity, Integer::sum);
        }

        // Check stock availability and update products
        List<ProductResponse> insufficientStockProducts = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : stockReductions.entrySet()) {
            Long productId = entry.getKey();
            Integer requestedQuantity = entry.getValue();
            Product product = productMap.get(productId);

            if (product.getQuantity() < requestedQuantity) {
                ProductResponse productResponse = ProductResponse.builder()
                        .productId(productId)
                        .requestedQuantity(requestedQuantity)
                        .availableStock(product.getQuantity())
                        .name(product.getName())
                        .build();
                insufficientStockProducts.add(productResponse);
            }
        }

        // Throw exception if any products have insufficient stock
        if (!insufficientStockProducts.isEmpty()) {
            ProductError productError = ProductError.builder()
                    .clientId(clientId)
                    .products(insufficientStockProducts)
                    .message("Insufficient stock for products")
                    .build();
            throw new InsufficientStockException(objectMapper.writeValueAsString(productError));
        }

        // All validations passed, now reduce the stock
        for (Map.Entry<Long, Integer> entry : stockReductions.entrySet()) {
            Long productId = entry.getKey();
            Integer requestedQuantity = entry.getValue();
            Product product = productMap.get(productId);
            product.setQuantity(product.getQuantity() - requestedQuantity);
            repository.persist(product);
        }
    }

    @Transactional
    public Map<Long, Product> validateProductExistance(List<Long> productIds) throws ProductNotFoundException {
        // Find all products in one go
        Map<Long, Product> productMap = repository.find("productId in ?1", productIds)
                .stream()
                .collect(Collectors.toMap(Product::getProductId, product -> product));

        // Validate all product existnance
        List<Long> notFoundIds = new ArrayList<>();
        for (Long id : productIds) {
            if (!productMap.containsKey(id)) {
                notFoundIds.add(id);
            }
        }

        // Throw exception if any products not found
        if (!notFoundIds.isEmpty()) {
            String errorMessage = "Products not found: " + notFoundIds;
            throw new ProductNotFoundException(errorMessage);
        }
        return productMap;
    }

    @Transactional
    public void validateProductPrice(int clientId, List<ProductPriceItem> productItems) throws ProductNotFoundException, InsufficientStockException, JsonProcessingException {
        if (productItems == null || productItems.isEmpty()) {
            return; // No items to process
        }

        // Collect all product IDs to validate
        List<Long> productIds = productItems.stream()
                .map(ProductPriceItem::getProductId)
                .collect(Collectors.toList());

        // Validate product existence
        Map<Long, Product> productMap = validateProductExistance(productIds);

        // Validate price for each product, it shouldn't be less than the cost
        List<ProductResponse> invalidPriceProducts = new ArrayList<>();
        for (ProductPriceItem item : productItems) {
            Long productId = item.getProductId();
            Double price = item.getPrice();
            Product product = productMap.get(productId);

            if (product == null) {
                continue; // This should not happen due to previous validation
            }

            if (price < product.getCost()) {
                ProductResponse productResponse = ProductResponse.builder()
                        .productId(productId)
                        .name(product.getName())
                        .price(price)
                        .cost(product.getCost())
                        .build();
                invalidPriceProducts.add(productResponse);
            }

        }
        // Throw exception if any products have invalid price
        if (!invalidPriceProducts.isEmpty()) {
            ProductError productError = ProductError.builder()
                    .clientId(clientId) // Assuming clientId is not relevant for price validation
                    .products(invalidPriceProducts)
                    .message("Invalid product prices")
                    .build();
            throw new InvalidPriceException(objectMapper.writeValueAsString(productError));
        }
    }
}
