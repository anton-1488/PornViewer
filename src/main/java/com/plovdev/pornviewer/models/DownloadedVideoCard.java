package com.plovdev.pornviewer.models;

import com.plovdev.pornviewer.gui.toast.Filer;
import com.plovdev.pornviewer.gui.video.DownloadedVideoPlayerPane;
import com.plovdev.pornviewer.utility.files.ServerPaths;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.util.Objects;

public class DownloadedVideoCard extends VideoCard {
    private static final Logger log = LoggerFactory.getLogger(DownloadedVideoCard.class);
    protected String path;
    protected String size;
    private String date;
    private String description;
    private byte[] preview;
    protected boolean isSelf = false;

    public boolean isSelf() {
        return isSelf;
    }

    public void setSelf(boolean self) {
        isSelf = self;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public byte[] getPreview() {
        return preview;
    }

    public void setPreview(byte[] preview) {
        this.preview = preview;
    }

    protected final Pane pane;
    protected Runnable deleteRun;

    public DownloadedVideoCard(Pane component) {
        pane = component;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Runnable getDeleteRun() {
        return deleteRun;
    }

    public void setDeleteRun(Runnable deleteRun) {
        this.deleteRun = deleteRun;
    }

    public Pane getPane() {
        return pane;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getPath() {
        return ServerPaths.getInstance().replaceFileToHttpPath(path);
    }

    public String getOriginalPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return getPath();
    }

    @Override
    public void render() {
        try {
            StackPane mainContainer = new StackPane();
            mainContainer.setMaxWidth(400);

            Label sizeLabel = new Label(String.valueOf(size));
            sizeLabel.getStyleClass().add("marker");

            Hyperlink titleLabel = new Hyperlink(title);
            titleLabel.setOnAction(e -> {
                DownloadedVideoPlayerPane playerPane = new DownloadedVideoPlayerPane(this);
                playerPane.show();
            });
            titleLabel.getStyleClass().add("video-title");
            titleLabel.setMaxWidth(400);
            titleLabel.setMinHeight(50);

            Label dur = new Label(duration);
            dur.getStyleClass().add("marker");

            VBox box = new VBox(titleLabel);
            box.getStyleClass().add("trailer");

            try {
                Image image = SwingFXUtils.toFXImage(ImageIO.read(new ByteArrayInputStream(preview)), null);
                renderImage(image, box);
            } catch (Exception e) {
                log.error("Error to load preview: ", e);
                Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/plovdev/pornviewer/download.png")));
                renderImage(image, box);
            }

            Region region = getHRegion();
            Region r = getVRegion();
            Region r2 = getVRegion();
            Region r1 = getHRegion();

            Label dateLabel = new Label(date);
            dateLabel.getStyleClass().add("marker");

            Label actions = new Label("|||");
            actions.getStyleClass().add("options");
            fillActionsBox(actions);

            VBox vBox = new VBox(new HBox(dateLabel, r1, actions), r, new HBox(sizeLabel, region, dur));
            vBox.setPadding(new Insets(10));
            HBox.setHgrow(vBox, Priority.ALWAYS);

            HBox infoOverlay = new HBox(vBox);
            infoOverlay.setVisible(false);
            infoOverlay.getStyleClass().add("video-hover");
            StackPane.setAlignment(infoOverlay, Pos.BOTTOM_CENTER);
            StackPane.setMargin(infoOverlay, new Insets(0, 0, 50, 0));
            mainContainer.getChildren().addAll(box, infoOverlay);

            mainContainer.setOnMouseEntered(e -> infoOverlay.setVisible(true));
            mainContainer.setOnMouseExited(e -> infoOverlay.setVisible(false));
            getChildren().add(mainContainer);
        } catch (Exception e) {
            log.error("Rendering error: ", e);
        }
    }

    private void renderImage(Image image, VBox box) {
        ImageView view = new ImageView(image);
        view.setSmooth(true);
        view.getStyleClass().add("video-preview");
        view.setFitWidth(420);
        view.setFitHeight(250);
        view.setPreserveRatio(true);
        Platform.runLater(() -> box.getChildren().addFirst(view));
    }

    private void fillActionsBox(Label actions) {
        ContextMenu menu = new ContextMenu();
        menu.getStyleClass().add("options");
        fillActinsMenu(menu);

        actions.setOnMouseClicked(e -> Platform.runLater(() -> {
            menu.show(actions, e.getScreenX(), e.getScreenY());
            if (menu.getScene() != null) {
                menu.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/plovdev/pornviewer/styles/context-menu.css")).toExternalForm());
            }
        }));

    }

    private void fillActinsMenu(ContextMenu menu) {
        MenuItem delete = new MenuItem("Удалить");
        delete.setOnAction(a -> deleteRun.run());

        MenuItem export = new MenuItem("Экспортировать");
        export.setOnAction(a -> {
            Filer filer = new Filer();
            if (filer.getPath() != null) {
                handler.executePost(ServerPaths.getInstance().getInfoUrl(), filer.getPath());
            }
        });

        menu.getItems().addAll(delete, export);
    }

    private Region getVRegion() {
        Region region = new Region();
        VBox.setVgrow(region, Priority.ALWAYS);
        return region;
    }

    private Region getHRegion() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }
}