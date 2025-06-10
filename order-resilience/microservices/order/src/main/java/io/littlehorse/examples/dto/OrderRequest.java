package io.littlehorse.examples.dto;

import java.util.List;

import lombok.Data;

@Data
public class OrderRequest {
    private int orderId;

    private int clientId;

    private String message;

    private double total;

    private List<OrderLineRequest> orderLines;
    
    @Data
    public static class OrderLineRequest {
        private int productId;
        private int quantity;
        private double unitPrice;
    }
}
