package io.littlehorse.examples.service;

import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.littlehorse.examples.dto.*;
import io.littlehorse.examples.exceptions.dto.ProductError;
import io.littlehorse.examples.exceptions.InsufficientStockException;
import io.littlehorse.examples.exceptions.InvalidPriceException;
import io.littlehorse.examples.exceptions.ProductNotFoundException;
import io.littlehorse.examples.mapper.ProductMapper;
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

    @Inject
    private ProductMapper productMapper;


    public List<Product> getAllProducts() {
        return repository.listAll();
    }

    public Product get(Long id) throws ProductNotFoundException {
        return repository.findById(id);
    }

    @Transactional
    public ProductResponse addStock(ProductStockItem item) {
        Product product = repository.findById(item.getProductId());
        if (product == null) {
            return null; // or throw an exception
        }
        product.setQuantity(product.getQuantity() + item.getQuantity());
        repository.persist(product);
        return productMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse[] dispatch(int clientId, List<ProductStockItem> productItems) throws ProductNotFoundException, InsufficientStockException, JsonProcessingException {
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
                ProductResponse productResponse = productMapper.toResponse(product);
                productResponse.setAvailableStock(product.getQuantity());
                productResponse.setRequestedQuantity(requestedQuantity);
                insufficientStockProducts.add(productResponse);
            }
        }

        // Throw exception if any products have insufficient stock
        if (!insufficientStockProducts.isEmpty()) {
            var errors = insufficientStockProducts.stream()
                    .map(product -> {
                        if (product.getCategory().equalsIgnoreCase("food")){
                            return "Sorry, no engredients for preparing ( " + product.getName() + " ), but you can order it later :)";
                        } else
                            return "No stock for ( " + product.getName() + " )";
                    })
                    .collect(Collectors.joining("; "));
            ProductError productError = ProductError.builder()
                    .clientId(clientId)
                    .products(insufficientStockProducts)
                    .message(errors)
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
        // Return the updated product responses
        return productItems.stream()
                .map(item -> productMapper.toResponse(productMap.get(item.getProductId())))
                .toArray(ProductResponse[]::new);
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
    public ProductResponse[] applyDiscpunts(int clientId, List<ProductPriceItem> products, List<ProductDiscountItem> discounts) throws ProductNotFoundException, InsufficientStockException, JsonProcessingException {
        // Collect all product IDs to validate
        List<Long> productIds = products.stream()
                .map(ProductPriceItem::getProductId)
                .collect(Collectors.toList());

        // Validate product existence
        Map<Long, ProductResponse> productMap = validateProductExistance(productIds).entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> productMapper.toResponse(entry.getValue())
                ));

        // Validate price for each product, it shouldn't be less than the cost
        List<ProductResponse> invalidPriceProducts = new ArrayList<>();
        for (ProductPriceItem item : products) {
            long productId = item.getProductId();
            ProductResponse product = productMap.get(productId);
            double discountPercentage = discounts.stream()
                    .filter(discount -> discount.getProductId() == productId)
                    .findFirst()
                    .map(ProductDiscountItem::getDiscountPercentage)
                    .orElse(0.0);
            double price = product.getUnitPrice() * (1 - discountPercentage / 100);
            product.setUnitPrice(price);
            product.setDiscountPercentage(discountPercentage);

            if (product.getUnitPrice() < product.getCost()) {
                invalidPriceProducts.add(product);
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
        // All validations passed, return the product responses
        return products.stream()
                .map(item -> productMap.get(item.getProductId()))
                .toArray(ProductResponse[]::new);
    }
}
