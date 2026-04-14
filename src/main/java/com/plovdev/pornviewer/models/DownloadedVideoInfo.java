package com.plovdev.pornviewer.models;

import com.google.gson.annotations.SerializedName;
import com.plovdev.pornviewer.utility.json.VideoInfoSerializer;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

public class DownloadedVideoInfo {
    private static final Logger log = LoggerFactory.getLogger(DownloadedVideoInfo.class);

    private transient byte[] previewBytes;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("url")
    private String url;

    @SerializedName("timecodes")
    private List<Timecode> timecodes;

    @SerializedName("tags")
    private List<String> tags;

    @SerializedName("duration")
    private Duration totalDuration;

    public DownloadedVideoInfo(byte[] previewBytes, String title, String descr, String url, List<Timecode> timecodes, List<String> tags, Duration totalDuration) {
        this.previewBytes = previewBytes;
        this.title = title;
        this.description = descr;
        this.url = url;
        this.timecodes = timecodes;
        this.tags = tags;
        this.totalDuration = totalDuration;
    }
    public DownloadedVideoInfo(String title, String descr, String url, List<Timecode> timecodes, List<String> tags, Duration totalDuration) {
        this.title = title;
        this.description = descr;
        this.url = url;
        this.timecodes = timecodes;
        this.tags = tags;
        this.totalDuration = totalDuration;
    }

    public DownloadedVideoInfo() {
    }

    public byte[] getPreviewBytes() {
        return previewBytes;
    }

    public void setPreviewBytes(byte[] previewBytes) {
        this.previewBytes = previewBytes;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Timecode> getTimecodes() {
        return timecodes;
    }

    public void setTimecodes(List<Timecode> timecodes) {
        this.timecodes = timecodes;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Duration getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(Duration totalDuration) {
        this.totalDuration = totalDuration;
    }

    public Image javaFxPreview() {
        return new Image(new ByteArrayInputStream(previewBytes));
    }
    public BufferedImage javaAwtPreview() {
        try {
            return ImageIO.read(new ByteArrayInputStream(previewBytes));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String formJson() {
        return VideoInfoSerializer.serializeInfo(title, description, url, totalDuration, tags, timecodes);
    }
    public static DownloadedVideoInfo ofInfo(String json) {
        return VideoInfoSerializer.deserializeInfo(json);
    }

    public static class Timecode {
        @SerializedName("time")
        private Duration time;
        @SerializedName("text")
        private String text;

        public Timecode(Duration time, String text) {
            this.time = time;
            this.text = text;
        }

        public Duration getTime() {
            return time;
        }

        public void setTime(Duration time) {
            this.time = time;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Timecode timecode = (Timecode) o;
            return Objects.equals(time, timecode.time) && Objects.equals(text, timecode.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(time, text);
        }

        @Override
        public String toString() {
            return String.format("[%s] - %s", time.toString(), text);
        }
    }

    @Override
    public String toString() {
        return "DownloadedVideoInfo{" +
                "preview Size=" + previewBytes.length +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", timecodes=" + timecodes +
                ", tags=" + tags +
                ", totalDuration=" + totalDuration +
                '}';
    }
}