package io.littlehorse.orderresilience.customer.customer.exceptions;

public class AmountMismatch extends TransactionException {
  public AmountMismatch(String message) {
    super(message);
  }

}
