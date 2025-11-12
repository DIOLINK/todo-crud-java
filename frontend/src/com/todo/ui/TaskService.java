package com.todo.ui;

import java.net.http.*;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class TaskService {
    private final HttpClient client = HttpClient.newHttpClient();
    private static final String BASE_URL = "http://localhost:8080/tasks";

    public CompletableFuture<String> getTasks() {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();
        return client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    public CompletableFuture<Void> addTask(String title) {
        String json = String.format("{\"title\":\"%s\",\"done\":false}", title);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenAccept(r -> {});
    }
}
