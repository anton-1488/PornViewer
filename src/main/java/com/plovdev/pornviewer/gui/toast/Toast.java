package com.plovdev.pornviewer.gui.toast;

import com.plovdev.pornviewer.utility.Globals;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Toast {
    private static final Logger log = LoggerFactory.getLogger(Toast.class);

    private final Popup popup;
    private final Stage stage;
    private final StackPane pane;

    private boolean isPlayAnim = true;
    private int animDelay = 300;
    private int toastDelay = 2000;

    public int getToastDelay() {
        return toastDelay;
    }

    public void setToastDelay(int toastDelay) {
        this.toastDelay = toastDelay;
    }

    public int getAnimDelay() {
        return animDelay;
    }

    public void setAnimDelay(int animDelay) {
        this.animDelay = animDelay;
    }

    public Toast(Stage stage, String text) {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Not fx application thread!");
        }

        this.stage = stage;
        popup = new Popup();

        Label label = new Label(text);

        pane = new StackPane(label);
        pane.getStyleClass().add("toast-notify");
        popup.getContent().setAll(pane);

        popup.setAutoFix(true);
        popup.setAutoHide(true);
        popup.setConsumeAutoHidingEvents(true);

    }

    public static void showToast(String text) {
        Platform.runLater(() -> {
            Toast toast = new Toast(Globals.getPrimaryStage() == null ? new Stage() : Globals.getPrimaryStage(), text);
            toast.show();
        });
    }

    public Popup getPopup() {
        return popup;
    }

    public Stage getStage() {
        return stage;
    }

    public StackPane getPane() {
        return pane;
    }

    public boolean isPlayAnim() {
        return isPlayAnim;
    }

    public void setPlayAnim(boolean playAnim) {
        isPlayAnim = playAnim;
    }

    public void show() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Not fx application thread!");
        }

        popup.show(stage);
        popup.setX(stage.getX() + stage.getWidth() / 2 - pane.getWidth() / 2);
        popup.setY(stage.getY() + stage.getHeight() * 0.8);

        if (isPlayAnim) {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(animDelay), pane);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }

        PauseTransition closePopup = new PauseTransition(Duration.millis(toastDelay));
        closePopup.setOnFinished(a -> {
            if (isPlayAnim) {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(animDelay), pane);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> popup.hide());
                fadeOut.play();
            } else {
                popup.hide();
            }
        });
        closePopup.play();
    }
}