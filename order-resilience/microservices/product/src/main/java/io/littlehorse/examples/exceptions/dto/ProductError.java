package io.littlehorse.examples.exceptions.dto;

import io.littlehorse.examples.dto.ProductResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class ProductError {
    private long clientId;
    private List<ProductResponse> products ;
    private String message;
}
