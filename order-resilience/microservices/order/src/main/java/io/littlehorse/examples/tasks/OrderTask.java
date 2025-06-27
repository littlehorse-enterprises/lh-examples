package io.littlehorse.examples.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.littlehorse.examples.dto.OrderRequest;
import io.littlehorse.examples.dto.OrderResponse;
import io.littlehorse.examples.dto.ProductDiscountItem;
import io.littlehorse.examples.dto.ProductPriceItem;
import io.littlehorse.quarkus.task.LHTask;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;
import jakarta.inject.Inject;
import io.littlehorse.examples.services.OrderService;
import org.jboss.logging.Logger;

import java.util.Date;

@LHTask
public class OrderTask {
    public static final String SAVE_ORDER_TASK = "place-order";
    public static final String UPDATE_ORDER_STATUS = "update-order-status";
    public static final String FINALIZE_ORDER_TASK = "finalize-order";
    private static final Logger LOG = Logger.getLogger(OrderTask.class);


    @Inject
    OrderService orderService;

    @LHTaskMethod(SAVE_ORDER_TASK)
    public OrderResponse saveOrder(OrderRequest orderRequest, WorkerContext workerContext) {
        var startTime = new Date();
        LOG.infof("LHinfo wfRunId %s, nodeRunId %s, taskRunId %s ", workerContext.getWfRunId(), workerContext.getNodeRunId(), workerContext.getTaskRunId());
        LOG.infof("Placing order for %d at %s ", orderRequest.getClientId(), startTime);
        var response = this.orderService.saveOrder(orderRequest);
        var endTime = new Date();
        LOG.infof("Placed order for %d at %s , took %d ms", orderRequest.getClientId(), endTime, endTime.getTime() - startTime.getTime());
        return response;
    }

    @LHTaskMethod(UPDATE_ORDER_STATUS)
    public OrderResponse updateOrderStatus(Long orderId, String orderStatus, String message, WorkerContext workerContext) {
        var startTime = new Date();
        LOG.infof("LHinfo wfRunId %s, nodeRunId %s, taskRunId %s ", workerContext.getWfRunId(), workerContext.getNodeRunId(), workerContext.getTaskRunId());
        LOG.infof("Updating order %d at %s ", orderId, startTime);
        var response = this.orderService.updateOrderStatus(orderId, orderStatus, message);
        var endTime = new Date();
        LOG.infof("Updated order %d at %s , took %d ms", orderId, endTime, endTime.getTime() - startTime.getTime());
        return response;
    }

    @LHTaskMethod(FINALIZE_ORDER_TASK)
    public OrderResponse finalizeOrder(Long orderId, ProductPriceItem[] productPrices, WorkerContext workerContext) throws JsonProcessingException {
        var startTime = new Date();
        LOG.infof("LHinfo wfRunId %s, nodeRunId %s, taskRunId %s ", workerContext.getWfRunId(), workerContext.getNodeRunId(), workerContext.getTaskRunId());
        LOG.infof("Completing order %d at %s ", orderId, startTime);
        var response = this.orderService.finalizeOrder(orderId, productPrices);
        var endTime = new Date();
        LOG.infof("Completed order %d at %s , took %d ms", orderId, endTime, endTime.getTime() - startTime.getTime());
        return response;
    }
}
