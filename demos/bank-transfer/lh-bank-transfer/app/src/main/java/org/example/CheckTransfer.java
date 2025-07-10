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

public class CheckTransfer {
    private static final Logger log = LoggerFactory.getLogger(CheckTransfer.class);

    @LHTaskMethod("check-transfer")
    public Map<String, Object> checkTransfer(String transferId) throws Exception {

        HttpClient client = HttpClient.newBuilder().build();

        String apiUrl = "http://localhost:7070/transferStatus/" + transferId;
        log.info("Sending " + apiUrl);
        // Create an HTTP GET request
        HttpRequest request =
                HttpRequest.newBuilder().uri(URI.create(apiUrl)).GET().build();

        // Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Print the response status code and body
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
