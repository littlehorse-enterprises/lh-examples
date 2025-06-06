package io.littlehorse.orderresilience.customer.customer.exceptions;

public class TransactionException extends RuntimeException {
  public TransactionException(String message) {
    super(message);
  }
}
