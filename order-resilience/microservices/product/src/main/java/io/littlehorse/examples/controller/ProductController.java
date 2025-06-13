package io.littlehorse.examples.controller;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.littlehorse.examples.dto.ProductStockItem;
import io.littlehorse.examples.exceptions.InsufficientStockException;
import io.littlehorse.examples.exceptions.ProductNotFoundException;
import io.littlehorse.examples.mapper.ProductMapper;
import io.littlehorse.examples.model.Product;
import io.littlehorse.examples.service.ProductService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
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
    @Path("/dispatch/client/{clientId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reduceStock(@PathParam("clientId") int clientId, List<ProductStockItem> productItems) throws JsonProcessingException {
            productService.dispatch(clientId,productItems);
            return Response.ok().build();

    }
}
