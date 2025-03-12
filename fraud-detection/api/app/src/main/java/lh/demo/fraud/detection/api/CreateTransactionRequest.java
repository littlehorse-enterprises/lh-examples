package lh.demo.fraud.detection.api;

public record CreateTransactionRequest(String sourceAccount, String destinationAccount, Integer amount) {}
