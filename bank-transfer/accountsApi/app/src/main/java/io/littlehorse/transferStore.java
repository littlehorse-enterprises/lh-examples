package io.littlehorse;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.json.JSONObject;

public class transferStore {
    private Map<UUID, JSONObject> transactions;

    public transferStore() {
        this.transactions = new HashMap<>();
    }

    // Method to create a transaction and return its UUID
    // TODO: actually remove and add the money.
    public UUID addTransaction(JSONObject transaction) {
        // Validate the transaction fields
        if (!transaction.has("fromAccountId")
                || !transaction.has("toAccountId")
                || !transaction.has("amount")
                || !transaction.has("currency")
                || !transaction.has("description")) {
            throw new IllegalArgumentException(
                    "Transaction must contain fromAccountId, toAccountId, amount, and currency fields.");
        }
        Long createTime = System.currentTimeMillis();
        Long completeTime = createTime + ThreadLocalRandom.current().nextLong(1, 10000);
        transaction.put("status", "PENDING");
        transaction.put("createdAt", createTime);
        transaction.put("updatedAt", "");
        transaction.put("error", "");
        // Generate a unique UUID for this transaction
        UUID transactionId = UUID.randomUUID();
        transaction.put("transferId", transactionId);
        // Don't THIS IN THE getTransaction
        transaction.put("completeTime", completeTime);
        transactions.put(transactionId, transaction);
        return transactionId;
    }

    // Method to retrieve a transaction by its UUID
    public String getTransaction(UUID transactionId) {
        JSONObject transaction = transactions.get(transactionId);
        Long completeTime = transaction.getLong("completeTime");
        if (completeTime < System.currentTimeMillis()) {
            if (transaction.get("status") == "PENDING") {
                String newStatus = getTaskStatus();
                // update transactions(store) & the return result
                JSONObject transBody = transactions.get(transactionId);
                transBody.put("status", newStatus);
                transactions.put(transactionId, transBody);
                // update the result
                transaction.put("status", newStatus);
            }
        } else {
            System.out.println("didn't meet complete time requirements.  Current time - " + System.currentTimeMillis()
                    + " scheduled completed time -  " + transaction.get("completeTime"));
        }

        return transaction != null ? transaction.toString() : null;
    }

    // Method to retrieve all transactions with their UUIDs
    // This is for testing and demo purposes
    public JSONObject getAllTransactions() {
        JSONObject allTransactions = new JSONObject();
        for (Map.Entry<UUID, JSONObject> entry : transactions.entrySet()) {
            allTransactions.put(entry.getKey().toString(), entry.getValue());
        }
        return allTransactions;
    }

    // Used for setting random transactions to COMPLETED or FAILED.
    // 1 out of 3 transactions should fail for demo purposes.
    public static String getTaskStatus() {
        Random random = new Random();

        // Generate a random number between 0 and 2
        int randomNumber = random.nextInt(3); // random number between 0, 1, and 2

        if (randomNumber == 0) {
            return "FAILED"; // 1/3rd of the time
        } else {
            return "COMPLETED"; // 2/3rds of the time
        }
    }
}
