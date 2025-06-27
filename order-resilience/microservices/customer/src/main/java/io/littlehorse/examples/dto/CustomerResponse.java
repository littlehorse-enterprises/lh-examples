package io.littlehorse.examples.dto;

import java.util.UUID;

import io.littlehorse.examples.models.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private Long id;
    private String name;
    private String email;
    private String description;
    private CustomerType type;
}
