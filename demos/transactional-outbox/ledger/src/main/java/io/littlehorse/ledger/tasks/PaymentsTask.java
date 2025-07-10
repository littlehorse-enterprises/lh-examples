package io.littlehorse.ledger.tasks;

import org.springframework.orm.jpa.JpaSystemException;

import io.littlehorse.ledger.NotificationsService;
import io.littlehorse.ledger.transaction.Transaction;
import io.littlehorse.ledger.transaction.TransactionService;
import io.littlehorse.sdk.common.exception.LHTaskException;
import io.littlehorse.sdk.worker.LHTaskMethod;
import io.littlehorse.sdk.worker.WorkerContext;

public class PaymentsTask extends Task {
  public PaymentsTask(TransactionService transactionService, NotificationsService notificationsServices) {
    super(transactionService, notificationsServices, "payments");
  }

  @LHTaskMethod("process-payment")
  public String processPayment(String account, Double amount, WorkerContext context) throws Exception {
    String idempotencyKey = context.getIdempotencyKey();
    try {
      return this.debit(account, amount, idempotencyKey);
    } catch (JpaSystemException e) {
      // Business exception
      this.notificationsService.publishTransaction("payment.failures",
          new Transaction(account, amount, idempotencyKey));
      throw new LHTaskException("not-enough-balance", "account doesn't have enough balance");
    }
    // Technical exceptions aren't handled
  }

  @LHTaskMethod("issue-refund")
  public String issueRefund(String transactionId) throws Exception {
    return this.revert(transactionId);
  }
}
