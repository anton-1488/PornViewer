package com.plovdev.pornviewer.gui.panes;

import com.plovdev.pornviewer.databases.FavoriteGroupProvider;
import com.plovdev.pornviewer.databases.FavoriteVideos;
import com.plovdev.pornviewer.databases.UserPreferences;
import com.plovdev.pornviewer.events.listeners.EventListener;
import com.plovdev.pornviewer.events.listeners.FavoriteListener;
import com.plovdev.pornviewer.httpquering.PornParser;
import com.plovdev.pornviewer.httpquering.PornVideoAdapter;
import com.plovdev.pornviewer.httpquering.defimpl.PBPornHandler;
import com.plovdev.pornviewer.models.FavoriteVideo;
import com.plovdev.pornviewer.models.FavoriteVideoInfo;
import com.plovdev.pornviewer.models.VideoInfo;
import com.plovdev.pornviewer.utility.LauncherHelper;
import com.plovdev.pornviewer.utility.json.JSONSerializer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoritePane extends AnchorPane {
    private static final Logger log = LoggerFactory.getLogger(FavoritePane.class);
    private final ObservableList<FavoriteVideo> allFavorites = FXCollections.observableArrayList();
    private final ObservableList<FavoriteVideo> currentList = FXCollections.observableArrayList();
    private final Map<String, List<FavoriteVideo>> groups = new HashMap<>();
    private final ToggleButton allToggle = new ToggleButton("Все");
    protected final PBPornHandler handler = new PBPornHandler();
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    private String CURRENT_TOGGLE = "Все";

    private final ToggleGroup userGroups = new ToggleGroup();
    private final FlowPane pane = new FlowPane(50, 50);


    public FavoritePane() {
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
        CheckBox box3 = new CheckBox("Описания");
        box3.getStyleClass().add("porn-check-box");
        CheckBox box4 = new CheckBox("Дата");
        box4.getStyleClass().add("porn-check-box");
        CheckBox box6 = new CheckBox("Теги");
        box6.getStyleClass().add("porn-check-box");

        Region r1 = new Region();
        HBox.setHgrow(r1, Priority.ALWAYS);

        Region r2 = new Region();
        VBox.setVgrow(r2, Priority.ALWAYS);

        Region r3 = new Region();
        HBox.setHgrow(r3, Priority.ALWAYS);

        Label clear = new Label("✕");
        clear.setVisible(false);
        clear.setOnMousePressed(e -> field.setText(""));
        clear.getStyleClass().add("clear-search");


        Button addGroup = new Button("➕");
        addGroup.getStyleClass().add("add-favorite-group");

        HBox groupsBox = new HBox(10);
        groupsBox.setMinWidth(500);
        groupsBox.getStyleClass().add("groups-box");
        groupsBox.setAlignment(Pos.CENTER_LEFT);

        addGroup.setOnAction(e -> {
            TextInputDialog enterGroup = new TextInputDialog();
            enterGroup.setTitle("Enter group name");
            enterGroup.setContentText("Введите название группы");
            enterGroup.showAndWait().ifPresent(name -> {
                log.info("Name: {}", name);
                if (FavoriteGroupProvider.getAllGroups().contains(name)) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Adding Error");
                    alert.setContentText("Данная группа уже существует.");
                    alert.show();
                    return;
                }
                FavoriteGroupProvider.addGroup(name);
                addFavoriteGroup(name, groupsBox);
                groups.put(name, new CopyOnWriteArrayList<>());
            });
        });

        Button loadAll = new Button("Скачать все");
        loadAll.getStyleClass().add("load-all");
        loadAll.setOnMousePressed(e -> showContextMenu(loadAll, e.getScreenX(), e.getScreenY()));


        ScrollPane groupBoxScroll = new ScrollPane(groupsBox);
        groupBoxScroll.getStyleClass().add("group-box-scroll");
        groupBoxScroll.setFitToHeight(true);
        groupBoxScroll.setFitToWidth(false);
        groupBoxScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        groupBoxScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        groupBoxScroll.prefWidthProperty().bind(widthProperty().divide(1.5));

        HBox controlContainer = new HBox(30, new VBox(10, box1, box3), new VBox(10, box4, box6), r1, new VBox(0, new HBox(r3, loadAll), r2, new HBox(30, groupBoxScroll, addGroup)));
        vBox.getChildren().addAll(new HBox(field, clear), controlContainer);
        vBox.setPadding(new Insets(0, 0, 30, 0));

        Label totalFavorites = new Label("Всего в избранном: " + allFavorites.size());
        root.setTop(new VBox(vBox));

        root.getStyleClass().add("main-pane");
        pane.setAlignment(Pos.TOP_LEFT);
        pane.getStyleClass().add("main-pane-content");
        pane.setPadding(new Insets(10, 10, 50, 10));
        System.out.println("Create flow pane");
        recountChildren();
        System.out.println("Recounted");


        // Favorites groups::[start]
        allToggle.setSelected(true);
        allToggle.getStyleClass().add("favorite-user-toggle");
        allToggle.setToggleGroup(userGroups);
        groupsBox.getChildren().add(allToggle);
        allToggle.selectedProperty().addListener((p1, p2, p3) -> {
            if (p3) {
                CURRENT_TOGGLE = allToggle.getText();
                currentList.setAll(allFavorites);
                Platform.runLater(() -> pane.getChildren().setAll(currentList));
            }
        });
        for (String group : FavoriteGroupProvider.getAllGroups()) {
            addFavoriteGroup(group, groupsBox);
        }
        // Favorites groups::[end]

        field.textProperty().addListener((e1, e2, e3) -> {
            clear.setVisible(!e3.isEmpty());

            List<Pane> panes = new ArrayList<>(currentList);
            panes = panes.stream().filter(e -> {
                if (e instanceof FavoriteVideo card) {
                    return card.getTitle().toLowerCase().contains(e3.trim().toLowerCase());
                }
                return true;
            }).toList();

            pane.getChildren().setAll(panes);
        });

        field.setOnAction(a -> {
            String txt = field.getText();
            if (txt.startsWith("pv://") || txt.startsWith("pornviewer://")) {
                field.setText("");
                LauncherHelper.getInstance().notifyDeepLink(URI.create(txt));
            }
        });

        ScrollPane pornScroll = new ScrollPane(pane);
        pornScroll.setFitToHeight(true);
        pornScroll.setFitToWidth(true);

        root.setCenter(pornScroll);

        getChildren().addAll(root);
        AnchorPane.setLeftAnchor(root, 0.0);
        AnchorPane.setRightAnchor(root, 0.0);
        AnchorPane.setTopAnchor(root, 0.0);
        AnchorPane.setBottomAnchor(root, 0.0);

        FavoriteListener.addListener((videoCard) -> Platform.runLater(() -> {
            log.info("Favorite event: {}", videoCard);
            FavoriteVideo favoriteVideo = copyCard(videoCard);
            if (!videoCard.isFavorite()) {
                pane.getChildren().remove(favoriteVideo);
                allFavorites.remove(favoriteVideo);
                groups.computeIfAbsent(favoriteVideo.getGroup(), s -> new CopyOnWriteArrayList<>()).remove(favoriteVideo);
                EventListener.notifyListeners("favorieStateChanged");
            } else {
                favoriteVideo.render();
                allFavorites.addFirst(favoriteVideo);
                if (CURRENT_TOGGLE.equals(allToggle.getText())) {
                    pane.getChildren().addFirst(favoriteVideo);
                }
            }
        }));
    }

    private FavoriteVideo copyCard(FavoriteVideo videoCard) {
        return new FavoriteVideo(videoCard.getCardId(), videoCard.getTitle(), videoCard.getUrl(), videoCard.getPic(), videoCard.getDuration(), videoCard.getViews(), videoCard.getRating(), videoCard.getInfo(), videoCard.isFavorite(), videoCard.getGroup());
    }

    private void addFavoriteGroup(String name, HBox groupsBox) {
        ToggleButton button = new ToggleButton(name);
        button.getStyleClass().add("favorite-user-toggle");
        button.setToggleGroup(userGroups);

        groupsBox.getChildren().add(button);
        button.selectedProperty().addListener((p1, p2, p3) -> {
            if (p3) {
                CURRENT_TOGGLE = button.getText();
                List<FavoriteVideo> currentGroup = groups.computeIfAbsent(name, k -> new CopyOnWriteArrayList<>());
                currentList.setAll(currentGroup);
                Platform.runLater(() -> pane.getChildren().setAll(currentList));
            }
        });
        MenuItem delete = getMenuItem(name, groupsBox, button);
        ContextMenu contextMenu = new ContextMenu(delete);
        contextMenu.getStyleClass().add("options");
        contextMenu.setOnShowing(e -> {
            if (contextMenu.getScene() != null) {
                contextMenu.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/plovdev/pornviewer/styles/context-menu.css")).toExternalForm());
            }
        });
        button.setContextMenu(contextMenu);
        setupDnDToButton(button, name);
    }

    private MenuItem getMenuItem(String name, HBox groupsBox, ToggleButton button) {
        MenuItem delete = new MenuItem("Удалить");
        delete.setOnAction(e -> {
            FavoriteGroupProvider.removeGroup(name);
            List<FavoriteVideo> toRemove = new CopyOnWriteArrayList<>(groups.get(name));
            groups.remove(name);
            Platform.runLater(() -> groupsBox.getChildren().remove(button));
            toRemove.forEach(video -> FavoriteVideos.update("mark", null, video.getCardId()));
            if (button.getText().equals(CURRENT_TOGGLE)) {
                allToggle.setSelected(true);
            }
        });
        return delete;
    }

    private void setupDnDToButton(ToggleButton button, String group) {
        button.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
                button.getStyleClass().add("drag-drop");
            }
            event.consume();
        });
        button.setOnDragExited(event -> {
            button.getStyleClass().setAll("favorite-user-toggle");
            event.consume();
        });
        button.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                FavoriteVideo video = FavoriteVideo.ofInfo(JSONSerializer.deserialize(db.getString(), FavoriteVideoInfo.class));

                int videoId = video.getCardId();
                String videoGroup = video.getGroup();
                button.getStyleClass().setAll("favorite-user-toggle");

                if (videoGroup != null) {
                    if (videoGroup.equals(group)) {
                        return;
                    }
                    if (containsId(videoId, groups.computeIfAbsent(group, s -> new CopyOnWriteArrayList<>()))) {
                        return;
                    }
                    pane.getChildren().remove(video);
                    groups.computeIfAbsent(videoGroup, s -> new CopyOnWriteArrayList<>()).remove(video);
                }

                FavoriteVideos.update("mark", group, videoId);
                groups.computeIfAbsent(group, s -> new CopyOnWriteArrayList<>()).addFirst(video);
                video.render();
                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    private boolean containsId(int cardId, List<FavoriteVideo> videos) {
        for (FavoriteVideo video : videos) {
            if (video.getCardId() == cardId) {
                return true;
            }
        }
        return false;
    }

    private void recountGroups() {
        for (FavoriteVideo card : allFavorites) {
            String group = card.getGroup();
            if (group != null) {
                List<FavoriteVideo> groupList = groups.computeIfAbsent(group, (s) -> new CopyOnWriteArrayList<>());
                groupList.add(card);
            }
        }
    }

    private void recountChildren() {
        Thread.startVirtualThread(() -> {
            FavoriteVideos.getAll().forEach(e -> {
                e.render();
                allFavorites.add(e);
                Platform.runLater(() -> pane.getChildren().add(e));
            });
            recountGroups();
        });
    }

    private void showContextMenu(Node node, double x, double y) {
        ContextMenu menu = new ContextMenu();
        menu.getStyleClass().add("options");
        for (String str : List.of("1080p", "SD", "LQ", "HQ", "HD")) {
            menu.getItems().add(getLoader(str));
        }
        Platform.runLater(() -> {
            menu.show(node, x, y);
            if (menu.getScene() != null) {
                menu.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/plovdev/pornviewer/styles/context-menu.css")).toExternalForm());
            }
        });
    }

    protected MenuItem getLoader(String qual) {
        MenuItem item = new MenuItem(qual);
        PornVideoAdapter adapter = UserPreferences.get("0000").getPornAdapter();
        PornParser parser = adapter.getParser();
        List<FavoriteVideo> toLoad = new ArrayList<>(currentList);

        item.setOnAction(e -> {
            for (FavoriteVideo video : toLoad) {
                executor.execute(() -> {
                    VideoInfo info = video.getInfo();
                    if (info == null) {
                        info = parser.parseVideo(video.getUrl());
                    }
                    String url = info.getUrls().get(qual);
                    handler.downloadPorn(url, info.getTitle(), info);
                });
            }
        });
        return item;
    }
}