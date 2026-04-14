package com.plovdev.pornviewer.gui.video;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;
import javafx.stage.PopupWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;

public class TimecodeView extends Circle {
    private static final Logger log = LoggerFactory.getLogger(TimecodeView.class);
    private String text;
    private Duration time;
    private Runnable onChecked = () -> {
    };

    public TimecodeView(String text, Duration time) {
        super(5, Color.WHITE);
        toFront();
        this.text = text;
        this.time = time;

        setMouseTransparent(false);

        Tooltip tooltip = new Tooltip();
        tooltip.setContentDisplay(ContentDisplay.BOTTOM);
        tooltip.setTextAlignment(TextAlignment.CENTER);
        tooltip.setAnchorLocation(PopupWindow.AnchorLocation.CONTENT_BOTTOM_LEFT);
        tooltip.setShowDelay(javafx.util.Duration.millis(0));
        tooltip.setHideDelay(javafx.util.Duration.seconds(3));
        tooltip.setText(text);
        Tooltip.install(this, tooltip);

        setOnMouseClicked(a -> onChecked.run());
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Duration getTime() {
        return time;
    }

    public void setTime(Duration time) {
        this.time = time;
    }

    public Runnable getOnChecked() {
        return onChecked;
    }

    public void setOnChecked(Runnable onChecked) {
        this.onChecked = Objects.requireNonNull(onChecked);
    }
}