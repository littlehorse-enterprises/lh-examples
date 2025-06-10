package io.littlehorse.examples.tasks;

import java.util.List;
import java.util.Map;

import io.littlehorse.examples.dto.ProductStockItem;
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
    public void reduceStock(List<ProductStockItem> productItems) throws ProductNotFoundException, InsufficientStockException {
         this.productService.reduceStock(productItems);
    }
}
