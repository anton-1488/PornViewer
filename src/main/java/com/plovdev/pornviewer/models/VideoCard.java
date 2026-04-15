package com.plovdev.pornviewer.models;

import com.plovdev.pornviewer.databases.FavoriteVideos;
import com.plovdev.pornviewer.events.listeners.FavoriteListener;
import com.plovdev.pornviewer.gui.video.VideoPlayerPane;
import com.plovdev.pornviewer.httpquering.defimpl.PBPornHandler;
import com.plovdev.pornviewer.pornimpl.porn365.DefPornParser;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoCard extends PornCard {
    private static final Logger log = LoggerFactory.getLogger(VideoCard.class);
    protected static final ExecutorService previewLoader = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    protected String duration;
    protected int views;
    protected String rating;
    protected VideoInfo info;
    protected boolean isFavorite;
    private final SVGPath favPath = new SVGPath();
    protected List<String> tags;

    private List<DownloadedVideoInfo.Timecode> timecodes = new ArrayList<>();

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    protected final PBPornHandler handler = new PBPornHandler();

    public boolean isFavorite() {
        return isFavorite;
    }

    public List<DownloadedVideoInfo.Timecode> getTimecodes() {
        return timecodes;
    }

    public void setTimecodes(List<DownloadedVideoInfo.Timecode> timecodes) {
        this.timecodes = timecodes;
    }

    public VideoCard(int id, String title, String url, String pic, String duration, int views, String rating, VideoInfo info, boolean isFavorite) {
        super(id, title, url, pic);
        this.duration = duration;
        this.views = views;
        this.rating = rating;
        this.info = info;
        this.isFavorite = isFavorite;
    }

    public VideoCard(int id, String title, String url, String pic, String duration, int views, String rating) {
        super(id, title, url, pic);
        this.duration = duration;
        this.views = views;
        this.rating = rating;
        this.info = null;
        this.isFavorite = false;
    }

    public VideoCard(int id, String title, String url, String pic) {
        super(id, title, url, pic);
    }

    public VideoCard() {

    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
        favPath.setFill(isFavorite() ? Color.rgb(255, 15, 45) : Color.TRANSPARENT);
    }

    public void makeFavorite() {
        if (isFavorite) {
            System.out.println("Adding to favorite");
            FavoriteVideos.add(FavoriteVideo.ofVideoCard(this));
        } else {
            System.out.println("Removing from favorite");
            FavoriteVideos.remove(String.valueOf(getCardId()));
        }
        FavoriteListener.notifyListeners(FavoriteVideo.ofVideoCard(this));
    }

    public VideoInfo getInfo() {
        return info;
    }

    public void setInfo(VideoInfo info) {
        this.info = info;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    @Override
    public void render() {
        try {
            DefPornParser parser = new DefPornParser();
            StackPane mainContainer = new StackPane();
            mainContainer.setMaxWidth(400);

            Label label = new Label(String.valueOf(views));
            label.getStyleClass().add("marker");

            Hyperlink titleLabel = new Hyperlink(title);
            titleLabel.setOnAction(e -> {
                System.out.println(getUrl());
                setInfo(parser.parseVideo(getUrl()));
                VideoPlayerPane player = new VideoPlayerPane(this);
                player.show();
            });
            titleLabel.getStyleClass().add("video-title");
            titleLabel.setMaxWidth(350);
            Label dur = new Label(duration);
            dur.getStyleClass().add("marker");

            Label rating = new Label(this.rating);

            HBox hBox = new HBox(10, titleLabel, rating);
            hBox.getStyleClass().add("title-box");
            VBox box = new VBox(10, hBox);
            box.getStyleClass().add("trailer");

            previewLoader.execute(() -> {
                try {
                    Image image;
                    if (pic.endsWith(".webp")) {
                        image = SwingFXUtils.toFXImage(ImageIO.read(new ByteArrayInputStream(handler.getBytes(pic))), null);
                    } else {
                        image = new Image(pic);
                    }
                    ImageView view = new ImageView(image);
                    view.setSmooth(true);
                    view.getStyleClass().add("video-preview");
                    view.setFitWidth(420);
                    view.setFitHeight(250);
                    view.setPreserveRatio(true);

                    Platform.runLater(() -> box.getChildren().addFirst(view));
                } catch (Exception e) {
                    log.error("Error to load preview: {}", e.getMessage());
                }
            });

            // Панель с дополнительной информацией (показывается при наведении)
            Region region = new Region();
            HBox.setHgrow(region, Priority.ALWAYS);

            Region r = new Region();
            VBox.setVgrow(r, Priority.ALWAYS);

            Region r2 = new Region();
            VBox.setVgrow(r2, Priority.ALWAYS);

            Region r1 = new Region();
            HBox.setHgrow(r1, Priority.ALWAYS);

            Label fav = new Label();
            favPath.setContent("M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z");

            favPath.setStrokeWidth(2);
            favPath.setStroke(Color.WHITE);
            favPath.setFill(isFavorite() ? Color.rgb(255, 15, 45) : Color.TRANSPARENT);
            fav.setGraphic(favPath);
            fav.setTranslateX(3.5);


            ImageView download = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/plovdev/pornviewer/download.png"))));
            download.setFitWidth(30);
            download.setFitHeight(30);
            download.setPreserveRatio(true);

            download.setOnMousePressed(e -> {
                setInfo(parser.parseVideo(getUrl()));
                showContextMenu(download, e.getScreenX(), e.getScreenY());
            });

            VBox vBox = new VBox(new HBox(r1, new VBox(10, fav, download)), r, new HBox(label, region, dur));
            vBox.setPadding(new Insets(10));
            HBox.setHgrow(vBox, Priority.ALWAYS);


            HBox infoOverlay = new HBox(vBox);
            infoOverlay.setVisible(false);
            infoOverlay.getStyleClass().add("video-hover");

            // Позиционируем overlay поверх изображения
            StackPane.setAlignment(infoOverlay, Pos.BOTTOM_CENTER);
            StackPane.setMargin(infoOverlay, new Insets(0, 0, 50, 0));

            // Собираем всё вместе
            mainContainer.getChildren().addAll(box, infoOverlay);

            // Обработчики наведения
            mainContainer.setOnMouseEntered(e -> infoOverlay.setVisible(true));
            mainContainer.setOnMouseExited(e -> infoOverlay.setVisible(false));
            fav.setOnMousePressed(e -> {
                setFavorite(!isFavorite());
                makeFavorite();
            });

            getChildren().add(mainContainer);
        } catch (Exception e) {
            log.error("Rendering error: ", e);
        }
    }

    private void showContextMenu(ImageView node, double x, double y) {
        ContextMenu menu = new ContextMenu();
        menu.getStyleClass().add("options");
        for (String str : info.getUrls().keySet()) {
            menu.getItems().add(getLoader(str));
        }
        menu.show(node, x, y);
        if (menu.getScene() != null) {
            menu.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/plovdev/pornviewer/styles/context-menu.css")).toExternalForm());
        }
    }

    protected MenuItem getLoader(String qual) {
        MenuItem item = new MenuItem(qual);
        item.setOnAction(e -> new Thread(() -> {
            String url = info.getUrls().get(qual);
            handler.downloadPorn(url, info.getTitle(), info);
        }).start());
        return item;
    }


    public String getVideoDuration(javafx.util.Duration total) {
        if (total != javafx.util.Duration.UNKNOWN) {
            BigDecimal mills = new BigDecimal(String.valueOf(total.toMillis()));

            BigDecimal totalSeconds = mills.divide(new BigDecimal("1000.0"), 10, RoundingMode.HALF_UP);

            int hours = totalSeconds.intValue() / (60 * 60);
            String h = "";
            if (hours != 0) h = hours + ":";

            BigDecimal minutes = totalSeconds.divide(new BigDecimal("60.0"), 10, RoundingMode.HALF_UP);
            BigDecimal seconds = totalSeconds.remainder(new BigDecimal("60.0"));

            long sec = Math.round(seconds.doubleValue());
            long min = Math.round(minutes.doubleValue());

            return h + String.format("%2s:%2s", min, sec);
        }
        return "00:00";
    }

    @Override
    public String toString() {
        return "VideoCard{" +
                "duration='" + duration + '\'' +
                ", views=" + views +
                ", rating='" + rating + '\'' +
                ", info=" + info +
                ", isFavorite=" + isFavorite +
                ", favPath=" + favPath +
                ", handler=" + handler +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", pic='" + pic + '\'' +
                '}';
    }
}