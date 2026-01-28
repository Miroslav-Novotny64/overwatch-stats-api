package org.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OverwatchAPI {
    private static final String BASE_URL = "https://overfast-api.tekrop.fr";
    private final HttpClient client;

    public OverwatchAPI() {
        this.client = HttpClient.newHttpClient();
    }

    public String getPlayerProfile(String battletag) throws Exception {
        String url = BASE_URL + "/players/" + battletag;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        return response.body();
    }
}