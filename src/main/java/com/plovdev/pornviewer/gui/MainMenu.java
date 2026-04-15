package com.plovdev.pornviewer.gui;

import com.plovdev.pornviewer.databases.FavoriteVideos;
import com.plovdev.pornviewer.databases.UserPreferences;
import com.plovdev.pornviewer.events.listeners.DeepLinkListener;
import com.plovdev.pornviewer.events.listeners.FavoriteListener;
import com.plovdev.pornviewer.gui.tabs.PornTabPane;
import com.plovdev.pornviewer.gui.toast.Toast;
import com.plovdev.pornviewer.gui.video.VideoPlayerPane;
import com.plovdev.pornviewer.gui.video.DurationUtils;
import com.plovdev.pornviewer.httpquering.PornParser;
import com.plovdev.pornviewer.httpquering.PornVideoAdapter;
import com.plovdev.pornviewer.httpquering.Resourcer;
import com.plovdev.pornviewer.httpquering.defimpl.PBPornHandler;
import com.plovdev.pornviewer.models.FavoriteVideo;
import com.plovdev.pornviewer.models.VideoCard;
import com.plovdev.pornviewer.models.VideoInfo;
import com.plovdev.pornviewer.utility.Globals;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class MainMenu extends Application {
    private static final Logger log = LoggerFactory.getLogger(MainMenu.class);
    private final PBPornHandler handler = new PBPornHandler();
    private static Runnable onStarted = () -> {};

    @Override
    public void start(Stage stage) {
        Globals.setPrimaryStage(stage);

        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/plovdev/pornviewer/pv-logo.png")));
        stage.getIcons().add(icon);

        PornTabPane pane = new PornTabPane(stage);
        Scene scene = new Scene(pane, 1000, 600);
        stage.setOnCloseRequest(e -> {
            try {
                stop();
            } catch (Exception ex) {
                log.error("Stop error: ", ex);
            }
            System.exit(0);
        });

        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/plovdev/pornviewer/styles/white.css")).toExternalForm());
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setTitle("PornViewer");
        stage.show();
        initDeepLinkHandler(stage);

        onStarted.run();
    }
    public static void setStartListener(Runnable r) {
        Objects.requireNonNull(r);
        onStarted = r;
    }

    private void initDeepLinkHandler(Stage stage) {
        PornVideoAdapter adapter = UserPreferences.get("0000").getPornAdapter();
        PornParser parser = adapter.getParser();
        Resourcer resourcer = adapter.getResourcer();

        DeepLinkListener.addListener("share", link -> {
            if (link.getAction().equals("video")) {
                int id = Integer.parseInt(link.getParams().get("id"));
                String url = resourcer.buildVideoUrlFromId(id);
                log.info("Watching video: {}", url);
                VideoCard card = new VideoCard();
                card.setCardId(id);
                card.setUrl(url);
                VideoInfo info = parser.parseVideo(url);
                card.setInfo(info);
                card.setTitle(info.getTitle());
                log.info("Creating player...");
                Platform.runLater(() -> {
                    try {
                        log.info("Now in UI Thread, creating VideoPlayerPane...");
                        VideoPlayerPane player = new VideoPlayerPane(card);
                        player.show();
                    } catch (Exception e) {
                        log.error("Error in UI thread", e);
                    }
                });
            }
        });

        DeepLinkListener.addListener("favorites", link -> {
            String action = link.getAction();
            int id = Integer.parseInt(link.getParams().get("id"));
            if (action.equals("add")) {
                VideoInfo info = parser.parseVideo(resourcer.buildVideoUrlFromId(id));
                String duration = DurationUtils.formatDurationToString(info.getDuration());
                FavoriteVideo video = new FavoriteVideo(id, info.getTitle(), info.getUrl(), info.getPic(), duration, info.getViews(), info.getRating(), info, true, null);
                FavoriteVideos.add(video);
                FavoriteListener.notifyListeners(video);
                Platform.runLater(() -> new Toast(stage, "Добавлено в избранное").show());
            } else if (action.equals("remove")) {
                VideoInfo info = parser.parseVideo(resourcer.buildVideoUrlFromId(id));
                String duration = DurationUtils.formatDurationToString(info.getDuration());
                FavoriteVideo video = new FavoriteVideo(id, info.getTitle(), info.getUrl(), info.getPic(), duration, info.getViews(), info.getRating(), info, false, null);
                FavoriteVideos.remove(String.valueOf(id));
                FavoriteListener.notifyListeners(video);
                Platform.runLater(() -> new Toast(stage, "Удалено из избранного").show());
            }
        });
    }
}
