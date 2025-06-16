package io.littlehorse.examples.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductPriceItem {
    Long productId;
    private double unitPrice;
}
