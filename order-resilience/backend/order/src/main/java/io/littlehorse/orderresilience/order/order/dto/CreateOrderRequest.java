package io.littlehorse.orderresilience.order.order.dto;

import java.util.List;

import lombok.Data;

@Data
public class CreateOrderRequest {
    private int clientId;
    private List<OrderLineRequest> orderLines;
    
    @Data
    public static class OrderLineRequest {
        private int productId;
        private int quantity;
        private double unitPrice;
    }
}
