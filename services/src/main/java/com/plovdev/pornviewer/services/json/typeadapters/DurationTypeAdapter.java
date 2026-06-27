package com.plovdev.pornviewer.services.json.typeadapters;

import com.google.gson.*;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Type;
import java.time.Duration;

public class DurationTypeAdapter implements JsonSerializer<Duration>, JsonDeserializer<Duration> {

    @Override
    public JsonElement serialize(@NonNull Duration duration, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(duration.toString());
    }

    @Override
    public Duration deserialize(@NonNull JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return Duration.parse(jsonElement.getAsString());
    }
}