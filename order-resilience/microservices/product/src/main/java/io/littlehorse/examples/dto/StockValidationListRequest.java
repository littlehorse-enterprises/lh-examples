package io.littlehorse.examples.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockValidationListRequest {
    @NotEmpty(message = "Product items cannot be empty")
    @Valid
    private List<ProductStockItem> productItems;
}
