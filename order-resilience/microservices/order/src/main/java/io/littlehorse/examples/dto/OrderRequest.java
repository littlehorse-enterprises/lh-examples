package io.littlehorse.examples.dto;

import java.util.List;

import lombok.Data;

@Data
public class OrderRequest {

    private int clientId;

    private List<OrderLineRequest> orderLines;

    private List<String> discountCodes;

    @Data
    public static class OrderLineRequest {
        private int productId;
        private int quantity;
    }
}
