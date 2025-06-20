package io.littlehorse.examples.tasks;

import java.util.Arrays;
import java.util.Date;

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
import io.littlehorse.sdk.worker.WorkerContext;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;


@LHTask
public class ProductTask {
    public static final String DISPATCH_ORDER = "dispatch-order";
    public static final String APPLY_DISCOUNTS = "apply-discounts";
    private static final Logger LOG = Logger.getLogger(ProductTask.class);

    @Inject
    ProductService productService;

    public ProductTask(ProductService productService) {
        this.productService = productService;
    }

    @LHTaskMethod(DISPATCH_ORDER)
    public ProductResponse[] dispatch(int clientid, ProductStockItem[] productItems, WorkerContext workerContext) throws ProductNotFoundException, InsufficientStockException, JsonProcessingException {
        var startTime=new Date();
        LOG.infof("LHinfo wfRunId %s, nodeRunId %s, taskRunId %s ", workerContext.getWfRunId(),workerContext.getNodeRunId(),workerContext.getTaskRunId());
        LOG.infof("Dispatching order for client %d at %s ", clientid, startTime);
        var productResponse = this.productService.dispatch(clientid, Arrays.asList(productItems));
        var endTime = new Date();
        LOG.infof("Order dispatched for client %d at %s, took %d ms", clientid, endTime, endTime.getTime() - startTime.getTime());
        return productResponse;
    }

    @LHTaskMethod(APPLY_DISCOUNTS)
    public ProductResponse[] applyDiscounts(int clientid, ProductPriceItem[] products, ProductDiscountItem[] discounts, WorkerContext workerContext) throws ProductNotFoundException, InvalidPriceException, JsonProcessingException {
        var startTime=new Date();
        LOG.infof("LHinfo wfRunId %s, nodeRunId %s, taskRunId %s ", workerContext.getWfRunId(),workerContext.getNodeRunId(),workerContext.getTaskRunId());
        LOG.infof("Appliying discounts for client %d at %s", clientid, startTime);
        var productResponse = this.productService.applyDiscpunts(clientid, Arrays.asList(products), Arrays.asList(discounts));
        var endTime = new Date();
        LOG.infof("Discounts applied for client %d at %s, took %d ms", clientid, endTime, endTime.getTime() - startTime.getTime());
        return productResponse;
    }
}
