package io.littlehorse.examples.controller;

import java.util.List;
import java.util.Map;

import io.littlehorse.examples.dto.ProductResponse;
import io.littlehorse.examples.dto.ProductStockItem;
import io.littlehorse.examples.dto.StockValidationListRequest;
import io.littlehorse.examples.dto.StockValidationRequest;
import io.littlehorse.examples.exceptions.InsufficientStockException;
import io.littlehorse.examples.exceptions.ProductNotFoundException;
import io.littlehorse.examples.mapper.ProductMapper;
import io.littlehorse.examples.model.Product;
import io.littlehorse.examples.service.ProductService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
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
    

    @POST
    @Path("/reduce-stock")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reduceStock( List<ProductStockItem> productItems) {
        try {
            productService.reduceStock(productItems);
            return Response.ok().build();
        } catch (ProductNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (InsufficientStockException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }
}
