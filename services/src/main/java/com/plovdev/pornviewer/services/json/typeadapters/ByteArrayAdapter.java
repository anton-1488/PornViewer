package com.plovdev.pornviewer.services.json.typeadapters;

import com.google.gson.*;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class ByteArrayAdapter implements JsonTypeAdapter<byte[]> {
    @Override
    public JsonElement serialize(byte[] bytes, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(new String(bytes, StandardCharsets.UTF_8));
    }

    @Override
    public byte[] deserialize(@NonNull JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        String bytesStr = jsonElement.getAsString();
        if (bytesStr == null || bytesStr.isBlank()) {
            return new byte[0];
        }

        return bytesStr.getBytes(StandardCharsets.UTF_8);
    }
}