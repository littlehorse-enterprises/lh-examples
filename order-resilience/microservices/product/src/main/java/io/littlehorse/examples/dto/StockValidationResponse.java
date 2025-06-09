package io.littlehorse.examples.dto;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockValidationResponse {
    private Map<Integer, Integer> confirmedQuantities;
}
