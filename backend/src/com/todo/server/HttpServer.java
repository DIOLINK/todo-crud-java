
package com.todo.server;

import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.todo.model.Task;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoClientSettings;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class HttpServer {
    private static final int PORT = 8080;
    private static final Logger logger = Logger.getLogger(HttpServer.class.getName());

    public static void main(String[] args) throws IOException {
        // Configurar codec POJO
        CodecRegistry pojoCodecRegistry = fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        try (MongoClient mongoClient = 
                 MongoClients.create("mongodb://localhost:27017/")) {
            MongoDatabase db = 
                mongoClient.getDatabase("todo_db").withCodecRegistry(pojoCodecRegistry);
            MongoCollection<Task> collection = 
                db.getCollection("tasks", Task.class);

            // Inicializar colección si está vacía
            initializeTasksCollection(collection);

            com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new 
                InetSocketAddress(PORT), 0);
            server.createContext("/tasks", new TaskHandler(collection));
            server.setExecutor(null); // default
            server.start();
            if (logger.isLoggable(Level.INFO)) {
                logger.info(String.format("Servidor HTTP escuchando en puerto %d", PORT));
            }
        }
    }

    // Método para inicializar la colección 'tasks' si está vacía
    private static void initializeTasksCollection(MongoCollection<Task> collection) {
        if (collection.countDocuments() == 0) {
            Task example = new Task(UUID.randomUUID().toString(), "Ejemplo de tarea", false);
            collection.insertOne(example);
            if (logger.isLoggable(Level.INFO)) {
                logger.info("Colección 'tasks' inicializada con una tarea de ejemplo.");
            }
        }
    }
// ...existing code...
}