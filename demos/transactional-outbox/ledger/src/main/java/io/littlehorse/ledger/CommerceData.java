package io.littlehorse.ledger;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommerceData {
  private String gifcard;
  private String sku;
  private BigDecimal price;
  private Integer quantity;
}
