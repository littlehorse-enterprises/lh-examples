package io.littlehorse.orderresilience.product.product;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.littlehorse.orderresilience.product.product.dto.ProductRequest;
import io.littlehorse.orderresilience.product.product.dto.ProductResponse;
import io.littlehorse.orderresilience.product.product.dto.StockValidationRequest;
import io.littlehorse.orderresilience.product.product.dto.StockValidationResponse;
import io.littlehorse.orderresilience.product.product.exceptions.InsufficientStockException;
import io.littlehorse.orderresilience.product.product.exceptions.ProductNotFoundException;
import io.littlehorse.orderresilience.product.product.mapper.ProductMapper;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductMapper productMapper;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return new ResponseEntity<>(productMapper.toResponseList(products), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("id") Integer id) throws ProductNotFoundException {
        Product product = productService.get(id);
        return new ResponseEntity<>(productMapper.toResponse(product), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> getProductsBySearchCriteria(@RequestParam("criteria") String criteria) {
        List<Product> products = productService.findBySearchCriteria(criteria);
        return new ResponseEntity<>(productMapper.toResponseList(products), HttpStatus.OK);
    }
    
    @GetMapping("/in-stock")
    public ResponseEntity<List<ProductResponse>> getProductsInStock(@RequestParam(value = "minStock", defaultValue = "1") int minStock) {
        List<Product> products = productService.findProductsInStock(minStock);
        return new ResponseEntity<>(productMapper.toResponseList(products), HttpStatus.OK);
    }
    
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        Product product = productService.create(productMapper.toEntity(productRequest));
        return new ResponseEntity<>(productMapper.toResponse(product), HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable("id") Integer id,
            @Valid @RequestBody ProductRequest productRequest) throws ProductNotFoundException {
        
        Product product = productService.update(id, productMapper.toEntity(productRequest));
        return new ResponseEntity<>(productMapper.toResponse(product), HttpStatus.OK);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") Integer id) throws ProductNotFoundException {
        productService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @PostMapping("/validate-stock")
    public ResponseEntity<?> validateAndReduceStock(@Valid @RequestBody StockValidationRequest request) {
        try {
            Map<Integer, Integer> confirmedQuantities = productService.validateAndReduceStock(request.getProductQuantities());
            return new ResponseEntity<>(new StockValidationResponse(confirmedQuantities), HttpStatus.OK);
        } catch (ProductNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (InsufficientStockException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
