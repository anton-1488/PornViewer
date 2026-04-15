package com.plovdev.pornviewer.utility;

import javafx.stage.Stage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Globals {
    public static final ExecutorService GLOBAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private Globals() {}
    private static Stage primaryStage = null;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}