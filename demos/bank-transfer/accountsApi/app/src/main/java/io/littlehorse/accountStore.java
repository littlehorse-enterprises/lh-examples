package io.littlehorse;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class accountStore {
    private List<JSONObject> documents;

    public accountStore() {
        this.documents = new ArrayList<>();
    }

    // Method to add a new JSON document from a JSONObject
    public void addDocument(JSONObject document) {
        this.documents.add(document);
    }

    // Method to add a new JSON document from a JSON string
    public void addDocument(String jsonString) {
        try {
            JSONObject document = new JSONObject(jsonString);
            this.documents.add(document);
        } catch (JSONException e) {
            System.out.println("Invalid JSON format: " + e.getMessage());
        }
    }

    // Method to retrieve all documents
    public List<JSONObject> getAllDocuments() {
        return new ArrayList<>(documents); // Return a copy to prevent modification
    }

    public JSONObject queryByField(String field, Object value) {

        for (JSONObject doc : documents) {
            if (value.equals(doc.opt(field))) {
                return doc;
            }
        }
        return null; // Return null if no match is found
    }

    // Method to query documents by a field, supporting partial match for strings
    public List<JSONObject> queryByFieldPartialMatch(String field, String partialValue) {
        return documents.stream()
                .filter(doc -> doc.has(field)
                        && doc.get(field) instanceof String
                        && ((String) doc.get(field)).contains(partialValue))
                .collect(Collectors.toList());
    }

    // Method to remove a document by its index
    public void removeDocument(int index) {
        if (index >= 0 && index < documents.size()) {
            documents.remove(index);
        }
    }
    // Test data that is loaded into the demo
    public void loadTestData() {
        Random random = new Random();
        String acct1 =
                "{\"accountId\": \"1234\", \"name\": \"mitch\", \"accountBalance\": \"123.45\", \"accountStatus\": \"ACTIVE\", \"accountType\": \"CHECKING\" }";
        String acct2 =
                "{\"accountId\": \"4564\", \"name\": \"Bob\", \"accountBalance\": \"12.00\", \"accountStatus\": \"ACTIVE\", \"accountType\": \"SAVINGS\" }";
        String acct3 =
                "{\"accountId\": \"7892\", \"name\": \"Alice\", \"accountBalance\": \"1238.88\", \"accountStatus\": \"ACTIVE\", \"accountType\": \"SAVINGS\" }";
        String acct4 =
                "{\"accountId\": \"1248\", \"name\": \"Joe\", \"accountBalance\": \"42.42\", \"accountStatus\": \"ACTIVE\", \"accountType\": \"CHECKING\" }";
        String acct5 =
                "{\"accountId\": \"4769\", \"name\": \"Mike\", \"accountBalance\": \"7677.64\", \"accountStatus\": \"ACTIVE\", \"accountType\": \"CHECKING\" }";
        String acct6 =
                "{\"accountId\": \"7502\", \"name\": \"Nick\", \"accountBalance\": \"950.12\", \"accountStatus\": \"ACTIVE\", \"accountType\": \"SAVINGS\" }";
        String acct7 =
                "{\"accountId\": \"93254\", \"name\": \"Keri\", \"accountBalance\": \"876.23\", \"accountStatus\": \"NON_ACTIVE\", \"accountType\": \"SAVINGS\" }";
        String acct8 =
                "{\"accountId\": \"7491\", \"name\": \"Mandy\", \"accountBalance\": \"14.89\", \"accountStatus\": \"NON_ACTIVE\", \"accountType\": \"SAVINGS\" }";
        String acct9 =
                "{\"accountId\": \"7570\", \"name\": \"Test1\", \"accountBalance\": \"18237.84\", \"accountStatus\": \"ACTIVE\", \"accountType\": \"CHECKING\" }";
        String acct10 = "{\"accountId\": \"" + random.nextInt(2000, 20000)
                + "\", \"name\": \"Test2\", \"accountBalance\": \"" + getRandomDouble(0.00, 20000.00)
                + "\", \"accountStatus\": \"ACTIVE\", \"accountType\": \"CHECKING\"}";
        String acct11 = "{\"accountId\": \"" + random.nextInt(2000, 20000)
                + "\", \"name\": \"Test3\", \"accountBalance\": \"" + getRandomDouble(0.00, 20000.00)
                + "\", \"accountStatus\": \"ACTIVE\", \"accountType\": \"CHECKING\"}";
        this.addDocument(acct1);
        this.addDocument(acct2);
        this.addDocument(acct3);
        this.addDocument(acct4);
        this.addDocument(acct5);
        this.addDocument(acct6);
        this.addDocument(acct7);
        this.addDocument(acct8);
        this.addDocument(acct9);
        this.addDocument(acct10);
        this.addDocument(acct11);
    }
    // used to generate account balances for test accounts
    public static double getRandomDouble(double min, double max) {
        double randomDouble = ThreadLocalRandom.current().nextDouble(min, max);

        return Math.round(randomDouble * 100.0) / 100.0;
    }

    @Override
    public String toString() {
        JSONArray array = new JSONArray(documents);
        return array.toString(4);
    }
}
