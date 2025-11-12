package com.todo.ui;

import javafx.application.Application;
import javafx.collections.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.net.http.*;
import java.net.URI;
import java.util.ArrayList;

public class Main extends Application {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String BASE = "http://localhost:8080/tasks";
    private final ObservableList<String> items = 
FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        ListView<String> list = new ListView<>(items);
        TextField input = new TextField();
        Button add = new Button("Add");
        Button refresh = new Button("Refresh");

        add.setOnAction(e -> {
            String title = input.getText().trim();
            if (title.isEmpty()) return;
            String json = "{\"title\":\"" + title + 
"\",\"done\":false}";
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(BASE))
                    .header("Content-Type", "application/json")
                    
.POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            client.sendAsync(req, 
HttpResponse.BodyHandlers.ofString())
                  .thenRun(this::loadTasks);
            input.clear();
        });

        refresh.setOnAction(e -> loadTasks());

        VBox root = new VBox(10, input, new HBox(10, add, 
refresh), list);
        root.setStyle("-fx-padding:10");
        stage.setScene(new Scene(root, 400, 300));
        stage.setTitle("To-Do JavaFX");
        stage.show();

        loadTasks();
    }

    private void loadTasks() {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE))
                .GET()
                .build();
        client.sendAsync(req, 
HttpResponse.BodyHandlers.ofString())
              .thenApply(HttpResponse::body)
              .thenAccept(this::parseAndShow);
    }

    private void parseAndShow(String json) {
        // Parse super-simple
        items.clear();
        String[] parts = json.split("\\},\\{");
        for (String p : parts) {
            String title = extract(p, "title");
            String done = extract(p, "done");
            items.add(title + (done.equals("true") ? " ✅" : " ❌"));
        }
    }

    private String extract(String json, String key) {
        String k = "\"" + key + "\":";
        int idx = json.indexOf(k);
        if (idx == -1) return "";
        int start = json.indexOf("\"", idx + k.length()) + 1;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    public static void main(String[] args) {
        launch(args);
    }
}