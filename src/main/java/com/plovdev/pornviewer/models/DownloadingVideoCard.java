package com.plovdev.pornviewer.models;

import com.plovdev.pornviewer.encryptionsupport.videoparser.read.PVVFVideoReader;
import com.plovdev.pornviewer.events.FileDownloadingEvent;
import com.plovdev.pornviewer.events.listeners.FileDownloadingListener;
import com.plovdev.pornviewer.gui.video.DurationUtils;
import com.plovdev.pornviewer.utility.DialogShower;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class DownloadingVideoCard extends DownloadedVideoCard {
    private static final Logger log = LoggerFactory.getLogger(DownloadingVideoCard.class);
    private final ProgressBar progressBar = new ProgressBar(0.0);
    private final DateTimeFormatter createFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private final DoubleProperty progress = new SimpleDoubleProperty(0.0);
    private final LongProperty total = new SimpleLongProperty(0);
    private final LongProperty loaded = new SimpleLongProperty(0);

    public DownloadingVideoCard(Pane component) {
        super(component);
        addListeners();
    }

    @Override
    public void render() {
        getChildren().clear();
        AnchorPane anchorPane = new AnchorPane();
        progressBar.progressProperty().bind(progress);
        minWidthProperty().bind(pane.widthProperty().divide(1.03));
        prefWidthProperty().bind(pane.widthProperty().divide(1.03));
        maxWidthProperty().bind(pane.widthProperty().divide(1.03));
        anchorPane.minWidthProperty().bind(pane.widthProperty().divide(1.03));
        anchorPane.prefWidthProperty().bind(pane.widthProperty().divide(1.03));
        anchorPane.maxWidthProperty().bind(pane.widthProperty().divide(1.03));

        ImageView view = new ImageView(Objects.requireNonNull(getClass().getResource("/com/plovdev/pornviewer/download.png")).toExternalForm());
        view.setFitHeight(70);
        view.setFitWidth(70);
        view.setPreserveRatio(true);
        AnchorPane.setLeftAnchor(view, 20.0);
        AnchorPane.setBottomAnchor(view, 5.0);
        AnchorPane.setTopAnchor(view, 25.0);
        anchorPane.getChildren().add(view);

        progressBar.getStyleClass().add("porn-downloading-bar");
        progressBar.setPrefHeight(7.5);
        anchorPane.getChildren().add(progressBar);
        AnchorPane.setTopAnchor(progressBar, 0.0);
        AnchorPane.setLeftAnchor(progressBar, 0.0);
        AnchorPane.setRightAnchor(progressBar, 0.0);

        Hyperlink title = new Hyperlink(getTitle());
        title.getStyleClass().add("video-title-download");
        AnchorPane.setTopAnchor(title, 10.0);
        AnchorPane.setLeftAnchor(title, 120.0);
        anchorPane.getChildren().add(title);

        Label durLabel = new Label("0:00");
        durLabel.getStyleClass().add("marker-download");
        AnchorPane.setBottomAnchor(durLabel, 10.0);
        AnchorPane.setLeftAnchor(durLabel, 120.0);
        anchorPane.getChildren().add(durLabel);


        Label dateLabel = new Label(getDate());
        dateLabel.getStyleClass().add("marker-download");
        AnchorPane.setTopAnchor(dateLabel, 10.0);
        AnchorPane.setRightAnchor(dateLabel, 40.0);
        anchorPane.getChildren().add(dateLabel);

        Label sizeLabel = new Label(size + "MB");
        sizeLabel.getStyleClass().add("marker-download");
        AnchorPane.setBottomAnchor(sizeLabel, 10.0);
        AnchorPane.setRightAnchor(sizeLabel, 40.0);
        anchorPane.getChildren().add(sizeLabel);

        Label delete = new Label("×");
        delete.getStyleClass().add("delete-label");
        delete.setOnMousePressed(e -> DialogShower.showConfirm("Удалить видео?", deleteRun));
        AnchorPane.setBottomAnchor(delete, 45.0);
        AnchorPane.setTopAnchor(delete, 45.0);
        AnchorPane.setRightAnchor(delete, 10.0);
        anchorPane.getChildren().add(delete);

        getStyleClass().add("download-video-card");

        HBox smoothBox = new HBox();
        smoothBox.getStyleClass().add("smooth-box");
        smoothBox.setPrefHeight(100);
        StackPane.setAlignment(smoothBox, Pos.BOTTOM_CENTER);
        StackPane.setMargin(smoothBox, new Insets(7.5, 0, 0, 0));

        smoothBox.minWidthProperty().bind(pane.widthProperty().divide(1.03));
        smoothBox.prefWidthProperty().bind(pane.widthProperty().divide(1.03));
        smoothBox.maxWidthProperty().bind(pane.widthProperty().divide(1.03));

        StackPane stackPane = new StackPane(anchorPane, smoothBox);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(stackPane.widthProperty());
        clip.heightProperty().bind(stackPane.heightProperty());
        clip.setArcWidth(60);
        clip.setArcHeight(60);
        stackPane.setClip(clip);

        progressBar.prefWidthProperty().bind(stackPane.widthProperty());

        getChildren().add(stackPane);
    }

    private void addListeners() {
        FileDownloadingListener.addListener(new FileDownloadingEvent() {
            private final DownloadedVideoCard downloadedVideoCard = new DownloadedVideoCard(pane);
            @Override
            public void fileDownloading(long downloadedBytes) {
                loaded.set(downloadedBytes);
                if (total.get() > 0) {
                    progress.set((double) downloadedBytes / total.get());
                } else {
                    progress.set(0.0);
                }
            }

            @Override
            public void onDownloadFinishing(File file) {
                System.err.println("End");
                try {
                    ObservableList<Node> nodes = pane.getChildren();
                    downloadedVideoCard.setPath(file.toURI().toString());
                    BigDecimal size = new BigDecimal(String.valueOf(file.length())).divide(new BigDecimal("1000000.0"), 10, RoundingMode.HALF_UP);
                    DecimalFormat format = new DecimalFormat("#0.00MB");
                    downloadedVideoCard.setSize(format.format(size));

                    try {
                        DownloadedVideoInfo videoInfo = PVVFVideoReader.readInfo(file);
                        downloadedVideoCard.setTitle(videoInfo.getTitle());
                        downloadedVideoCard.setDuration(DurationUtils.formatDurationToString(videoInfo.getTotalDuration()));
                        downloadedVideoCard.setDescription(videoInfo.getDescription());
                        downloadedVideoCard.setPreview(videoInfo.getPreviewBytes());
                    } catch (Exception e) {
                        log.error("Error read metadata: {}", e.getMessage());
                    }

                    Platform.runLater(() -> {
                        downloadedVideoCard.render();
                        pane.getChildren().set(getById(nodes), downloadedVideoCard);
                    });
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }

            @Override
            public void onDownloadStrarting(long totalBytes) {
                total.set(totalBytes);
                try {
                    LocalDateTime dateTime = LocalDateTime.now();
                    downloadedVideoCard.setDate(dateTime.format(createFormatter));
                    setDate(dateTime.format(createFormatter));
                    downloadedVideoCard.setTitle(getTitle());
                    setTitle(getTitle());

                    downloadedVideoCard.setDeleteRun(() -> {
                        downloadedVideoCard.setSelf(false);
                        try {
                            Files.delete(Path.of(URI.create(getOriginalPath())));
                            pane.getChildren().forEach(fp -> {
                                if (fp instanceof VideoCard vc) {
                                    if (vc.getTitle().equals(getTitle()))
                                        Platform.runLater(() -> pane.getChildren().remove(fp));
                                }
                            });
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                        }
                    });
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }

                Platform.runLater(() -> {
                    render();
                    pane.getChildren().addFirst(DownloadingVideoCard.this);
                });
            }

            @Override
            public void onError(Exception e) {
                progressBar.getStyleClass().add("porn-downloading-bar-error");
                System.err.println(e.getMessage());
            }
        });
    }

    private int getById(List<Node> nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            if (n instanceof DownloadingVideoCard dc) {
                if (dc.getTitle().equals(getTitle())) {
                    return i;
                }
            }
        }
        return 0;
    }
}