package org.example;

import io.littlehorse.sdk.worker.LHTaskMethod;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitiateTransfer {
    private static final Logger log = LoggerFactory.getLogger(InitiateTransfer.class);
    private final HttpClient client = HttpClient.newHttpClient();

    @LHTaskMethod("initiate-transfer")
    public Map<String, Object> initiateTransfer(
            String fromAccountId, String toAccountId, Double amount, String currency, String description)
            throws Exception {

        HttpClient client = HttpClient.newBuilder().build();

        var json = new JSONObject();
        json.put("fromAccountId", fromAccountId);
        json.put("toAccountId", toAccountId);
        json.put("amount", amount);
        json.put("currency", currency);
        json.put("description", description);
        log.info("Sending " + json.toString());
        var request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:7070/transfer"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            log.info("Response " + response.body());
            return new JSONObject(response.body()).toMap();

        } else {
            log.info("Error: " + response.statusCode());
            log.info("Response Body: " + response.body());
            throw new IOException("Failed to initiate transfer: HTTP error code " + response.statusCode());
        }
    }
}
