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
        } else if ("POST".equalsIgnoreCase(method)) {
            handlePost(exchange);
        } else {
            String response = "{\"error\":\"Método no soportado\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(405, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
    private void handlePost(HttpExchange exchange) throws IOException {
        try {
            String body = new String(exchange.getRequestBody().readAllBytes());
            logger.info("[DEBUG] handlePost: body=" + body);
            // Parsear JSON manualmente (simple)
            String title = extractJsonString(body, "title");
            boolean done = extractJsonBoolean(body, "done");
            if (title == null || title.isBlank()) {
                String response = "{\"error\":\"Título requerido\"}";
                exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(400, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }
            Task task = new Task(java.util.UUID.randomUUID().toString(), title, done);
            collection.insertOne(task);
            String response = "{\"ok\":true}";
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(201, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            logger.info("[DEBUG] handlePost: tarea agregada: " + title);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "[ERROR] handlePost: excepción: " + ex.getMessage(), ex);
            String response = "{\"error\":\"Error interno\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(500, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    // Métodos utilitarios para parsear JSON simple
    private String extractJsonString(String json, String key) {
        String k = "\"" + key + "\":";
        int idx = json.indexOf(k);
        if (idx == -1) return null;
        int start = json.indexOf('"', idx + k.length());
        if (start == -1) return null;
        start++;
        int end = json.indexOf('"', start);
        if (end == -1) return null;
        return json.substring(start, end);
    }
    private boolean extractJsonBoolean(String json, String key) {
        String k = "\"" + key + "\":";
        int idx = json.indexOf(k);
        if (idx == -1) return false;
        int start = idx + k.length();
        // Saltar espacios y dos puntos
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '"')) start++;
        int end = json.indexOf(',', start);
        if (end == -1) end = json.indexOf('}', start);
        if (end == -1) end = json.length();
        String val = json.substring(start, end).replace("\"", "").trim();
        return val.equalsIgnoreCase("true");
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        try {
            logger.info("[DEBUG] handleGet: inicio");
            StringBuilder json = new StringBuilder();
            json.append("[");
            boolean first = true;
            int count = 0;
            for (Task t : collection.find()) {
                if (!first) json.append(",");
                json.append("{\"title\":\"")
                    .append(escapeJson(t.getTitle()))
                    .append("\",\"done\":")
                    .append(t.isDone())
                    .append("}");
                first = false;
                count++;
            }
            logger.info("[DEBUG] handleGet: tareas encontradas=" + count);
            json.append("]");
            String response = json.toString();
            logger.info("[DEBUG] handleGet: JSON generado=" + response);
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            logger.info("[DEBUG] handleGet: respuesta enviada, bytes=" + response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "[ERROR] handleGet: excepción: " + ex.getMessage(), ex);
            String response = "{\"error\":\"Error interno\"}";
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(500, response.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    // Escapa caracteres especiales para JSON simple
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
