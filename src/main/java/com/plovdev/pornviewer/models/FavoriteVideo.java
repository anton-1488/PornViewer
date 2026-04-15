package com.plovdev.pornviewer.models;

import com.plovdev.pornviewer.utility.json.JSONSerializer;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class FavoriteVideo extends VideoCard {
    private static final Logger log = LoggerFactory.getLogger(FavoriteVideo.class);
    private String group;

    public FavoriteVideo(int id, String title, String url, String pic, String duration, int views, String rating, VideoInfo info, boolean isFavorite, String group) {
        super(id, title, url, pic, duration, views, rating, info, isFavorite);
        this.group = group;
        setupDragAndDrop();
    }
    public FavoriteVideo(int id, String title, String url, String pic, String duration, int views, String rating) {
        super(id, title, url, pic, duration, views, rating);
        this.group = null;
        setupDragAndDrop();
    }

    public FavoriteVideo(int id, String title, String url, String pic) {
        super(id, title, url, pic);
        setupDragAndDrop();
    }
    public FavoriteVideo() {
        setupDragAndDrop();
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public static FavoriteVideo ofVideoCard(VideoCard card) {
        return new FavoriteVideo(card.getCardId(), card.getTitle(), card.getUrl(), card.getPic(), card.getDuration(), card.getViews(), card.getRating(), card.getInfo(), card.isFavorite(), null);
    }

    private void setupDragAndDrop() {
        setOnDragDetected(event -> {
            Dragboard db = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(JSONSerializer.serialize(toInfo(this)));
            db.setContent(content);

            setOpacity(0.5);
            event.consume();
        });

        setOnDragDone(event -> {
            setOpacity(1.0);
            event.consume();
        });
    }

    public static FavoriteVideo ofInfo(FavoriteVideoInfo info) {
        return new FavoriteVideo(info.getId(), info.getTitle(), info.getUrl(), info.getPic(), info.getDuration(), info.getViews(), info.getRating(), info.getInfo(), info.isFavorite(), info.getGroup());
    }
    public static FavoriteVideoInfo toInfo(FavoriteVideo video) {
        return new FavoriteVideoInfo(video.getCardId(), video.getTitle(), video.getUrl(), video.getPic(), video.getDuration(), video.getViews(), video.getRating(), video.getInfo(), video.isFavorite(), video.getGroup());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FavoriteVideo that = (FavoriteVideo) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "FavoriteVideo{" +
                "group='" + group + '\'' +
                ", duration='" + duration + '\'' +
                ", views=" + views +
                ", rating='" + rating + '\'' +
                ", info=" + info +
                ", isFavorite=" + isFavorite +
                ", handler=" + handler +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", pic='" + pic + '\'' +
                '}';
    }
}