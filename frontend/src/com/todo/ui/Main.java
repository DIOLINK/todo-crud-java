package com.todo.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.todo.ui.TaskFormatter;

import java.util.logging.Logger;

public class Main extends Application {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private final TaskService taskService = new TaskService();
    private final ObservableList<String> items = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        UIComponents ui = setupUI(stage);
        setupActions(ui);
        ui.add.setDisable(true);
        ui.refresh.setDisable(true);
        loadTasks(ui.add, ui.refresh);
    }

    private static class UIComponents {
        ListView<String> list;
        TextField input;
        Button add;
        Button refresh;
        Button delete;
    }

    private UIComponents setupUI(Stage stage) {
        UIComponents ui = new UIComponents();
        ui.list = new ListView<>(items);
        ui.input = new TextField();
        ui.add = new Button("Add");
        ui.refresh = new Button("Refresh");
        ui.delete = new Button("Delete");

        HBox buttons = new HBox(10, ui.add, ui.refresh, ui.delete);
        VBox root = new VBox(10, ui.input, buttons, ui.list);
        root.setStyle("-fx-padding:10");
        stage.setScene(new Scene(root, 400, 300));
        stage.setTitle("To-Do JavaFX");
        stage.show();
        return ui;
    }

    private void setupActions(UIComponents ui) {
        ui.add.setOnAction(e -> {
            String title = ui.input.getText().trim();
            logger.info("[DEBUG] Botón 'Add' presionado. Título ingresado: '" + title + "'");
            if (title.isEmpty()) {
                logger.info("[DEBUG] Título vacío, no se agrega tarea.");
                return;
            }
            ui.add.setDisable(true);
            ui.refresh.setDisable(true);
            taskService.addTask(title)
                .thenRun(() -> {
                    logger.info("[DEBUG] Tarea agregada. Recargando lista de tareas...");
                    loadTasks(ui.add, ui.refresh);
                });
            ui.input.clear();
        });

        ui.refresh.setOnAction(e -> {
            logger.info("[DEBUG] Botón 'Refresh' presionado. Recargando lista de tareas...");
            ui.add.setDisable(true);
            ui.refresh.setDisable(true);
            loadTasks(ui.add, ui.refresh);
        });

        ui.delete.setOnAction(e -> {
            String selected = ui.list.getSelectionModel().getSelectedItem();
            if (selected == null || selected.isBlank()) {
                logger.info("[DEBUG] No hay tarea seleccionada para borrar.");
                return;
            }
            // Extraer el título (antes del primer espacio o emoji)
            String title = selected;
            int idx = title.lastIndexOf(" ❌");
            if (idx == -1) idx = title.lastIndexOf(" ✅");
            if (idx != -1) title = title.substring(0, idx);
            logger.info("[DEBUG] Botón 'Delete' presionado. Título a borrar: '" + title + "'");
            ui.delete.setDisable(true);
            taskService.deleteTask(title)
                .thenRun(() -> {
                    logger.info("[DEBUG] Tarea borrada. Recargando lista de tareas...");
                    Platform.runLater(() -> ui.delete.setDisable(false));
                    loadTasks(ui.add, ui.refresh);
                });
        });
    }

    private void loadTasks(Button add, Button refresh) {
        logger.info("[DEBUG] Solicitando tareas al servicio...");
        taskService.getTasks()
            .thenAccept(json -> {
                logger.info("[DEBUG] Respuesta JSON recibida: " + json);
                parseAndShow(json, add, refresh);
            });
    }

    private void parseAndShow(String json, Button add, Button refresh) {
        Platform.runLater(() -> {
            logger.info("[DEBUG] Mostrando tareas en la UI...");
            if (json != null && json.trim().startsWith("{\"error\"")) {
                logger.warning("[ERROR] Respuesta de error recibida: " + json);
                items.clear();
                add.setDisable(false);
                refresh.setDisable(false);
                return;
            }
            java.util.List<String> parsed = parseTasks(json);
            logger.info("[DEBUG] Tareas parseadas: " + parsed);
            items.setAll(parsed);
            add.setDisable(false);
            refresh.setDisable(false);
        });
    }

    // Devuelve una lista de strings para mostrar en la UI a partir del JSON recibido
    private java.util.List<String> parseTasks(String json) {
        java.util.List<String> result = new java.util.ArrayList<>();
        if (json == null || json.isBlank()) {
            logger.info("[DEBUG] JSON vacío o nulo en parseTasks");
            return result;
        }
        // Quitar corchetes y dividir objetos
        String arr = json.trim();
        logger.info("[DEBUG] JSON para parsear: " + arr);
        if (arr.startsWith("[")) arr = arr.substring(1);
        if (arr.endsWith("]")) arr = arr.substring(0, arr.length()-1);
        if (arr.isBlank()) return result;
        String[] parts = arr.split("},\\{");
        for (String p : parts) {
            String obj = p;
            if (!obj.startsWith("{")) obj = "{" + obj;
            if (!obj.endsWith("}")) obj = obj + "}";
            String title = extractField(obj, "title");
            Boolean done = extractBooleanField(obj, "done");
            if (logger.isLoggable(java.util.logging.Level.INFO)) {
                logger.info(String.format("[DEBUG] Tarea encontrada: title='%s', done='%s'", title, done));
            }
            String formatted = TaskFormatter.format(title, done != null ? done.toString() : null);
            if (!formatted.isBlank()) {
                result.add(formatted);
            }
        }
        return result;
    }

    // Extrae el valor de un campo string del JSON plano
    private String extractField(String json, String key) {
        String k = "\"" + key + "\":";
        int idx = json.indexOf(k);
        if (idx == -1) return null;
        int start = idx + k.length();
        // Si el valor empieza con comillas, es string
        if (json.charAt(start) == '"') {
            start++;
            int end = json.indexOf('"', start);
            if (end == -1) return null;
            return json.substring(start, end);
        } else {
            // Si no, es booleano o número
            int end = json.indexOf(',', start);
            if (end == -1) end = json.indexOf('}', start);
            if (end == -1) return null;
            return json.substring(start, end).trim();
        }
    }

    // Extrae el valor booleano de un campo del JSON plano
    private Boolean extractBooleanField(String json, String key) {
    String val = extractField(json, key);
    if (val == null) return Boolean.FALSE;
    if (val.equalsIgnoreCase("true")) return Boolean.TRUE;
    if (val.equalsIgnoreCase("false")) return Boolean.FALSE;
    return Boolean.FALSE;
    }


    public static void main(String[] args) {
        launch(args);
    }
}