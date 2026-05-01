package com.plovdev.pornviewer.gui.filters;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TrianglePaginationBlock extends VBox {
    private final Button toStart = new Button("В начало");
    private final Button back = new Button("Назад 0");
    private final Button next = new Button("1 Вперед");


    public TrianglePaginationBlock(Runnable r1, Runnable r2, Runnable r3) {
        super(5);

        toStart.setOnAction(e -> r1.run());
        back.setOnAction(e -> r2.run());
        next.setOnAction(e -> r3.run());

        toStart.getStyleClass().add("pagination-button");
        back.getStyleClass().add("pagination-button");
        next.getStyleClass().add("pagination-button");

        toStart.prefWidthProperty().bind(widthProperty());
        back.prefWidthProperty().bind(widthProperty().divide(2));
        next.prefWidthProperty().bind(widthProperty().divide(2));

        setPadding(new Insets(10));
        getChildren().addAll(toStart, new HBox(5, back, next));
    }

    public void setOnToStart(Runnable r) {
        toStart.setOnAction(e -> r.run());
    }
    public void setOnBack(Runnable r) {
        back.setOnAction(e -> r.run());
    }
    public void setOnNext(Runnable r) {
        next.setOnAction(e -> r.run());
    }

    public Button getToStart() {
        return toStart;
    }

    public Button getBack() {
        return back;
    }

    public Button getNext() {
        return next;
    }
}