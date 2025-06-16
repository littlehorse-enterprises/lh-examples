package io.littlehorse.examples.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.littlehorse.examples.dto.OrderRequest;
import io.littlehorse.examples.dto.OrderResponse;
import io.littlehorse.examples.dto.ProductDiscountItem;
import io.littlehorse.examples.dto.ProductPriceItem;
import io.littlehorse.quarkus.task.LHTask;
import io.littlehorse.sdk.worker.LHTaskMethod;
import jakarta.inject.Inject;
import io.littlehorse.examples.services.OrderService;

@LHTask
public class OrderTask {
    public static final String SAVE_ORDER_TASK = "place-order";
    public static final String UPDATE_ORDER_STATUS = "update-order-status";
    public static final String FINALIZE_ORDER_TASK = "finalize-order";

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

    @LHTaskMethod(FINALIZE_ORDER_TASK)
    public OrderResponse finalizeOrder(Long orderId, ProductPriceItem[] productPrices, ProductDiscountItem[] discountItems) throws JsonProcessingException {
        return this.orderService.finalizeOrder(orderId, productPrices, discountItems);
    }
}
