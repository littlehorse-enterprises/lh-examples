package io.littlehorse.orderresilience.product.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.littlehorse.orderresilience.product.Product;
import io.littlehorse.orderresilience.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.littlehorse.orderresilience.product.exceptions.InsufficientStockException;
import io.littlehorse.orderresilience.product.exceptions.ProductNotFoundException;

@Service
public class ProductService {
  @Autowired
  private ProductRepository repository;

  public List<Product> getAllProducts() {
    List<Product> products = new ArrayList<>();
    repository.findAll().forEach(products::add);
    return products;
  }

  public Product get(Integer id) throws ProductNotFoundException {
    return repository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
  }

  public Product create(Product product) {
    return repository.save(product);
  }

  public Product update(Integer id, Product productDetails) throws ProductNotFoundException {
    Product product = get(id);
    product.setName(productDetails.getName());
    product.setDescription(productDetails.getDescription());
    product.setPrice(productDetails.getPrice());
    product.setStock(productDetails.getStock());
    product.setCategory(productDetails.getCategory());
    return repository.save(product);
  }

  public void delete(Integer id) throws ProductNotFoundException {
    Product product = get(id);
    repository.delete(product);
  }
  
  public List<Product> findBySearchCriteria(String criteria){
    return repository.findBySearchCriteria(criteria);
  }
  
  public List<Product> findProductsInStock(int minStock) {
    return repository.findByStockGreaterThanEqual(minStock);
  }
  
  @Transactional
  public Map<Integer, Integer> validateAndReduceStock(Map<Integer, Integer> productQuantities) throws ProductNotFoundException, InsufficientStockException {
    // Validate all products exist and have sufficient stock
    for (Map.Entry<Integer, Integer> entry : productQuantities.entrySet()) {
      Integer productId = entry.getKey();
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
              Integer productId = entry.getKey();
              int quantity = entry.getValue();
              try {
                Product product = get(productId);
                product.setStock(product.getStock() - quantity);
                repository.save(product);
                return quantity;
              } catch (ProductNotFoundException e) {
                // This shouldn't happen as we already validated all products exist
                throw new RuntimeException("Product disappeared during transaction: " + productId);
              }
            }
        ));
  }
}
