package io.littlehorse.examples.dto;

import io.littlehorse.examples.models.OrderStatus;
import lombok.Data;

import java.util.List;

@Data
public class OrderResponse {
    private int id;

    private int clientId;

    private String errorMessage;

    private double total;

    private OrderStatus status = OrderStatus.PENDING;

    private List<OrderLineRequest> orderLines;
    
    @Data
    public static class OrderLineRequest {
        private int productId;
        private int quantity;
        private double unitPrice;
    }
}
