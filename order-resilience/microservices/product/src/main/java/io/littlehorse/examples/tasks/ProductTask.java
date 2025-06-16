package io.littlehorse.examples.tasks;

import java.util.Arrays;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.littlehorse.examples.dto.ProductPriceItem;
import io.littlehorse.examples.dto.ProductStockItem;
import io.littlehorse.examples.exceptions.InsufficientStockException;
import io.littlehorse.examples.exceptions.InvalidPriceException;
import io.littlehorse.examples.exceptions.ProductNotFoundException;
import io.littlehorse.examples.service.ProductService;
import io.littlehorse.quarkus.task.LHTask;
import io.littlehorse.sdk.worker.LHTaskMethod;
import jakarta.inject.Inject;

@LHTask
public class ProductTask {
    public static final String DISPATCH_ORDER = "dispatch-order";
    public static final String VALIDATE_PRICE = "validate-price";

    @Inject
    ProductService productService;

    public ProductTask(ProductService productService) {
        this.productService = productService;
    }

    @LHTaskMethod(DISPATCH_ORDER)
    public void dispatch(int clientid, ProductStockItem[] productItems) throws ProductNotFoundException, InsufficientStockException, JsonProcessingException {
        this.productService.dispatch(clientid, Arrays.asList(productItems));
    }

    @LHTaskMethod(VALIDATE_PRICE)
    public void validatePrice(int clientid, ProductPriceItem[] productItems) throws ProductNotFoundException, InvalidPriceException, JsonProcessingException {
        this.productService.validateProductPrice(clientid, Arrays.asList(productItems));
    }
}
