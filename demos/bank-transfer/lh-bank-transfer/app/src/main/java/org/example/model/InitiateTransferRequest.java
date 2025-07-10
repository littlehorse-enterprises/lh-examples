package org.example.model;

public class InitiateTransferRequest {
    public String fromAccountId;
    public String toAccountId;
    public String currency;
    public Double amount;
    public String description;

    // For idempotency: add a transfer id if you want.
    public String idempotencyId;
}
