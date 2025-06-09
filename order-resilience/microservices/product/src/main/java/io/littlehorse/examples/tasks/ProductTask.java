package io.littlehorse.examples.tasks;

import java.util.Map;

import io.littlehorse.examples.exceptions.InsufficientStockException;
import io.littlehorse.examples.exceptions.ProductNotFoundException;
import io.littlehorse.examples.service.ProductService;
import io.littlehorse.quarkus.task.LHTask;
import io.littlehorse.sdk.worker.LHTaskMethod;
import jakarta.inject.Inject;

@LHTask
public class ProductTask {
    public static final String REDUCE_STOCK = "reduce-stock";

    @Inject
    ProductService productService;

    public ProductTask(ProductService productService) {
        this.productService = productService;
    }

    @LHTaskMethod(REDUCE_STOCK)
    public Map<Long, Integer> reduceStock(Map<Long, Integer> productQuantities) throws ProductNotFoundException, InsufficientStockException {
        return this.productService.reduceStock(productQuantities);
    }
}
