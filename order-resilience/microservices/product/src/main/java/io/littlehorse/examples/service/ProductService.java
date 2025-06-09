package io.littlehorse.examples.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

  public List<Product> getAllProducts() {
    return repository.listAll();
  }

  public Product get(Long id) throws ProductNotFoundException {
    return repository.findById(id);
  }

  @Transactional
  public Map<Long, Integer> reduceStock(Map<Long, Integer> productQuantities) throws ProductNotFoundException, InsufficientStockException {
    // Validate all products exist and have sufficient stock
    for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
      Long productId = entry.getKey();
      int requestedQuantity = entry.getValue();
      
      Product product = get(productId);
      if (product.getStock() < requestedQuantity) {
        throw new InsufficientStockException("Insufficient stock for product: " + product.getName() + " (ID: " + productId + "). Available: " + product.getStock() + ", Requested: " + requestedQuantity);
      }
    }
    
    // Reduce stock for all products
    return productQuantities.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> {
              Long productId = entry.getKey();
              int quantity = entry.getValue();
              try {
                Product product = get(productId);
                product.setStock(product.getStock() - quantity);
                repository.persist(product);
                return quantity;
              } catch (ProductNotFoundException e) {
                // This shouldn't happen as we already validated all products exist
                throw new RuntimeException("Product disappeared during transaction: " + productId);
              }
            }
        ));
  }
}
