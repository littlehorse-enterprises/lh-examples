package io.littlehorse.examples.tasks;

import io.littlehorse.examples.dto.OrderRequest;
import io.littlehorse.examples.dto.OrderResponse;
import io.littlehorse.examples.models.Order;
import io.littlehorse.quarkus.task.LHTask;
import io.littlehorse.sdk.worker.LHTaskMethod;
import jakarta.inject.Inject;
import io.littlehorse.examples.services.OrderService;

@LHTask
public class OrderTask {
    public static final String SAVE_ORDER = "save-order";

    @Inject
    OrderService orderService;

    public OrderTask(OrderService orderService) {
        this.orderService = orderService;
    }

    @LHTaskMethod(SAVE_ORDER)
    public OrderResponse reduceStock(OrderRequest orderRequest) {
        return this.orderService.saveOrder(orderRequest);
    }
}
