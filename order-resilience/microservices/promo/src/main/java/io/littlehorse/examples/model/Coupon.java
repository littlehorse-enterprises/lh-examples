package io.littlehorse.examples.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {
    @Id
    Long clientId;
    @Id
    Long productId;
    private String code;
    private String description;
    private double discountPercentage;
    private boolean isActive;
}
