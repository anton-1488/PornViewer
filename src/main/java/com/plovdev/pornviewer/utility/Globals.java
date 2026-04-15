package com.plovdev.pornviewer.utility;

import javafx.stage.Stage;

public final class Globals {
    private Globals() {}
    private static Stage primaryStage = null;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}