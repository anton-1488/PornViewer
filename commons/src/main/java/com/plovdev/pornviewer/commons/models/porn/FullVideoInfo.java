package com.plovdev.pornviewer.commons.models.porn;

import com.plovdev.pornviewer.commons.models.video.VideoQuality;
import com.plovdev.pornviewer.commons.models.video.VideoRating;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record FullVideoInfo(List<Comment> comments, int views, VideoRating rating, Map<VideoQuality, URI> qualityMap,
                            List<Timecode> timecodes, Map<String, URI> tagLinks, Map<String, URI> modelsLinks,
                            List<CategoryInfo> categories, Duration videoDuration, String videoId, String description,
                            String title, URI videoUri, URI previewUrl) {

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FullVideoInfo that = (FullVideoInfo) o;
        return Objects.equals(videoId, that.videoId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(videoId);
    }
}