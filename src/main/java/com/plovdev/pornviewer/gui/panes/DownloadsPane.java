package com.plovdev.pornviewer.gui.panes;

import com.plovdev.pornviewer.encryptionsupport.videoparser.read.PVVFParser;
import com.plovdev.pornviewer.encryptionsupport.videoparser.read.PVVFVideoReader;
import com.plovdev.pornviewer.events.listeners.EventListener;
import com.plovdev.pornviewer.gui.filters.FilterBox;
import com.plovdev.pornviewer.gui.video.DurationUtils;
import com.plovdev.pornviewer.models.DownloadedCardInfo;
import com.plovdev.pornviewer.models.DownloadedVideoCard;
import com.plovdev.pornviewer.models.DownloadedVideoInfo;
import com.plovdev.pornviewer.models.DownloadingVideoCard;
import com.plovdev.pornviewer.utility.Globals;
import com.plovdev.pornviewer.utility.LauncherHelper;
import com.plovdev.pornviewer.utility.deeplink.DeepLinker;
import com.plovdev.pornviewer.utility.files.FileUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class DownloadsPane extends AnchorPane {
    private static final Logger log = LoggerFactory.getLogger(DownloadsPane.class);
    private final ObservableList<DownloadedVideoCard> originNots = FXCollections.observableArrayList();

    private final DateTimeFormatter createFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private final FlowPane pane = new FlowPane(50, 50);

    public DownloadsPane() {
        BorderPane root = new BorderPane();
        VBox vBox = new VBox(10);

        vBox.getStyleClass().add("vbox");
        TextField field = new TextField();
        field.getStyleClass().add("porn-search");
        field.prefWidthProperty().bind(widthProperty().divide(1.2));
        field.setPrefHeight(35);

        field.setPromptText("Поиск...");

        CheckBox box1 = new CheckBox("Названия");
        box1.getStyleClass().add("porn-check-box");
        CheckBox box3 = new CheckBox("Длительность");
        box3.getStyleClass().add("porn-check-box");
        CheckBox box4 = new CheckBox("Дата");
        box4.getStyleClass().add("porn-check-box");
        CheckBox box6 = new CheckBox("Размер");
        box6.getStyleClass().add("porn-check-box");

        Region r1 = new Region();
        HBox.setHgrow(r1, Priority.ALWAYS);

        FilterBox filterBox = new FilterBox(pane);
        filterBox.setPrefSize(300, 100);

        Label clear = new Label("✕");
        clear.setVisible(false);
        clear.setOnMousePressed(e -> field.setText(""));
        clear.getStyleClass().add("clear-search");

        vBox.getChildren().addAll(new HBox(field, clear), new HBox(30, new VBox(10, box1, box3), new VBox(10, box4, box6), r1));
        vBox.setPadding(new Insets(0, 0, 30, 0));
        root.setTop(new VBox(vBox));

        root.getStyleClass().add("main-pane");
        pane.setAlignment(Pos.TOP_LEFT);
        pane.getStyleClass().add("main-pane-content");
        pane.setPadding(new Insets(10, 10, 50, 10));
        runPornParsing(pane);

        field.textProperty().addListener((e1, e2, e3) -> {
            clear.setVisible(!e3.isEmpty());
            if (!(e3.startsWith("pv://") || e3.startsWith("pornviewer://"))) {
                List<Pane> panes = new ArrayList<>(originNots);
                panes = panes.stream().filter(e -> {
                    if (e instanceof DownloadingVideoCard card) {
                        return card.getTitle().toLowerCase().contains(e3.trim().toLowerCase());
                    }
                    return true;
                }).toList();

                pane.getChildren().setAll(panes);
            }
        });
        DeepLinker.bindAutocompleteToSearchFiled(field);
        field.setOnAction(a -> {
            String txt = field.getText();
            if (txt.startsWith("pv://") || txt.startsWith("pornviewer://")) {
                field.setText("");
                LauncherHelper.getInstance().notifyDeepLink(URI.create(txt));
            }
        });


        ScrollPane pornScroll = new ScrollPane(pane);
        pornScroll.getStyleClass().add("porn-scroll");
        pornScroll.setFitToHeight(true);
        pornScroll.setFitToWidth(true);

        root.setCenter(pornScroll);

        getChildren().addAll(root);
        AnchorPane.setLeftAnchor(root, 0.0);
        AnchorPane.setRightAnchor(root, 0.0);
        AnchorPane.setTopAnchor(root, 0.0);
        AnchorPane.setBottomAnchor(root, 0.0);

        EventListener.addListener(e -> {
            if (e.startsWith("START_DWONLOAD:")) {
                String name = e.substring(e.indexOf(':') + 1);
                DownloadingVideoCard card = new DownloadingVideoCard(pane);
                card.setTitle(name);
            }
        });
    }

    private void runPornParsing(FlowPane pane) {
        Thread.startVirtualThread(getParseTask(pane));
    }

    @Contract(pure = true)
    private @NotNull Runnable getParseTask(FlowPane pane) {
        return () -> {
            originNots.clear();
            Platform.runLater(() -> pane.getChildren().clear());
            FileUtils fileUtils = new FileUtils();
            try (Stream<Path> stream = Files.list(fileUtils.getPvDownloadsPath()).filter(Files::isRegularFile).filter(PVVFParser::isPVVFFile)) {
                stream.forEach(p -> {
                    CompletableFuture<DownloadedCardInfo> cardFuture = prepareCard(p);
                    cardFuture.thenAccept(info -> {
                        DownloadedVideoCard card = DownloadedVideoCard.ofInfo(info, pane);
                        card.render();
                        originNots.add(card);
                        Platform.runLater(() -> pane.getChildren().add(card));
                    });
                });
            } catch (Exception e) {
                log.error("Error parsing task: ", e);
            }
        };
    }

    @Contract("_ -> new")
    private @NotNull CompletableFuture<DownloadedCardInfo> prepareCard(Path p) {
        return CompletableFuture.supplyAsync(() -> {
            File file = p.toFile();
            DownloadedVideoInfo videoInfo = PVVFVideoReader.readInfo(file);

            String date = "00.00.0000";
            try {
                BasicFileAttributes attributes = Files.readAttributes(p, BasicFileAttributes.class);
                FileTime time = attributes.creationTime();
                LocalDateTime dateTime = LocalDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault());
                date = dateTime.format(createFormatter);
            } catch (Exception e) {
                log.error("Error to read atrtibutes: ", e);
            }

            String path = file.toURI().toString();
            String size = String.format("%.2fMB", file.length() / 1000000.0);
            String title = videoInfo.getTitle();
            String duration = DurationUtils.formatDurationToString(videoInfo.getTotalDuration());
            String description = videoInfo.getDescription();
            byte[] preview = videoInfo.getPreviewBytes();
            List<DownloadedVideoInfo.Timecode> timecodes = videoInfo.getTimecodes();

            return new DownloadedCardInfo(title, path, size, date, duration, description, timecodes, preview);
        }, Globals.GLOBAL_EXECUTOR);
    }
}