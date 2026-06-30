package com.plovdev.pornviewer.services.json.typeadapters;

import com.google.gson.*;
import com.plovdev.pornviewer.core.models.video.VideoQuality;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Type;

public class VideoQualityAdapter implements JsonTypeAdapter<VideoQuality> {
    @Override
    public JsonElement serialize(@NonNull VideoQuality videoQuality, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(videoQuality.name());
    }

    @Override
    public VideoQuality deserialize(@NonNull JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return VideoQuality.fromString(jsonElement.getAsString());
    }
}