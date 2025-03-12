package org.example;

import io.littlehorse.sdk.worker.LHTaskMethod;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import javax.net.ssl.*;
import org.json.JSONObject;

public class FetchAccount {

    @LHTaskMethod("fetch-account")
    public Map<String, Object> fetchAccount(String accountId) throws Exception {

        HttpClient client = HttpClient.newBuilder().build();

        // URL of the API you want to call
        String apiUrl = "http://localhost:7070/account/" + accountId;

        // Create an HTTP GET request
        HttpRequest request =
                HttpRequest.newBuilder().uri(URI.create(apiUrl)).GET().build();

        // Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Print the response status code and body
        if (response.statusCode() == 200) {
            return new JSONObject(response.body()).toMap();
        } else {
            throw new IOException("Failed to fetch account: HTTP error code " + response.statusCode());
        }
    }
}
