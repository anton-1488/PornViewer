package com.plovdev.pornviewer.utils.json.typeadapters;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Duration;

public class DurationTypeAdapter implements JsonSerializer<Duration>, JsonDeserializer<Duration> {

    @Override
    public JsonElement serialize(Duration duration, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(duration.toString());
    }

    @Override
    public Duration deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return Duration.parse(jsonElement.getAsString());
    }
}