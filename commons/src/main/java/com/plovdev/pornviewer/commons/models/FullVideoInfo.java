package com.plovdev.pornviewer.commons.models;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public record FullVideoInfo(List<Comment> comments, int views, VideoRating rating, Map<VideoQuality, URI> qualityMap, List<Timecode> timecodes, Map<String, URI> tagLinks, Map<String, URI> modelLiks, Duration videoDuration, int videoId, String description, String title, URI videoUri) {
    @Override
    @NotNull
    public String toString() {
        return "FullVideoInfo{" +
                "comments=" + comments +
                ", views=" + views +
                ", rating=" + rating +
                ", qualityMap=" + qualityMap +
                ", timecodes=" + timecodes +
                ", tagLinks=" + tagLinks +
                ", modelLiks=" + modelLiks +
                ", videoDuration=" + videoDuration +
                ", videoId=" + videoId +
                ", description='" + description + '\'' +
                ", title='" + title + '\'' +
                ", videoUri=" + videoUri +
                '}';
    }
}