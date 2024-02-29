package lh.demo.fraud.detection.api.task;

import io.littlehorse.sdk.worker.LHTaskMethod;
import java.util.UUID;
import lh.demo.fraud.detection.api.Transaction;
import lh.demo.fraud.detection.api.TransactionRepository;

public class TransactionTasks {

    public static final String SAVE_TRANSACTION = "save-transaction";
    public static final String APPROVE_TRANSACTION = "approve-transaction";
    public static final String REJECT_TRANSACTION = "reject-transaction";
    public static final String DETECT_FRAUD = "detect-fraud";

    private final TransactionRepository repository;

    public TransactionTasks(TransactionRepository repository) {
        this.repository = repository;
    }

    @LHTaskMethod(SAVE_TRANSACTION)
    public String saveTransaction(String sourceAccount, String destinationAccount, Integer amount) {
        Transaction transaction = new Transaction(sourceAccount, destinationAccount, amount, "PENDING");
        return repository.save(transaction).getId().toString();
    }

    @LHTaskMethod(APPROVE_TRANSACTION)
    public void approveTransaction(String transactionId) {
        Transaction transaction =
                repository.findById(UUID.fromString(transactionId)).get();
        transaction.setStatus("APPROVED");
        repository.save(transaction);
    }

    @LHTaskMethod(DETECT_FRAUD)
    public boolean detectFraud(String transactionId) {
        Transaction transaction =
                repository.findById(UUID.fromString(transactionId)).get();
        return transaction.getAmount().equals(1000);
    }

    @LHTaskMethod(REJECT_TRANSACTION)
    public void rejectTransaction(String transactionId) {
        Transaction transaction =
                repository.findById(UUID.fromString(transactionId)).get();
        transaction.setStatus("REJECTED");
        repository.save(transaction);
    }
}
