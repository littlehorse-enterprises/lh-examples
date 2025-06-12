package io.littlehorse.examples.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.littlehorse.examples.dto.ProductStockItem;
import io.littlehorse.examples.exceptions.InsufficientStockException;
import io.littlehorse.examples.exceptions.ProductNotFoundException;
import io.littlehorse.examples.model.Product;
import io.littlehorse.examples.repositories.ProductRepository;
import io.littlehorse.sdk.common.proto.VariableValue;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProductService {
  @Inject
  private ProductRepository repository;

  public List<Product> getAllProducts() {
    return repository.listAll();
  }

  public Product get(Long id) throws ProductNotFoundException {
    return repository.findById(id);
  }

  @Transactional
  public void reduceStock(List<ProductStockItem> productItems) throws ProductNotFoundException, InsufficientStockException {
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
      throw new ProductNotFoundException("Products not found: " + notFoundIds);
    }
    
    // Validate stock and accumulate reductions for each product
    Map<Long, Integer> stockReductions = new HashMap<>();
    for (ProductStockItem item : productItems) {
      Long productId = item.getProductId();
      Integer quantity = item.getQuantity();
      
      // Accumulate quantities in case the same product appears multiple times
      stockReductions.merge(productId, quantity, Integer::sum);
    }
    
    // Check stock availability and update products
    List<String> insufficientStockProducts = new ArrayList<>();
    for (Map.Entry<Long, Integer> entry : stockReductions.entrySet()) {
      Long productId = entry.getKey();
      Integer requestedQuantity = entry.getValue();
      Product product = productMap.get(productId);
      
      if (product.getQuantity() < requestedQuantity) {
        insufficientStockProducts.add(
            String.format("Product [ %s ] has %d items in stock, but %d were requested",
                product.getName(), product.getQuantity(), requestedQuantity)
        );
      }
    }
    
    // Throw exception if any products have insufficient stock
    if (!insufficientStockProducts.isEmpty()) {
      String message ="Error dispatching the order, " + String.join("; ", insufficientStockProducts);
      throw new InsufficientStockException( message, VariableValue.newBuilder().setStr(message).build());
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
