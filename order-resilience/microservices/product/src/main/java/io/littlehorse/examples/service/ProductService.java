package io.littlehorse.examples.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.littlehorse.examples.dto.DispatchErrorResponse;
import io.littlehorse.examples.dto.ProductResponse;
import io.littlehorse.examples.dto.ProductStockItem;
import io.littlehorse.examples.exceptions.InsufficientStockException;
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
    public void dispatch(int clientId,List<ProductStockItem> productItems) throws ProductNotFoundException, InsufficientStockException, JsonProcessingException {
        if (productItems == null || productItems.isEmpty()) {
            return; // No items to process
        }

        // Collect all product IDs to validate
        List<Long> productIds = productItems.stream()
                .map(ProductStockItem::getProductId)
                .collect(Collectors.toList());

        // Find all products in one go
        Map<Long, Product> productMap = repository.find("productId in ?1", productIds)
                .stream()
                .collect(Collectors.toMap(Product::getProductId, product -> product));

        // Validate all products exist
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

        // Validate stock and accumulate reductions for each product
        Map<Long, Integer> stockReductions = new HashMap<>();
        for (ProductStockItem item : productItems) {
            Long productId = item.getProductId();
            Integer quantity = item.getQuantity();
            stockReductions.merge(productId, quantity, Integer::sum);
        }

        // Check stock availability and update products
        List <ProductResponse> insufficientStockProducts = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : stockReductions.entrySet()) {
            Long productId = entry.getKey();
            Integer requestedQuantity = entry.getValue();
            Product product = productMap.get(productId);

            if (product.getQuantity() < requestedQuantity) {
                ProductResponse productResponse=ProductResponse.builder()
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
            DispatchErrorResponse dispatchErrorResponse=DispatchErrorResponse.builder()
                    .clientId(clientId)
                    .products(insufficientStockProducts)
                    .message("Insufficient stock for products")
                    .build();
            throw new InsufficientStockException(objectMapper.writeValueAsString(dispatchErrorResponse));
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
}
