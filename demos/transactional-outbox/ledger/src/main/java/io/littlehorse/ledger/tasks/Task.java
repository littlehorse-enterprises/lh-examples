package io.littlehorse.ledger.tasks;

import java.math.BigDecimal;
import java.util.UUID;

import io.littlehorse.ledger.NotificationsService;
import io.littlehorse.ledger.transaction.Transaction;
import io.littlehorse.ledger.transaction.TransactionService;

public abstract class Task {
  protected String worker;
  protected TransactionService transactionService;
  protected NotificationsService notificationsService;

  protected Task(TransactionService transactionService, NotificationsService notificationsServices, String worker) {
    this.transactionService = transactionService;
    this.notificationsService = notificationsServices;
    this.worker = worker;
  }

  protected String debit(String account, Integer amount, String idempodencyKey) {
    Transaction transaction = transactionService.debit(account, BigDecimal.valueOf(amount), idempodencyKey);
    publishTransaction(transaction);
    return transaction.getId().toString();
  }

  protected String debit(String account, Double amount, String idempodencyKey) {
    Transaction transaction = transactionService.debit(account, BigDecimal.valueOf(amount), idempodencyKey);
    publishTransaction(transaction);
    return transaction.getId().toString();
  }

  protected String credit(String account, BigDecimal amount, String idempotencyKey) {
    Transaction transaction = transactionService.credit(account, amount, idempotencyKey);
    publishTransaction(transaction);
    return transaction.getId().toString();
  }

  protected String revert(String transactionId) {
    Transaction transaction = transactionService.revert(UUID.fromString(transactionId));
    publishTransaction(transaction);
    return transaction.getId().toString();
  }

  private void publishTransaction(Transaction transaction) {
    notificationsService.publishTransaction(worker, transaction);
  }
}
