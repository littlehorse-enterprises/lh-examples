package io.littlehorse.examples.tasks;

import java.util.Arrays;

import io.littlehorse.examples.dto.ProductStockItem;
import io.littlehorse.examples.exceptions.InsufficientStockException;
import io.littlehorse.examples.exceptions.ProductNotFoundException;
import io.littlehorse.examples.service.ProductService;
import io.littlehorse.quarkus.task.LHTask;
import io.littlehorse.sdk.worker.LHTaskMethod;
import jakarta.inject.Inject;

@LHTask
public class ProductTask {
    public static final String DISPATCH_ORDER = "dispatch-order";

    @Inject
    ProductService productService;

    public ProductTask(ProductService productService) {
        this.productService = productService;
    }

    @LHTaskMethod(DISPATCH_ORDER)
    public void dispatchOrder(ProductStockItem[] productItems) throws ProductNotFoundException, InsufficientStockException {
         this.productService.reduceStock(Arrays.asList(productItems));
    }
}
