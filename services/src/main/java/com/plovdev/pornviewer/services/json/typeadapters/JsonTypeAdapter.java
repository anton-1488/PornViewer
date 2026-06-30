package com.plovdev.pornviewer.services.json.typeadapters;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

public interface JsonTypeAdapter<T> extends JsonSerializer<T>, JsonDeserializer<T> {
}