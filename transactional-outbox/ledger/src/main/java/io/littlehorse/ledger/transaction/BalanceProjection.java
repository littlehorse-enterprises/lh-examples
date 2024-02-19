package io.littlehorse.ledger.transaction;

import java.math.BigDecimal;

public interface BalanceProjection {
  String getAccount();

  BigDecimal getBalance();
}
