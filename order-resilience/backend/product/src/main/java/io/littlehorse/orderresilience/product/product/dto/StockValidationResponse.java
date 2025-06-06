package io.littlehorse.orderresilience.product.product.dto;

import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockValidationResponse {
    private Map<Integer, Integer> confirmedQuantities;
}
