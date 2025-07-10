package io.littlehorse.examples.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductDiscountItem {
    Long productId;
    private double discountPercentage;
}
