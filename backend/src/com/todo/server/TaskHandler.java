package com.todo.server;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.todo.model.Task;
import com.mongodb.client.MongoCollection;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;
import java.util.logging.Level;

public class TaskHandler implements HttpHandler {
    private final MongoCollection<Task> collection;
    private static final Logger logger = Logger.getLogger(TaskHandler.class.getName());

    public TaskHandler(MongoCollection<Task> collection) {
        this.collection = collection;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Request recibido en /tasks: " + method);
        }
        if ("GET".equalsIgnoreCase(method)) {
            handleGet(exchange);
        } else {
            String response = "{\"error\":\"Método no soportado\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(405, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("[");
        boolean first = true;
        for (Task t : collection.find()) {
            if (!first) json.append(",");
            json.append("{\"title\":\"")
                .append(escapeJson(t.getTitle()))
                .append("\",\"done\":\"")
                .append(t.isDone())
                .append("\"}");
            first = false;
        }
        json.append("]");
        String response = json.toString();
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    // Escapa caracteres especiales para JSON simple
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    }
}