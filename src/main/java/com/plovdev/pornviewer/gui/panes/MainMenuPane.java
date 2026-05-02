package com.plovdev.pornviewer.gui.panes;

import com.plovdev.pornviewer.databases.UserPreferences;
import com.plovdev.pornviewer.events.listeners.FavoriteListener;
import com.plovdev.pornviewer.events.listeners.PornUpdateListener;
import com.plovdev.pornviewer.gui.filters.CategoryManager;
import com.plovdev.pornviewer.gui.filters.TrianglePaginationBlock;
import com.plovdev.pornviewer.gui.panes.pagination.MainPagination;
import com.plovdev.pornviewer.gui.utils.DeepSearcher;
import com.plovdev.pornviewer.httpquering.PornChecker;
import com.plovdev.pornviewer.httpquering.PornParser;
import com.plovdev.pornviewer.httpquering.PornVideoAdapter;
import com.plovdev.pornviewer.httpquering.Resourcer;
import com.plovdev.pornviewer.httpquering.defimpl.PBPornHandler;
import com.plovdev.pornviewer.models.VideoCard;
import com.plovdev.pornviewer.pornimpl.porn365.DefPornParser;
import com.plovdev.pornviewer.utility.LauncherHelper;
import com.plovdev.pornviewer.utility.deeplink.DeepLinker;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainMenuPane extends AnchorPane {
    private static final Logger log = LoggerFactory.getLogger(MainMenuPane.class);
    private final ObservableList<VideoCard> originNots = FXCollections.observableArrayList();
    private final PornVideoAdapter adapter = UserPreferences.get("0000").getPornAdapter();
    private final Resourcer resourcer = adapter.getResourcer();
    private final PornChecker checker = adapter.getChecker();
    private final PBPornHandler handler = new PBPornHandler();
    private static final double TRIGGER_THRESHOLD = 300.0;
    private final StringProperty CURRENT_URL = new SimpleStringProperty(resourcer.baseUrl());
    private double totalDeltaY = 0;

    public MainMenuPane() {
        FlowPane pane = new FlowPane(50, 50);

        BorderPane root = new BorderPane();
        VBox vBox = new VBox(10);

        Button categ = new Button("K");
        categ.getStyleClass().add("categories");

        vBox.getStyleClass().add("vbox");
        TextField field = new TextField();
        DeepLinker.bindAutocompleteToSearchFiled(field);

        field.getStyleClass().add("porn-search");
        field.prefWidthProperty().bind(widthProperty().divide(1.2));
        field.setPrefHeight(35);

        field.setPromptText("Поиск...");

        CheckBox box1 = new CheckBox("Названия");
        box1.getStyleClass().add("porn-check-box");
        CheckBox box3 = new CheckBox("Описания");
        box3.getStyleClass().add("porn-check-box");
        CheckBox box4 = new CheckBox("Дата");
        box4.getStyleClass().add("porn-check-box");
        CheckBox box6 = new CheckBox("Теги");
        box6.getStyleClass().add("porn-check-box");

        Region r1 = new Region();
        HBox.setHgrow(r1, Priority.ALWAYS);

        Label clear = new Label("✕");
        clear.setVisible(false);
        clear.setOnMousePressed(e -> field.setText(""));
        clear.getStyleClass().add("clear-search");

        Button deepSaarch = new Button("Глубокий поиск");
        deepSaarch.setVisible(false);
        deepSaarch.getStyleClass().add("deep-search-button");
        deepSaarch.setOnAction(e -> {
            deepSaarch.setDisable(true);
            try {
                TextInputDialog keywordsInput = new TextInputDialog();
                keywordsInput.setTitle("Input");
                keywordsInput.setHeaderText("Input keywords");
                keywordsInput.setContentText("Введите ключевые слова для поиска через ','");
                keywordsInput.showAndWait().ifPresent(string -> {
                    originNots.clear();
                    String[] keywords = string.toLowerCase().replace(" ", "").split(",");
                    String url = resourcer.baseUrl() + resourcer.searchUrl() + URLEncoder.encode(field.getText(), Charset.defaultCharset()) + "/popular/";
                    DeepSearcher.searchVideo(new DefPornParser(), url, Arrays.stream(keywords).toList(), card -> {
                        card.render();
                        originNots.add(card);
                        Platform.runLater(() -> pane.getChildren().add(card));
                    });
                });
            } catch (Exception ex) {
                log.error("Error while deep search: ", ex);
            } finally {
                deepSaarch.setDisable(false);
            }
        });


        TrianglePaginationBlock block = new TrianglePaginationBlock(null, null, null);
        block.setTranslateX(38);
        block.setTranslateY(35);
        block.prefWidthProperty().bind(widthProperty().divide(5));

        Region space = new Region();
        space.setPrefWidth(30);

        vBox.getChildren().addAll(new HBox(field, clear, space, deepSaarch), new HBox(30, new VBox(10, box1, box3), new VBox(10, box4, box6), r1, block, categ));
        vBox.setPadding(new Insets(0, 0, 30, 0));
        root.setTop(new VBox(vBox));

        CategoryManager manager = new CategoryManager(resourcer);
        AnchorPane.setBottomAnchor(manager, 0.0);
        AnchorPane.setTopAnchor(manager, 0.0);
        AnchorPane.setRightAnchor(manager, 0.0);

        manager.resize(this);
        categ.setOnAction(e -> manager.toggle());

        root.getStyleClass().add("main-pane");
        pane.setAlignment(Pos.TOP_LEFT);
        pane.getStyleClass().add("main-pane-content");
        pane.setPadding(new Insets(10, 10, 50, 10));
        runPornParsing(pane, resourcer.baseUrl());

        MainPagination pagination = new MainPagination(pane, block);

        field.setOnAction(e -> {
            if (!checker.canSearch()) return;
            String txt = field.getText();
            if (txt.startsWith("pv://") || txt.startsWith("pornviewer://")) {
                field.setText("");
                LauncherHelper.getInstance().notifyDeepLink(URI.create(txt));
                return;
            }
            if (!txt.isEmpty()) {
                String url = resourcer.baseUrl() + resourcer.searchUrl() + URLEncoder.encode(field.getText(), Charset.defaultCharset()) + "/popular";
                runPornParsing(pane, url);
                CURRENT_URL.setValue(url);
            }
        });
        field.textProperty().addListener((e1, e2, e3) -> {
            clear.setVisible(!e3.isEmpty());
            deepSaarch.setVisible(!e3.isEmpty());
            if (!(e3.startsWith("pv://") || e3.startsWith("pornviewer://"))) {
                List<Pane> panes = new ArrayList<>(originNots);
                panes = panes.stream().filter(e -> {
                    if (e instanceof VideoCard card) {
                        return card.getTitle().toLowerCase().contains(e3.trim().toLowerCase());
                    }
                    return true;
                }).toList();

                pane.getChildren().setAll(panes);
            }
        });

        CURRENT_URL.addListener((p1, p2, p3) -> {
            pagination.reset();
            pagination.setBaseUrl(CURRENT_URL.getValue());
        });

        ScrollPane pornScroll = new ScrollPane(pane);
        pornScroll.getStyleClass().add("porn-scroll");
        pornScroll.setFitToHeight(true);
        pornScroll.setFitToWidth(true);

        pornScroll.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (pornScroll.getVvalue() <= 0.01 && e.getDeltaY() > 0) {
                if (e.isInertia()) {
                    totalDeltaY = 0;
                    pane.setTranslateY(0);
                    return;
                }
                updateDelta(pane, e.getDeltaY());
                if (totalDeltaY >= TRIGGER_THRESHOLD) {
                    runPornParsing(pane, CURRENT_URL.getValue());
                    totalDeltaY = 0;
                    pane.setTranslateY(0);
                    e.consume();
                }
            } else {
                totalDeltaY = 0;
                pane.setTranslateY(0);
            }
        });

        root.setCenter(pornScroll);

        getChildren().addAll(root, manager);
        AnchorPane.setLeftAnchor(root, 0.0);
        AnchorPane.setRightAnchor(root, 0.0);
        AnchorPane.setTopAnchor(root, 0.0);
        AnchorPane.setBottomAnchor(root, 0.0);

        PornUpdateListener.addListener((e, t) -> {
            if (t == 0) {
                runPornParsing(pane, e);
                pagination.setBaseUrl(e);
                CURRENT_URL.setValue(e);
            }
        });

        FavoriteListener.addListener(c -> {
            if (!c.isFavorite()) {
                int id = c.getCardId();
                pane.getChildren().forEach(crd -> {
                    if (crd instanceof VideoCard videoCard && videoCard.getCardId() == id) {
                        videoCard.setFavorite(false);
                    }
                });
            }
        });
    }

    private double calculateRubberPull(double delta) {
        if (delta <= 0) return 0;
        double normalized = Math.min(delta / TRIGGER_THRESHOLD, 2.0);
        if (normalized <= 1.0) {
            return TRIGGER_THRESHOLD * 0.4 * Math.pow(normalized, 1.5);
        } else {
            double extra = normalized - 1.0;
            return TRIGGER_THRESHOLD * 0.4 + TRIGGER_THRESHOLD * 0.3 * Math.log1p(extra);
        }
    }

    private void updateDelta(@NotNull FlowPane pane, double delta) {
        totalDeltaY += delta;
        pane.setTranslateY(calculateRubberPull(totalDeltaY));
    }


    private void runPornParsing(@NotNull FlowPane pane, String url) {
        pane.getChildren().clear();
        originNots.clear();
        Thread.startVirtualThread(getParseTask(pane, url));
    }

    @Contract(pure = true)
    private @NotNull Runnable getParseTask(FlowPane pane, String url) {
        return () -> {
            try {
                PornParser pornParser = adapter.getParser();
                List<VideoCard> cards = pornParser.getAllVideos(handler.requestPorn(url));
                cards.parallelStream().forEach(e -> {
                    e.render();
                    originNots.add(e);
                    Platform.runLater(() -> pane.getChildren().add(e));
                });
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        };
    }
}