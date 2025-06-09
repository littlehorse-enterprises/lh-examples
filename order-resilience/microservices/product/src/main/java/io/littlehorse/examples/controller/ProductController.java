package io.littlehorse.examples.controller;

import java.util.List;

import io.littlehorse.examples.dto.ProductResponse;
import io.littlehorse.examples.mapper.ProductMapper;
import io.littlehorse.examples.model.Product;
import io.littlehorse.examples.service.ProductService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("/api/products")
@Produces(MediaType.APPLICATION_JSON)
public class ProductController {

    @Inject
    private ProductService productService;
    
    @Inject
    private ProductMapper productMapper;

    @GET
    public Response getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return Response.ok(productMapper.toResponseList(products)).build();
    }

}
