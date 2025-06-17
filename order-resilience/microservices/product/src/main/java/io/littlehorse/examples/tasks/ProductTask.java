package io.littlehorse.examples.tasks;

import java.util.Arrays;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.littlehorse.examples.dto.ProductDiscountItem;
import io.littlehorse.examples.dto.ProductPriceItem;
import io.littlehorse.examples.dto.ProductResponse;
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
    public static final String APPLY_DISCOUNTS = "apply-discounts";

    @Inject
    ProductService productService;

    public ProductTask(ProductService productService) {
        this.productService = productService;
    }

    @LHTaskMethod(DISPATCH_ORDER)
    public ProductResponse[] dispatch(int clientid, ProductStockItem[] productItems) throws ProductNotFoundException, InsufficientStockException, JsonProcessingException {
        return this.productService.dispatch(clientid, Arrays.asList(productItems));
    }

    @LHTaskMethod(APPLY_DISCOUNTS)
    public ProductResponse[] applyDiscounts(int clientid, ProductPriceItem[] products, ProductDiscountItem[] discounts) throws ProductNotFoundException, InvalidPriceException, JsonProcessingException {
       return this.productService.applyDiscpunts(clientid, Arrays.asList(products), Arrays.asList(discounts));
    }
}
