package io.littlehorse.examples.mapper;

import java.util.List;
import java.util.stream.Collectors;


import io.littlehorse.examples.exceptions.dto.ProductError;
import io.littlehorse.examples.dto.ProductRequest;
import io.littlehorse.examples.dto.ProductResponse;
import io.littlehorse.examples.model.Product;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        return  ProductResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .unitPrice(product.getPrice())
                .cost(product.getCost())
                .quantity(product.getQuantity())
                .availableStock(product.getQuantity())
                .category(product.getCategory())
                .build();
    }

    public List<ProductResponse> toResponseList(List<Product> products) {
        return products.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Product toEntity(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setCategory(request.getCategory());
        return product;
    }
    public Product toEntity(ProductResponse response) {
        Product product = new Product();
        product.setName(response.getName());
        product.setDescription(response.getDescription());
        product.setCost(response.getCost());
        product.setPrice(response.getUnitPrice());
        product.setQuantity(response.getQuantity());
        product.setCategory(response.getCategory());
        return product;
    }
    public ProductError toDispatchErrorResponse(long clientId, List<ProductResponse> products, String errorMessage) {
        return ProductError.builder()
                .clientId(clientId)
                .products(products)
                .message(errorMessage)
                .build();
    }
}
