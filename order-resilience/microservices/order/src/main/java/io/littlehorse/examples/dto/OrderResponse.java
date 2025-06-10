package io.littlehorse.examples.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderResponse {
    private int orderId;

    private int clientId;

    private String message;

    private double total;

    private String status = "PENDING";

    private List<OrderLineRequest> orderLines;
    
    @Data
    public static class OrderLineRequest {
        private int productId;
        private int quantity;
        private double unitPrice;
    }
}
