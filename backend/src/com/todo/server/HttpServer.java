package com.todo.server;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import com.todo.model.Task;
import org.bson.Document;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import static com.mongodb.client.model.Updates.*;
import com.mongodb.MongoClientSettings;
import org.bson.codecs.pojo.PojoCodecProvider;
import static 
org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static 
org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;

public class HttpServer {
    private static final int PORT = 8080;
    private static MongoCollection<Task> collection;

    public static void main(String[] args) throws IOException {
        // Configurar codec POJO
    CodecRegistry pojoCodecRegistry = fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(),
        fromProviders(PojoCodecProvider.builder().automatic(true).build()));
    MongoClientSettings settings = 
        MongoClientSettings.builder()
            .codecRegistry(pojoCodecRegistry).build();

    try (MongoClient mongoClient = 
             MongoClients.create(settings)) {
        MongoDatabase db = 
            mongoClient.getDatabase("todo_db");
        collection = 
            db.getCollection("tasks", Task.class);

        com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new 
            InetSocketAddress(PORT), 0);
        server.createContext("/tasks", new TaskHandler());
        server.setExecutor(null); // default
        server.start();
        System.out.println("Servidor HTTP escuchando en puerto " + PORT);
    }
    }

    static class TaskHandler implements HttpHandler {
        private static final String TASKS_PATH = "/tasks/";
        
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String method = ex.getRequestMethod();
            String path = ex.getRequestURI().getPath();
            String response = "";
            int status = 200;

            try {
                switch (method) {
                    case "GET":
                        if (path.equals("/tasks")) {
                            List<Task> list = new ArrayList<>();
                            // Aquí deberías poblar la lista y asignar response
                        } else if (path.startsWith(TASKS_PATH)) {
                            String id = path.substring(TASKS_PATH.length());
                            Task t = collection.find(Filters.eq("_id", id)).first();
                            response = t == null ? "{}" : toJson(t);
                        }
                        break;
                    case "POST":
                        String body = read(ex.getRequestBody());
                        Task t = fromJson(body, Task.class);
                        t.setId(UUID.randomUUID().toString());
                        collection.insertOne(t);
                        // Falta asignar response si es necesario
                        break;
                    case "PUT":
                        String idPut = path.substring(TASKS_PATH.length());
                        String bodyPut = read(ex.getRequestBody());
                        Task u = fromJson(bodyPut, Task.class);
                        collection.replaceOne(Filters.eq("_id", idPut), u);
                        // Falta asignar response si es necesario
                        break;
                    case "DELETE":
                        String idDel = path.substring(TASKS_PATH.length());
                        collection.deleteOne(Filters.eq("_id", idDel));
                        response = "{}";
                        break;
                    default:
                        status = 405;
                }
            } catch (Exception e) {
                status = 500;
                response = "{\"error\":\"" + e.getMessage() + "\"}";
            }

            ex.getResponseHeaders().add("Content-Type", 
"application/json");
            ex.sendResponseHeaders(status, 
response.getBytes().length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(response.getBytes());
            }
        }

        private String read(InputStream is) throws IOException {
sb.append(line);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }

        private String toJson(Object obj) {
            // JSON super-simple (solo para demo)
            if (obj instanceof Task) {
                Task t = (Task) obj;
                return 
String.format("{\"id\":\"%s\",\"title\":\"%s\",\"done\":%b}",
                        t.getId(), t.getTitle(), t.isDone());
            }
            if (obj instanceof List) {
                StringBuilder sb = new StringBuilder("[");
                List<?> list = (List<?>) obj;
                for (int i = 0; i < list.size(); i++) {
                    sb.append(toJson(list.get(i)));
                    if (i < list.size() - 1) sb.append(",");
                }
                sb.append("]");
                return sb.toString();
            }
            return "{}";
        }

        private Task fromJson(String json) {
            // Parse super-simple
            String id = extract(json, "id");
            String title = extract(json, "title");
            boolean done = Boolean.parseBoolean(extract(json, "done"));
            return new Task(id, title, done);
        }

        private String extract(String json, String key) {
            String k = "\"" + key + "\":";
            int idx = json.indexOf(k);
            if (idx == -1) return "";
            int start = json.indexOf("\"", idx + k.length()) + 1;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        }
    }
}