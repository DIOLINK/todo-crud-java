package com.todo.ui;

public class TaskFormatter {
    private TaskFormatter() {}

    public static String format(String title, String done) {
        if (title == null || title.isBlank()) return "";
        return title + ("true".equals(done) ? " ✅" : " ❌");
    }
}
