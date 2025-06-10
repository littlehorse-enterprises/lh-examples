package io.littlehorse.examples.dto;

import java.util.List;

import io.littlehorse.examples.models.OrderStatus;
import lombok.Data;

@Data
public class OrderRequest {
    private int id;

    private int clientId;

    private String errorMessage;

    private double total;

    private List<OrderLineRequest> orderLines;
    
    @Data
    public static class OrderLineRequest {
        private int productId;
        private int quantity;
        private double unitPrice;
    }
}
