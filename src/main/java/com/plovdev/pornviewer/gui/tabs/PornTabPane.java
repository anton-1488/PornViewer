package com.plovdev.pornviewer.gui.tabs;

import com.plovdev.pornviewer.databases.UserPreferences;
import com.plovdev.pornviewer.events.listeners.ClickListener;
import com.plovdev.pornviewer.events.listeners.DeepLinkListener;
import com.plovdev.pornviewer.gui.panes.DownloadsPane;
import com.plovdev.pornviewer.gui.panes.FavoritePane;
import com.plovdev.pornviewer.gui.panes.MainMenuPane;
import com.plovdev.pornviewer.gui.panes.ModelsPane;
import com.plovdev.pornviewer.httpquering.PornVideoAdapter;
import com.plovdev.pornviewer.httpquering.Resourcer;
import com.plovdev.pornviewer.httpquering.defimpl.PBPornHandler;
import com.plovdev.pornviewer.models.ModelInfo;
import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PornTabPane extends TabPane {
    private static final Logger log = LoggerFactory.getLogger(PornTabPane.class);
    private final PBPornHandler handler = new PBPornHandler();

    public PornTabPane(Stage stage) {
        getStyleClass().add("porn-tab-pane");
        setSide(Side.BOTTOM);

        tabMinWidthProperty().bind(widthProperty().divide(4.5));
        tabMaxWidthProperty().bind(widthProperty().divide(4.5));

        setTabMinHeight(45);
        setTabMaxHeight(45);

        PornTab main = new PornTab(new Pane(), "Главная");
        PornTab models = new PornTab(new Pane(), "Модели");
        PornTab favorites = new PornTab(new Pane(), "Избранное");
        PornTab downloads = new PornTab(new Pane(), "Загрузки");

        DeepLinkListener.addListener("open", link -> {
            switch (link.getAction()) {
                case "models":
                    getSelectionModel().select(models);
                    break;
                case "favorites":
                    getSelectionModel().select(favorites);
                    break;
                case "downloads":
                    getSelectionModel().select(downloads);
                    break;
            }
        });

        getTabs().addAll(main, models, favorites, downloads);

        try (ExecutorService service = Executors.newCachedThreadPool()) {
            service.execute(() -> {
                MainMenuPane mainMenuPane = new MainMenuPane();
                Platform.runLater(() -> main.setContent(mainMenuPane));
            });

            service.execute(() -> {
                ModelsPane modelsPane = new ModelsPane();
                Platform.runLater(() -> models.setContent(modelsPane));

                DeepLinkListener.addListener("share", link -> {
                    if (link.getAction().equals("model")) {
                        Platform.runLater(() -> getSelectionModel().select(models));
                        PornVideoAdapter adapter = UserPreferences.get("0000").getPornAdapter();
                        Resourcer resourcer = adapter.getResourcer();
                        ModelInfo info = new ModelInfo();
                        info.setUrl(resourcer.baseUrl() + resourcer.modelUrl(link.getParams().get("name")));
                        log.info("Notify model sharing: {}", info.getUrl());
                        ClickListener.notifyListeners(info);
                    }
                });
            });

            service.execute(() -> {
                FavoritePane favoritePane = new FavoritePane();
                Platform.runLater(() -> favorites.setContent(favoritePane));
            });

            service.execute(() -> {
                DownloadsPane downloadsPane = new DownloadsPane();
                Platform.runLater(() -> downloads.setContent(downloadsPane));
            });
            service.shutdown();
        } catch (Exception e) {
            log.error("Initiliazing error: ", e);
        }
    }
}