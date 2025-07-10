package io.littlehorse.examples.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.littlehorse.examples.deserializer.CustomDateDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private int orderId;
    private int clientId;
    private String message;
    private double total;
    private String status = "PENDING";
    private List<OrderLineResponse> orderLines;
    private List<String> discountCodes;

    @JsonDeserialize(using = CustomDateDeserializer.class)
    private Date creationDate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderLineResponse {
        private int productId;
        private int quantity;
        private double unitPrice;
        private double discountPercentage;
        private double totalPrice;
    }
}
