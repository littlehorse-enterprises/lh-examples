package io.littlehorse.ledger.tasks;

import org.springframework.orm.jpa.JpaSystemException;

import io.littlehorse.ledger.NotificationsService;
import io.littlehorse.ledger.transaction.TransactionService;
import io.littlehorse.sdk.common.exception.LHTaskException;
import io.littlehorse.sdk.worker.LHTaskMethod;

public class WarehouseTask extends Task {
  public WarehouseTask(TransactionService transactionService, NotificationsService notificationsServices) {
    super(transactionService, notificationsServices, "warehouse");
  }

  @LHTaskMethod("ship-item")
  public String shipItem(String sku, Integer amount, String idempotencyKey) throws Exception {
    try {
      return this.debit(sku, amount, idempotencyKey);
    } catch (JpaSystemException e) {
      throw new LHTaskException("out-of-stock", "not enough stock to ship item");
    } catch (Exception e) {
      throw new LHTaskException("unknown-error", "something really bad happened");
    }
  }
}
