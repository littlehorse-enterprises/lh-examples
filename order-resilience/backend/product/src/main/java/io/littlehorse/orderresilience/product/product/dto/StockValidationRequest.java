package io.littlehorse.orderresilience.product.product.dto;

import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockValidationRequest {
    @NotEmpty(message = "Product quantities cannot be empty")
    private Map<Integer, @NotNull @Min(1) Integer> productQuantities;
}
