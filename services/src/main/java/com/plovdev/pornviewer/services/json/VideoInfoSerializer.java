package com.plovdev.pornviewer.services.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.plovdev.pornviewer.core.models.porn.FullVideoInfo;
import com.plovdev.pornviewer.core.models.porn.Timecode;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class VideoInfoSerializer {
    public static @NonNull String serializeInfo(@NonNull FullVideoInfo info) {
        return serializeInfo(info.title(), info.description(), info.videoUri(), info.videoDuration(), info.tagLinks(), info.timecodes());
    }

    public static @NonNull String serializeInfo(String title, String description, URI url, Duration duration, @NonNull Map<String, URI> tags, List<Timecode> timecodes) {
        return serializeInfo(title, description, url, duration, tags.keySet().stream().toList(), timecodes);
    }

    public static @NonNull String serializeInfo(String title, String description, @NonNull URI url, @NonNull Duration duration, @NonNull List<String> tags, List<Timecode> timecodes) {
        JsonObject infoObject = new JsonObject();
        infoObject.addProperty("title", title);
        infoObject.addProperty("description", description);
        infoObject.addProperty("url", url.toString());
        infoObject.addProperty("duration", duration.toString());

        JsonArray tagsArray = new JsonArray();
        for (String tag : tags) {
            tagsArray.add(tag);
        }
        infoObject.add("tags", tagsArray);

        JsonArray timecodesArray = new JsonArray();
        for (Timecode timecode : timecodes) {
            JsonObject timecodeObj = new JsonObject();
            timecodeObj.addProperty("time", timecode.time().toString());
            timecodeObj.addProperty("text", timecode.text());
            timecodesArray.add(timecodeObj);
        }
        infoObject.add("timecodes", timecodesArray);

        return JSONSerializer.GSON.toJson(infoObject);
    }

    public static DownloadedVideoInfo deserializeInfo(String json) {
        return JSONSerializer.GSON.fromJson(json, DownloadedVideoInfo.class);
    }
}