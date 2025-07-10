package io.littlehorse.ledger.transaction.exceptions;

public class TransactionException extends RuntimeException {
  public TransactionException(String message) {
    super(message);
  }
}
