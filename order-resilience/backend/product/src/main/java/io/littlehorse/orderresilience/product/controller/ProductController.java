package io.littlehorse.orderresilience.product.controller;

import java.util.List;

import io.littlehorse.orderresilience.product.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.littlehorse.orderresilience.product.dto.ProductResponse;
import io.littlehorse.orderresilience.product.mapper.ProductMapper;
import io.littlehorse.orderresilience.product.service.ProductService;

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

}
