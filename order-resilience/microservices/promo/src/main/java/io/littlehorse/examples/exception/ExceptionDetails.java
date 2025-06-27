package io.littlehorse.examples.exception;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExceptionDetails {
    public int clientId;
    public List<Product> products;
    public String message;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Product {
        public int productId;
        public Integer availableStock;
        public Integer requestedQuantity;
        public String name;
    }
}