package io.littlehorse.examples.dto;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
@Builder
public class DispatchErrorResponse {
    private long clientId;
    private List<ProductResponse> products ;
    private String message;
}
