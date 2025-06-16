package io.littlehorse.examples.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse {
    private Long productId;
    private String name;
    private String description;
    private Double price;
    private Double cost;
    private Integer quantity;
    private Integer availableStock;
    private Integer requestedQuantity;
    private String category;
}
