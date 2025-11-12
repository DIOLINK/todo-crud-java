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
        // Aquí va la lógica de manejo de requests (GET, POST, etc.)
        String response = "TaskHandler operativo";
        exchange.sendResponseHeaders(200, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Request recibido en /tasks");
        }
    }
}