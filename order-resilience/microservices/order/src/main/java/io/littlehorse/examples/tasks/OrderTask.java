package io.littlehorse.examples.tasks;

import io.littlehorse.examples.dto.OrderRequest;
import io.littlehorse.examples.dto.OrderResponse;
import io.littlehorse.quarkus.task.LHTask;
import io.littlehorse.sdk.worker.LHTaskMethod;
import jakarta.inject.Inject;
import io.littlehorse.examples.services.OrderService;

@LHTask
public class OrderTask {
    public static final String SAVE_ORDER_TASK = "save-order";
    public static final String UPDATE_ORDER_STATUS = "update-order-status";

    @Inject
    OrderService orderService;

    @LHTaskMethod(SAVE_ORDER_TASK)
    public OrderResponse saveOrder(OrderRequest orderRequest) {
        return this.orderService.saveOrder(orderRequest);
    }

    @LHTaskMethod(UPDATE_ORDER_STATUS)
    public OrderResponse updateOrderStatus(Long orderId, String orderStatus, String message) {
        return this.orderService.updateOrderStatus(orderId, orderStatus, message);
    }
}
